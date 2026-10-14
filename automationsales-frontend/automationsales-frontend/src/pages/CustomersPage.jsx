// =====================================================================
// Customers page  ← READ THIS PAGE FIRST, it has the most comments
// =====================================================================
// Every list page in this app follows the same pattern:
//   1. useState holds the data, the search text and which popup is open
//   2. useEffect calls loadData() once when the page opens
//   3. The table shows the rows that match the search
//   4. Clicking a row opens a popup (details or form). Saving calls the API,
//      then loadData() again so the table shows the latest data.
// =====================================================================
import { useEffect, useState } from 'react';
import { apiGet, apiPost, apiPut, apiDelete, isAdmin, mySalesPersonId } from '../api';
import { loadCustomers, loadSalespeople } from '../lists';
import { formatDate, makeNameMap, matchesSearch, prepareForApi, toFormValues, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

export default function CustomersPage() {
  // ---------- 1. State ----------
  const [customers, setCustomers] = useState([]);
  const [salespeople, setSalespeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Which popup is open. null = closed.
  const [viewing, setViewing] = useState(null);   // the customer whose details are shown
  const [editing, setEditing] = useState(null);   // the customer being edited, or {} for a new one

  // ---------- 2. Load the data ----------
  async function loadData() {
    try {
      // Promise.all runs both requests at the same time (faster)
      const [customerList, salespersonList] = await Promise.all([loadCustomers(), loadSalespeople()]);
      setCustomers(customerList);
      setSalespeople(salespersonList);
      setLoadError(null);
    } catch (error) {
      setLoadError(error);
    } finally {
      setLoading(false);
    }
  }

  // The empty [] means: run once, when the page first appears
  useEffect(() => {
    loadData();
  }, []);

  // { 3: "Sara Ali", ... } so we can show owner names instead of ids
  const salespersonNames = makeNameMap(salespeople, 'salesPersonId', 'salesPersonName');

  // ---------- 3. Search and filter ----------
  const visibleCustomers = customers.filter((c) => {
    const text = `${c.customerName} ${c.companyName} ${c.customerEmail} ${c.customerPhone}`;
    return matchesSearch(text, search) && (statusFilter === '' || c.status === statusFilter);
  });

  // ---------- 4. The page ----------
  return (
    <>
      <div className="page-header">
        <div>
          <h1>Customers</h1>
          <p className="muted">Companies that have bought from you</p>
        </div>
        <button className="button button-primary" onClick={() => setEditing({})}>+ New customer</button>
      </div>

      <div className="card">
        <div className="toolbar">
          <input
            type="search"
            className="search"
            placeholder="Search name, company, email…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
            <option value="">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}

        {!loading && !loadError && visibleCustomers.length === 0 && (
          <div className="empty">
            {customers.length === 0 ? 'No customers yet. Click “New customer” to add one.' : 'No customers match your search.'}
          </div>
        )}

        {visibleCustomers.length > 0 && (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Company</th>
                  <th className="hide-mobile">Phone</th>
                  <th className="hide-mobile">Owner</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visibleCustomers.map((c) => (
                  // "key" helps React keep track of each row
                  <tr key={c.customerId} className="clickable" onClick={() => setViewing(c)}>
                    <td>
                      {/* React automatically makes text safe, so a name like <script> is shown as text */}
                      <strong>{c.customerName}</strong>
                      <div className="muted small">{c.customerEmail}</div>
                    </td>
                    <td>{c.companyName}</td>
                    <td className="hide-mobile">{c.customerPhone}</td>
                    <td className="hide-mobile">{salespersonNames[c.salespersonId]}</td>
                    <td><Badge value={c.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="table-count">{visibleCustomers.length} of {customers.length} customers</p>
          </>
        )}
      </div>

      {/* Popups: only shown when "viewing" / "editing" has a value */}
      {viewing && (
        <CustomerDetails
          customer={viewing}
          ownerName={salespersonNames[viewing.salespersonId]}
          onClose={() => setViewing(null)}
          onEdit={() => { setEditing(viewing); setViewing(null); }}
          onDeleted={() => { setViewing(null); loadData(); }}
        />
      )}

      {editing && (
        <CustomerForm
          customer={editing}
          salespeople={salespeople}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

// =====================================================================
// Details popup: customer info + their licences and open tasks
// =====================================================================
function CustomerDetails({ customer, ownerName, onClose, onEdit, onDeleted }) {
  const [licenses, setLicenses] = useState(null); // null = still loading
  const [tasks, setTasks] = useState(null);

  // Load the related records when the popup opens
  useEffect(() => {
    apiGet('/licenses/customer/' + customer.customerId).then(setLicenses).catch(() => setLicenses([]));
    apiGet('/sales-tasks/customer/' + customer.customerId).then(setTasks).catch(() => setTasks([]));
  }, [customer.customerId]);

  async function handleDelete() {
    if (!window.confirm(`Delete ${customer.customerName}? This cannot be undone.`)) return;
    try {
      await apiDelete('/customers/' + customer.customerId);
      showToast('Customer deleted');
      onDeleted();
    } catch (error) {
      // 409 = the customer still has licences/tasks linked to it
      showToast(error.status === 409 ? 'This customer still has licences or tasks. Remove those first.' : error.message, 'error');
    }
  }

  const openTasks = tasks ? tasks.filter((t) => t.taskStatus === 'OPEN' || t.taskStatus === 'IN_PROGRESS') : [];

  return (
    <Modal title={customer.customerName} onClose={onClose} size="modal-large">
      <dl className="details">
        <dt>Company</dt><dd>{customer.companyName}</dd>
        <dt>Email</dt><dd><a href={'mailto:' + customer.customerEmail}>{customer.customerEmail}</a></dd>
        <dt>Phone</dt><dd>{customer.customerPhone || '—'}</dd>
        <dt>Status</dt><dd><Badge value={customer.status} /></dd>
        <dt>Owner</dt><dd>{ownerName || '—'}</dd>
        <dt>Customer since</dt><dd>{formatDate(customer.createdAt)}</dd>
      </dl>

      <h3 className="section-title">Licences</h3>
      {licenses === null && <p className="muted">Loading…</p>}
      {licenses !== null && licenses.length === 0 && <p className="muted">No licences yet.</p>}
      {licenses !== null && licenses.length > 0 && (
        <ul className="mini-list">
          {licenses.map((l) => (
            <li key={l.licenseId}>
              <span>Licence #{l.licenseId} · {l.licenseQuantity} seats · expires {formatDate(l.expiryDate)}</span>
              <Badge value={l.status} />
            </li>
          ))}
        </ul>
      )}

      <h3 className="section-title">Open tasks</h3>
      {tasks === null && <p className="muted">Loading…</p>}
      {tasks !== null && openTasks.length === 0 && <p className="muted">No open tasks.</p>}
      {openTasks.length > 0 && (
        <ul className="mini-list">
          {openTasks.map((t) => (
            <li key={t.salesTaskId}>
              <span>{t.title} · due {formatDate(t.dueDate) || 'no date'}</span>
              <Badge value={t.priority} />
            </li>
          ))}
        </ul>
      )}

      <div className="form-actions">
        {isAdmin() && <button className="button button-danger-light" onClick={handleDelete}>Delete</button>}
        <button className="button button-primary" onClick={onEdit}>Edit customer</button>
      </div>
    </Modal>
  );
}

// =====================================================================
// Create / edit form. customer = {} for a new customer.
// =====================================================================
function CustomerForm({ customer, salespeople, onClose, onSaved }) {
  const isNew = !customer.customerId;

  // All form fields live in one state object.
  // toFormValues turns null into '' because inputs can't show null.
  const [form, setForm] = useState(toFormValues(customer, {
    customerName: '', companyName: '', customerEmail: '', customerPhone: '', status: 'ACTIVE',
    // A salesperson's customers always belong to them
    salespersonId: isAdmin() ? '' : mySalesPersonId(),
  }));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  // One change handler for every input: uses the input's "name" to know which field changed
  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault(); // stop the browser from reloading the page
    setSaving(true);
    setError(null);

    // Send only the fields the CustomerRequestDTO expects
    const data = prepareForApi({
      customerName: form.customerName,
      companyName: form.companyName,
      customerEmail: form.customerEmail,
      customerPhone: form.customerPhone,
      status: form.status,
      salespersonId: form.salespersonId,
    }, ['salespersonId']);

    try {
      if (isNew) {
        await apiPost('/customers', data);
        showToast('Customer created');
      } else {
        await apiPut('/customers/' + customer.customerId, data);
        showToast('Customer updated');
      }
      onSaved();
    } catch (err) {
      setError(err); // shows the message and the field errors from the backend
      setSaving(false);
    }
  }

  // The backend's error for one field, e.g. fieldError('customerEmail')
  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New customer' : 'Edit customer'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Contact name *" error={fieldError('customerName')}>
            <input name="customerName" value={form.customerName} onChange={handleChange} required maxLength={100} autoFocus />
          </Field>
          <Field label="Company *" error={fieldError('companyName')}>
            <input name="companyName" value={form.companyName} onChange={handleChange} required maxLength={150} />
          </Field>
          <Field label="Email *" error={fieldError('customerEmail')}>
            <input name="customerEmail" type="email" value={form.customerEmail} onChange={handleChange} required />
          </Field>
          <Field label="Phone" error={fieldError('customerPhone')}>
            <input name="customerPhone" type="tel" value={form.customerPhone} onChange={handleChange} placeholder="+971 50 000 0000" />
          </Field>
          <Field label="Status">
            <select name="status" value={form.status} onChange={handleChange}>
              <EnumOptions values={['ACTIVE', 'INACTIVE']} />
            </select>
          </Field>

          {/* Only admins choose the owner */}
          {isAdmin() && (
            <Field label="Owner (salesperson)">
              <select name="salespersonId" value={form.salespersonId} onChange={handleChange}>
                <option value="">— None —</option>
                {salespeople.map((s) => (
                  <option key={s.salesPersonId} value={s.salesPersonId}>{s.salesPersonName}</option>
                ))}
              </select>
            </Field>
          )}
        </div>

        <div className="form-actions">
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create customer' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
