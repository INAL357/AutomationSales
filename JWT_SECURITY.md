# JWT security

Every `/api/**` endpoint now needs a token, except login and the public website lead form.

## 1. Before you start the app

1. **Reset the users table.** The `User` entity changed (column names, a unique email, a longer password column for BCrypt hashes, and `@OneToOne` instead of the wrong `@OneToMany` to SalesPerson). In pgAdmin:
   ```sql
   DROP TABLE IF EXISTS password_reset_tokens, password_reset, users CASCADE;
   ```
   (or reset the whole schema as in `CHANGES_AND_TESTING.md`, Step 0).

2. **Set environment variables** (IntelliJ → Run configuration → Environment variables):

   | Variable | Example | Needed? |
   |---|---|---|
   | `ADMIN_EMAIL` | `admin@automationsales.local` | First run only: creates the first ADMIN if the users table is empty |
   | `ADMIN_PASSWORD` | `ChangeMe123!` (min 8 chars) | First run only |
   | `JWT_SECRET` | output of `openssl rand -base64 48` | Optional locally (there is a dev default in `application.yaml`), **required** anywhere else |
   | `JWT_EXPIRATION_MINUTES` | `60` | Optional (default 60) |

## 2. How to call the API

```http
POST /api/auth/login
Content-Type: application/json

{ "email": "admin@automationsales.local", "password": "ChangeMe123!" }
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": { "userId": 1, "fullName": "Administrator", "email": "...", "role": "ADMIN", ... }
}
```

Send the token on every other request:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Postman:** run `0 - Auth (run first) → Login as admin`. It saves `{{token}}`, and the whole collection now uses Bearer auth with that variable. Set `adminEmail` / `adminPassword` in the collection variables to match your env vars.

## 3. Who can do what

| URL | Access |
|---|---|
| `POST /api/auth/login` | Public |
| `POST /api/leads/public` | Public (website enquiry form) |
| `GET /api/auth/me` | Any logged-in user |
| `/api/users/**` | ADMIN |
| `POST /api/renewals/automation/run` | ADMIN |
| Any `DELETE /api/**` | ADMIN |
| Everything else | Any logged-in user (ADMIN or SALESPERSON) |

The rules are in `security/SecurityConfig.java`. You can also put `@PreAuthorize("hasRole('ADMIN')")` on any controller method.

## 4. Creating more users (ADMIN token)

```http
POST /api/users
{ "fullName": "John Smith", "email": "john@company.com", "password": "Secret123!",
  "role": "SALESPERSON", "salesPersonId": 1 }
```

- `SALESPERSON` users must be linked to an existing SalesPerson (`salesPersonId`), and only one login per SalesPerson.
- `PATCH /api/users/{id}/enabled` with `{ "enabled": false }` blocks a user immediately: the account is checked on every request, so their current token stops working too.
- `GET /api/users`, `GET /api/users/{id}` list users. Passwords are never returned.

## 5. Errors (same JSON shape as all other errors)

| Situation | Status | message |
|---|---|---|
| No `Authorization` header on a protected URL | 401 | Authentication required... |
| Token tampered with / malformed | 401 | Invalid token |
| Token expired | 401 | Token has expired, please log in again |
| Wrong email or password at login | 401 | Invalid email or password |
| Disabled account | 401 | Account is disabled |
| Logged in but wrong role | 403 | You do not have permission to perform this action |

## 6. Files

| File | What it does |
|---|---|
| `security/JwtService` | Creates and verifies HS256 tokens (subject = email, plus `uid`, `role`, issuer, expiry) |
| `security/JwtAuthenticationFilter` | Reads `Authorization: Bearer`, validates, sets the logged-in user |
| `security/CustomerUserDetailsService` | Loads a user by email |
| `security/SecurityUser` | The logged-in user; use `@AuthenticationPrincipal SecurityUser me` in a controller to get `me.getUserId()`, `me.getRole()`, `me.getSalesPersonId()` |
| `security/SecurityConfig` | URL rules, stateless sessions, BCrypt, AuthenticationManager |
| `security/RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `SecurityErrorWriter` | JSON 401 / 403 responses |
| `security/AdminBootstrap` | Creates the first ADMIN from `ADMIN_EMAIL` / `ADMIN_PASSWORD` |
| `security/JwtProperties` | `app.jwt.*` settings |
| `security/auth/AuthController` | `POST /api/auth/login`, `GET /api/auth/me` |
| `appUser/service/UserService(Impl)`, `appUser/controller/UserController` | Admin user management |

## 7. Not included yet (good next steps)

- **SalesPerson data scoping**: a SALESPERSON can currently see all customers/leads. Use `me.getSalesPersonId()` in services to limit results to their own.
- **Refresh tokens / logout**: tokens simply expire after `JWT_EXPIRATION_MINUTES`.
- **Password reset**: the `PasswordReset` entity is fixed and ready, but there is no endpoint yet.
- **CORS**: add a `CorsConfigurationSource` bean when a browser frontend on another port calls the API.
