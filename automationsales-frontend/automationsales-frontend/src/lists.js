// =====================================================================
// lists.js — loads the lists pages need (for tables, dropdowns and names)
// =====================================================================
// Salespeople should only see THEIR OWN records, so for them we call the
// ".../salesperson/{id}" endpoints. Admins get everything.
// =====================================================================
import { apiGet, isAdmin, mySalesPersonId, getCurrentUser } from './api';

export function loadCustomers() {
  return isAdmin() ? apiGet('/customers') : apiGet('/customers/salesperson/' + mySalesPersonId());
}

export function loadLeads() {
  return isAdmin() ? apiGet('/leads') : apiGet('/leads/salesperson/' + mySalesPersonId());
}

export function loadLicenses() {
  return isAdmin() ? apiGet('/licenses') : apiGet('/licenses/salesperson/' + mySalesPersonId());
}

export function loadTasks() {
  return isAdmin() ? apiGet('/sales-tasks') : apiGet('/sales-tasks/salesperson/' + mySalesPersonId());
}

export function loadCalls() {
  return isAdmin() ? apiGet('/sales-calls') : apiGet('/sales-calls/salesperson/' + mySalesPersonId());
}

export function loadNotifications() {
  return isAdmin() ? apiGet('/notifications') : apiGet('/notifications/salesperson/' + mySalesPersonId());
}

// There is no "/renewals/salesperson/{id}" endpoint yet, so everyone gets all renewals.
export function loadRenewals() {
  return isAdmin() ? apiGet('/renewals') : apiGet('/renewals/salesperson/' + mySalesPersonId());
}

export function loadProducts() {
  return apiGet('/products');
}

export function loadVendors() {
  return apiGet('/vendors');
}

export function loadDocuments() {
  return apiGet('/knowledge-documents');
}

// GET /api/salespersons is ADMIN-only. A salesperson just gets themselves.
export async function loadSalespeople() {
  if (isAdmin()) {
    return apiGet('/salespersons');
  }
  const me = getCurrentUser();
  return [{ salesPersonId: me.salesPersonId, salesPersonName: me.fullName + ' (you)' }];
}
