# Validation + licence expiry + renewal automation

This update adds three things on top of your DTO version:

1. **Entity fixes**: SoftwareLicense, SalesTask, SalesCall, Lead, Renewal
2. **Validation** on every request DTO, plus one consistent error format
3. **Licence expiry logic + a daily renewal scheduler** that creates Renewal → SalesTask → Notifications

---

## ⚠️ Step 0 — reset your test database (one time)

Several column types changed (for example licence `status` went from boolean to text, and `leadScore` from int to Integer).
`ddl-auto: update` adds new columns but **cannot change or drop old ones**, so old data will clash.
Because this is still test data, the simplest fix is to recreate the schema. In pgAdmin, on `automationDB`:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Start the app again and Hibernate will create all tables fresh.

---

## 1. What changed in the entities

| Entity | Change | Why |
|---|---|---|
| **SoftwareLicense** | + `licenseQuantity`, + `durationMonths`, `status` is now enum `LicenseStatus` (ACTIVE / EXPIRING_SOON / EXPIRED / RENEWED) | the renewal call needs the quantity, and the status must be more than true/false |
| **SalesTask** | now a real to-do: `title`, `description`, `taskType`, `priority` (LOW/MEDIUM/HIGH/URGENT), `taskStatus` (OPEN/IN_PROGRESS/DONE/CANCELLED), `dueDate` | the call fields were in the wrong entity |
| **SalesCall** | + `callType`, `callStatus`, `startTime`, `endTime`, `transcript`, `callSummary`, `outcome`, `nextAction` | moved here from SalesTask; the AI call will fill these later |
| **Lead** | + `phone`, `requiredLicenses`, `existingLicenses`, `preferredDurationMonths`, `purchaseTimeline`; `leadScore` → Integer | website form + qualification answers |
| **Renewal** | `status` is now enum `RenewalStatus` (PENDING / IN_PROGRESS / RENEWED / LOST), + `lastReminderDays`, counts → Integer | lets the scheduler avoid duplicate reminders and stop after renewal |

New enum files: `LicenseStatus`, `RenewalStatus`, `TaskStatus`, `TaskPriority` (each in its module's `model` folder).

---

## 2. Validation

- Request DTOs use `@NotBlank`, `@NotNull`, `@Email`, `@Positive`, `@PositiveOrZero`, `@Size`, `@Min`/`@Max` and `@Pattern` (phone numbers).
- Every controller uses `@Valid @RequestBody`. Without `@Valid`, the annotations on the DTO are ignored.
- Business rules that annotations can't express throw `BadRequestException` (400). For example:
  - a licence needs `expiryDate` **or** `durationMonths`
  - `expiryDate` must be after `purchaseDate`
  - a call must have a `customerId` or a `leadId`

### Error format (all errors look like this now)

```json
{
  "timestamp": "2026-10-06T08:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/customers",
  "fieldErrors": {
    "customerEmail": "customerEmail must be a valid email address"
  }
}
```

| Situation | Status |
|---|---|
| `@Valid` failed | 400 + `fieldErrors` |
| Business rule (`BadRequestException`) | 400 |
| Broken JSON / wrong date format / unknown enum value | 400 |
| Wrong type in the URL (`/api/customers/abc`) | 400 |
| Id not found | 404 |
| Duplicate unique value, or deleting a record still in use | 409 |
| Anything else | 500 (full error is logged in the console) |

**Date format** in JSON: `"2026-10-01T09:00:00"` for date-times, `"2026-10-01"` for `dueDate`.

---

## 3. Licence expiry logic

`LicenseExpiryCalculator` (in `softwarelicense/service`) is the single place that works out:
- `daysRemaining`: today → expiry date (negative means expired)
- `status`:
  - **RENEWED** stays RENEWED
  - **EXPIRED** if `days < 0`
  - **EXPIRING_SOON** if `days <= 90`
  - otherwise **ACTIVE**

When you create or update a licence:
- you **don't send `status`**; it is calculated
- if you send `durationMonths` without `expiryDate`, the expiry date = purchaseDate + months
- if you send `expiryDate` without `durationMonths`, the duration is calculated
- the response includes `daysRemaining`

Endpoints:

| Endpoint | What it does |
|---|---|
| `GET /api/licenses/expiring?days=30` | not expired, expires within N days |
| `GET /api/licenses/expired` | expired and not renewed |
| `GET /api/licenses/{id}/expiring-soon?days=30` | true / false |
| `GET /api/licenses/status/EXPIRING_SOON` | by status |

---

## 4. Renewal automation (the scheduler)

```
Every day at 08:00  (RenewalScheduler -> RenewalAutomationService.runDailyCheck)
  for every licence that is not RENEWED:
    1. recalculate status (ACTIVE / EXPIRING_SOON / EXPIRED)
    2. which reminder stage has it reached? (90 / 60 / 30 / 14 / 7)
         e.g. 58 days left -> stage 60;   3 days left -> stage 7;   120 days -> none
    3. renewal already RENEWED or LOST?          -> skip (reminders stop)
    4. find the open Renewal, or create one (PENDING)
    5. this stage already handled? (lastReminderDays) -> skip (no duplicates)
    6. cancel older OPEN tasks for this renewal, then create:
         - SalesTask  (assigned to the licence's salesperson; priority 7d=URGENT, 14d=HIGH, 30d=MEDIUM, else LOW)
         - Notification RENEWAL_REMINDER_INTERNAL  (for the salesperson)
         - Notification RENEWAL_REMINDER_CUSTOMER  (for the customer, status PENDING – sending email/SMS comes later)
    7. save lastReminderDays = stage
```

It uses "stage reached" rather than "exactly 60 days left", so if the server was off on a particular day, the reminder still happens the next day.

**When the customer renews:** `PATCH /api/renewals/{id}/status?status=RENEWED`
→ the licence becomes RENEWED, the renewal's open tasks are cancelled, and no more reminders are sent.
Status `LOST` also stops reminders.

**Run it now instead of waiting until 08:00:** `POST /api/renewals/automation/run`. It returns a summary:

```json
{ "licensesChecked": 4, "statusesUpdated": 3, "renewalsCreated": 2,
  "tasksCreated": 2, "notificationsCreated": 4, "skippedAlreadyReminded": 0 }
```

### Settings (application.yaml, no code change needed)

```yaml
app:
  renewal:
    reminder-days: 90,60,30,14,7
    expiring-soon-days: 90
    task-due-in-days: 3
    scheduler-cron: "0 0 8 * * *"     # sec min hour day month weekday
```

### New / changed files

- `AutomationSalesApplication`: + `@EnableScheduling`, `@ConfigurationPropertiesScan`
- `config/RenewalProperties`: reads `app.renewal.*`
- `softwarelicense/service/LicenseExpiryCalculator`
- `renewal/service/RenewalAutomationService` + `Impl`
- `renewal/scheduler/RenewalScheduler`
- `renewal/dto/RenewalAutomationResultDto`
- `exceptionHandling/BadRequestException`, `ErrorResponse`, updated `GlobalExceptionHandler`

### Other new endpoints

- `PATCH /api/sales-tasks/{id}/status?taskStatus=DONE`
- `GET /api/sales-tasks/status/OPEN`
- `GET /api/sales-tasks/overdue`
- `GET /api/sales-tasks/customer/{id}`
- `GET /api/sales-tasks/salesperson/{id}/status?taskStatus=OPEN`
- `GET /api/renewals/status/PENDING`
- `GET /api/renewals/customer/{id}`
- `GET /api/sales-calls/between?from=...&to=...` (replaces the old exact `start-time` / `end-time` search on tasks)

---

## 5. Testing in Postman

1. Reset the database (Step 0) and start the app.
2. In Postman: **Import** → `postman/AutomationSales.postman_collection.json`.
3. Right-click the collection → **Run collection** → **Run**. All 35 requests run in order with automatic checks.
   You can also click through the folders one by one (1 → 6) to see each response.

The collection builds dates relative to today, so "expires in 58 days" stays correct whenever you run it.
For folder 4 to match exactly, run it on a fresh database, because licences from earlier runs also get reminders.

What each folder proves:

1. **Setup**: salesperson, customer, vendor, product (`active` defaults to true)
2. **Licences**: status and `daysRemaining` are calculated; expiry is calculated from `durationMonths`
3. **Expiry queries**: expiring, expired, expiring-soon
4. **Automation**:
   - the run creates a renewal, a task and 2 notifications per licence
   - running again creates **nothing**
   - marking the renewal RENEWED → licence RENEWED and its task CANCELLED
5. **Validation**: 400 with `fieldErrors`, 404, a bad enum, a bad phone number, missing customer/lead on a call
6. **Leads / calls / tasks**: qualification fields, logging a call, ticking a task off

---

## 6. What's next (from your roadmap)

- **Notifications – actually send them:** a job that picks `PENDING` notifications and emails them (Spring Mail), then sets `sentAt` and status `SENT`.
- **Lead workflow:** a public `POST /api/public/enquiries` for the website form, lead statuses as an enum, auto-assigning a salesperson.
- **React frontend.**
- **Security (JWT).**
