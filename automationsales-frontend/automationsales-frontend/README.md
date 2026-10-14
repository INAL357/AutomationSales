# AutomationSales — React frontend

A React app (built with [Vite](https://vite.dev)) for the AutomationSales Spring Boot API.
It is a **separate project**: it runs on its own port (5173) and talks to your backend (8080) through `/api`.

## 1. Run it

You need **Node.js 20.19+ or 22.12+** ([nodejs.org](https://nodejs.org), choose LTS).

```bash
cd automationsales-frontend
npm install        # only the first time
npm run dev
```

Open **http://localhost:5173**. Make sure the Spring Boot backend is running on port 8080.

| Page | URL |
|---|---|
| Sign in | http://localhost:5173/login |
| App (after sign in) | http://localhost:5173/ |
| Public "Request a demo" form (for your website) | http://localhost:5173/request-demo |

## 2. How it connects to the backend

In development **you don't need CORS**. `vite.config.js` forwards every request that starts with `/api` to `http://localhost:8080`:

```
browser → http://localhost:5173/api/leads → (Vite forwards) → http://localhost:8080/api/leads
```

That's why the code only ever calls `/api/...`.

The JWT from `POST /api/auth/login` is saved in `localStorage` and sent as `Authorization: Bearer <token>` on every request (see `src/api.js`).

## 3. What the backend needs

Check these on the Spring Boot side:

1. **Public product list for the demo form.** `request-demo` calls `GET /api/products` without a token. Add this to `SecurityConfig`:
   ```java
   .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
   ```
   `POST /api/leads/public` is already public.

2. **Editing a salesperson.** The Team page sends `password: null` when editing (the password is only needed when creating). If your `SalesPersonRequestDto.password` has `@NotBlank`, editing will fail with "password is required". Remove `@NotBlank` there, and check for a password inside `createSalesPerson` instead.

3. **CORS, only for production on a different domain.** If you later host the frontend on another domain than the API (e.g. `app.yourcompany.com` and `api.yourcompany.com`), add a CORS config in Spring that allows the frontend's address, and put the API address in `.env` (see `.env.example`). If you serve both from the same domain (e.g. Nginx sends `/api` to Spring), you don't need CORS at all.

## 4. Build for production

```bash
npm run build
```

This creates a `dist/` folder with plain HTML/CSS/JS. You can host it on Nginx, Netlify, Vercel, S3, etc.

The app uses real URLs like `/leads`, so the server must send `index.html` for unknown paths. With Nginx:

```nginx
location / {
  try_files $uri /index.html;
}
location /api/ {
  proxy_pass http://localhost:8080;
}
```

## 5. Where everything is

```
src/
├── main.jsx                 starting point: puts <App /> on the page
├── App.jsx                  which page is shown for which URL (React Router)
├── api.js                   apiGet / apiPost / apiPut / apiPatch / apiDelete + saving the login
├── lists.js                 loadCustomers(), loadLeads()... (salespeople get only THEIR records)
├── utils.js                 formatDate, niceLabel, prepareForApi, showToast...
├── styles.css               all styling (colours at the top)
├── components/
│   ├── Layout.jsx           sidebar menu + the current page
│   ├── ProtectedRoute.jsx   sends you to /login if not signed in (adminOnly for admin pages)
│   ├── Modal.jsx            popup window
│   ├── Badge.jsx            coloured status pill
│   └── FormParts.jsx        Field, FormError, EnumOptions, Loading, ErrorBox
└── pages/                   one file per page
    ├── CustomersPage.jsx    ← READ THIS FIRST: it has the most comments
    ├── LeadsPage.jsx        pipeline board + list, change stage from the card
    ├── TasksPage.jsx        grouped Overdue / Today / Upcoming, tick to complete
    ├── LicensesPage.jsx     tabs All / Expiring / Expired
    ├── RenewalsPage.jsx     change status in the table, "Run automation now" (admin)
    ├── DashboardPage.jsx, CallsPage.jsx, NotificationsPage.jsx,
    ├── ProductsPage.jsx, VendorsPage.jsx, DocumentsPage.jsx
    ├── TeamPage.jsx         salespeople + logins, enable/disable (admin only)
    ├── LoginPage.jsx, RequestDemoPage.jsx, NotFoundPage.jsx
```

## 6. How a page works

Every page follows the same pattern (see `CustomersPage.jsx`):

1. **`useState`** holds the list, the search text, and which popup is open (`editing`).
2. **`useEffect(() => { loadData(); }, [])`** loads the data once when the page opens.
3. **The table** shows `list.filter(...)` for the current search.
4. **Clicking a row** sets `editing` to that record, which opens the form popup. **Saving** calls `apiPost` (new) or `apiPut` (edit), then `loadData()` again.

Each form keeps all its fields in one `form` state object, and has a single `handleChange` that uses the input's `name`:

```jsx
function handleChange(event) {
  setForm({ ...form, [event.target.name]: event.target.value });
}
<input name="customerName" value={form.customerName} onChange={handleChange} />
```

Before sending, `prepareForApi(form, numberFields, dateTimeFields)` turns `''` into `null`, ids into numbers, and `"2026-10-09T14:30"` into `"2026-10-09T14:30:00"` (what Spring's `LocalDateTime` expects).

## 7. Add a new page (example: Quotes)

1. Copy `src/pages/VendorsPage.jsx` to `src/pages/QuotesPage.jsx` (it's the simplest page).
2. Rename the components, and change the URLs (`/vendors` to `/quotes`) and field names to match your DTO.
3. In `App.jsx` add `import QuotesPage from './pages/QuotesPage';` and `<Route path="/quotes" element={<QuotesPage />} />`.
4. In `components/Layout.jsx` add `{ to: '/quotes', label: 'Quotes', icon: '✎' }` to `MENU`.

## 8. Good to know

- **Security:** React shows text safely, so a lead named `<script>…</script>` appears as plain text. Never use `dangerouslySetInnerHTML` with API data. Links from users are only shown if they start with `http://` or `https://` (`isSafeLink` in `utils.js`).
- **Hiding a button is not security.** The app hides admin buttons from salespeople, but the backend must also block those requests (`@PreAuthorize("hasRole('ADMIN')")`). The backend also needs to make sure salespeople can only read their own records, because the frontend just *asks* for them via `/…/salesperson/{id}`.
- **Currency:** prices show as AED. Change it in `formatMoney` in `utils.js`.
