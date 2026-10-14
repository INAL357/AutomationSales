// =====================================================================
// utils.js — small helper functions (formatting, forms, lookups)
// =====================================================================

// "IN_PROGRESS" -> "In progress"
export function niceLabel(value) {
  if (!value) return '';
  const text = String(value).replace(/_/g, ' ').toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

// "2026-10-09T14:30:00" -> "9 Oct 2026"
export function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

// "2026-10-09T14:30:00" -> "9 Oct 2026, 14:30"
export function formatDateTime(value) {
  if (!value) return '';
  return new Date(value).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

// 1499.5 -> "AED 1,499.50" (change the currency here if you need to)
export function formatMoney(value) {
  if (value === null || value === undefined) return '';
  return 'AED ' + Number(value).toLocaleString('en-GB', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Days from today until a date (negative = in the past)
export function daysFromToday(value) {
  if (!value) return null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const date = new Date(value);
  date.setHours(0, 0, 0, 0);
  return Math.round((date - today) / (1000 * 60 * 60 * 24));
}

// Today as "2026-10-09" (the format <input type="date"> uses)
export function todayForInput() {
  return new Date().toISOString().slice(0, 10);
}

// "2026-10-09T14:30:00" -> "2026-10-09T14:30" (what <input type="datetime-local"> needs)
export function toDateTimeInput(value) {
  return value ? String(value).slice(0, 16) : '';
}

// Turns a list into an object for quick lookups:
// makeNameMap(customers, 'customerId', 'customerName') -> { 1: "Ahmed", 2: "Sara" }
export function makeNameMap(list, idField, nameField) {
  const map = {};
  for (const item of list) {
    map[item[idField]] = item[nameField];
  }
  return map;
}

// True if every word typed in the search box appears in "text"
export function matchesSearch(text, searchText) {
  if (!searchText) return true;
  const haystack = String(text).toLowerCase();
  return searchText.toLowerCase().trim().split(' ').every((word) => haystack.includes(word));
}

// Only real web links are allowed (stops "javascript:..." links from running code)
export function isSafeLink(url) {
  return typeof url === 'string' && (url.startsWith('https://') || url.startsWith('http://'));
}

// Converts form state into what the backend expects:
//   - empty strings become null
//   - fields listed in numberFields become numbers
//   - datetime-local values "2026-10-09T14:30" get ":00" seconds added
//     (Spring's LocalDateTime wants "2026-10-09T14:30:00")
export function prepareForApi(form, numberFields = [], dateTimeFields = []) {
  const result = {};
  for (const key in form) {
    let value = form[key];
    if (typeof value === 'string') value = value.trim();

    if (value === '' || value === undefined) {
      result[key] = null;
    } else if (numberFields.includes(key)) {
      result[key] = Number(value);
    } else if (dateTimeFields.includes(key) && value.length === 16) {
      result[key] = value + ':00';
    } else {
      result[key] = value;
    }
  }
  return result;
}

// Replaces null values with '' so React inputs are happy (they don't accept null)
export function toFormValues(record, defaults = {}) {
  const values = { ...defaults };
  for (const key in record) {
    values[key] = record[key] === null || record[key] === undefined ? (defaults[key] ?? '') : record[key];
  }
  return values;
}

// Shows a small message in the bottom-right corner for 4 seconds.
// (It adds a plain <div> to the page, so any file can call it without extra React setup.)
export function showToast(message, type = 'success') {
  const toast = document.createElement('div');
  toast.className = 'toast toast-' + type;
  toast.textContent = message;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}
