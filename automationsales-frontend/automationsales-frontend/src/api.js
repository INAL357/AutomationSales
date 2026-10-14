// =====================================================================
// api.js — talking to the Spring Boot backend
// =====================================================================
// Every page uses apiGet / apiPost / apiPut / apiPatch / apiDelete from here.
// They:
//   1. add the JWT token ("Authorization: Bearer ...") to every request
//   2. turn the JSON response into a JavaScript object
//   3. throw an error with the backend's message if something went wrong
//   4. send the user to the login page if the token has expired (401)
// =====================================================================

// Empty in development (vite.config.js forwards /api to Spring Boot).
// In production you can set VITE_API_URL in a .env file (see .env.example).
const API_URL = import.meta.env.VITE_API_URL || '';

const LOGIN_STORAGE_KEY = 'automationSalesLogin';

// ---------- Saving and reading the login ----------

// Called after a successful login with what POST /api/auth/login returns:
// { accessToken: "eyJ...", tokenType: "Bearer", expiresIn: 3600, user: { ... } }
export function saveLogin(loginResponse) {
  const login = {
    token: loginResponse.accessToken,
    user: loginResponse.user,
    expiresAt: Date.now() + loginResponse.expiresIn * 1000, // expiresIn is in seconds
  };
  localStorage.setItem(LOGIN_STORAGE_KEY, JSON.stringify(login));
}

// Returns the saved login, or null if nobody is logged in or the token has expired.
export function getLogin() {
  const saved = localStorage.getItem(LOGIN_STORAGE_KEY);
  if (!saved) return null;

  const login = JSON.parse(saved);
  if (Date.now() > login.expiresAt) {
    logout();
    return null;
  }
  return login;
}

// { userId, fullName, email, role, salesPersonId, enabled, ... }
export function getCurrentUser() {
  const login = getLogin();
  return login ? login.user : null;
}

export function isAdmin() {
  const user = getCurrentUser();
  return user !== null && user.role === 'ADMIN';
}

// For salespeople this is their SalesPerson id. For admins it is null.
export function mySalesPersonId() {
  const user = getCurrentUser();
  return user ? user.salesPersonId : null;
}

export function logout() {
  localStorage.removeItem(LOGIN_STORAGE_KEY);
}

// ---------- Making requests ----------

async function apiRequest(method, url, body) {
  const headers = { 'Content-Type': 'application/json' };

  const login = getLogin();
  if (login) {
    headers.Authorization = 'Bearer ' + login.token;
  }

  let response;
  try {
    response = await fetch(API_URL + '/api' + url, {
      method: method,
      headers: headers,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch {
    // fetch only throws when the server can't be reached at all
    throw new Error('Cannot reach the server. Is the backend running on port 8080?');
  }

  // 204 No Content (e.g. after DELETE) has no body
  if (response.status === 204) {
    return null;
  }

  let data = null;
  const text = await response.text();
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    // Token expired or account disabled -> log out and go to the login page
    if (response.status === 401 && login) {
      logout();
      window.location.href = '/login?expired=1';
    }

    // Your GlobalExceptionHandler always sends { status, message, fieldErrors }
    const error = new Error(data && data.message ? data.message : 'Request failed (' + response.status + ')');
    error.status = response.status;
    error.fieldErrors = data ? data.fieldErrors : null; // e.g. { email: "email must be valid" }
    throw error;
  }

  return data;
}

export const apiGet = (url) => apiRequest('GET', url);
export const apiPost = (url, body) => apiRequest('POST', url, body);
export const apiPut = (url, body) => apiRequest('PUT', url, body);
export const apiPatch = (url, body) => apiRequest('PATCH', url, body);
export const apiDelete = (url) => apiRequest('DELETE', url);
