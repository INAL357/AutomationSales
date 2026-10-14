// =====================================================================
// Licences page — who owns which product, and when it expires
// =====================================================================
// The tabs use different endpoints:
//   All       -> GET /api/licenses (salespeople: /licenses/salesperson/{id})
//   Expiring  -> GET /api/licenses/expiring?days=30
//   Expired   -> GET /api/licenses/expired
// The backend works out "daysRemaining" and "status" for us.
// =====================================================================
import { useEffect, useState } from 'react';
import { apiGet, apiPost, apiPut, apiDelete, isAdmin, mySalesPersonId } from '../api';
import { loadLicenses, loadCustomers, loadProducts, loadSalespeople } from '../lists';
import { formatDate, makeNameMap, matchesSearch, prepareForApi, toFormValues, toDateTimeInput, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const STATUSES = ['ACTIVE', 'EXPIRING_SOON', 'EXPIRED', 'RENEWED'];

const TABS = [
  { id: 'all', label: 'All licences' },
  { id: 'expiring', label: 'Expiring in 30 days' },
  { id: 'expired', label: 'Expired' },
];

// A small coloured bar showing how much time is left
function TimeLeft({ days }) {
  if (days === null || days === undefined) return null;

  let colour = 'green';
  if (days <= 30) colour = 'red';
  else if (days <= 90) colour = 'amber';

  let text = days + ' days';
  if (days < 0) text = 'Expired ' + -days + ' days ago';
  if (days === 0) text = 'Expires today';

  const width = Math.max(4, Math.min(100, (Math.max(days, 0) / 365) * 100));
  return (
    <div className="time-left">
      <div className="time-bar"><span className={'bar-' + colour} style={{ width: width + '%' }} /></div>
      <span className="small">{text}</span>
    </div>
  );
}

export default function LicensesPage() {
  const [licenses, setLicenses] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [salespeople, setSalespeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [tab, setTab] = useState('all');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [editing, setEditing] = useState(null);

  // Customers, products and salespeople: load once
  useEffect(() => {
    Promise.all([loadCustomers(), loadProducts(), loadSalespeople()])
      .then(([customerList, productList, salespersonList]) => {
        setCustomers(customerList);
        setProducts(productList);
        setSalespeople(salespersonList);
      })
      .catch(setLoadError);
  }, []);

  async function loadLicenseList() {
    setLoading(true);
    try {
      let list;
      if (isAdmin()) {
        // Admins: the backend has ready-made endpoints for these tabs
        if (tab === 'expiring') list = await apiGet('/licenses/expiring?days=30');
        else if (tab === 'expired') list = await apiGet('/licenses/expired');
        else list = await loadLicenses();
      } else {
        // Salespeople: /expiring and /expired are admin-only (they return everyone's licences),
        // so we load OUR licences and pick the right ones here
        list = await loadLicenses();
        if (tab === 'expiring') list = list.filter((l) => l.daysRemaining !== null && l.daysRemaining >= 0 && l.daysRemaining <= 30);
        if (tab === 'expired') list = list.filter((l) => l.daysRemaining !== null && l.daysRemaining < 0);
      }
      // Soonest to expire first
      list.sort((a, b) => (a.daysRemaining ?? 99999) - (b.daysRemaining ?? 99999));
      setLicenses(list);
      setLoadError(null);
    } catch (error) {
      setLoadError(error);
    } finally {
      setLoading(false);
    }
  }

  // Runs when the page opens AND every time the tab changes
  useEffect(() => {
    loadLicenseList();
  }, [tab]);

  const customerNames = makeNameMap(customers, 'customerId', 'customerName');
  const productNames = makeNameMap(products, 'productId', 'productName');

  const visibleLicenses = licenses.filter((l) =>
    matchesSearch(`${l.licenseId} ${customerNames[l.customerId]} ${productNames[l.productId]}`, search)
    && (statusFilter === '' || l.status === statusFilter));

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Licences</h1>
          <p className="muted">Track every licence until it is renewed</p>
        </div>
        <button className="button button-primary" onClick={() => setEditing({})}>+ New licence</button>
      </div>

      <div className="tabs">
        {TABS.map((t) => (
          <button key={t.id} className={tab === t.id ? 'tab active' : 'tab'} onClick={() => setTab(t.id)}>{t.label}</button>
        ))}
      </div>

      <div className="card">
        <div className="toolbar">
          <input type="search" className="search" placeholder="Search customer or product…" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <EnumOptions values={STATUSES} emptyText="All statuses" />
          </select>
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}
        {!loading && !loadError && visibleLicenses.length === 0 && <div className="empty">No licences here.</div>}

        {!loading && visibleLicenses.length > 0 && (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>Licence</th><th>Customer</th><th>Product</th><th className="hide-mobile">Seats</th>
                  <th className="hide-mobile">Expires</th><th>Time left</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visibleLicenses.map((l) => (
                  <tr key={l.licenseId} className="clickable" onClick={() => setEditing(l)}>
                    <td><strong>#{l.licenseId}</strong></td>
                    <td>{customerNames[l.customerId] || 'Customer #' + l.customerId}</td>
                    <td>{productNames[l.productId] || 'Product #' + l.productId}</td>
                    <td className="hide-mobile">{l.licenseQuantity}</td>
                    <td className="hide-mobile">{formatDate(l.expiryDate)}</td>
                    <td><TimeLeft days={l.daysRemaining} /></td>
                    <td><Badge value={l.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="table-count">{visibleLicenses.length} licences</p>
          </>
        )}
      </div>

      {editing && (
        <LicenseForm
          license={editing}
          customers={customers}
          products={products}
          salespeople={salespeople}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadLicenseList(); }}
        />
      )}
    </>
  );
}

function LicenseForm({ license, customers, products, salespeople, onClose, onSaved }) {
  const isNew = !license.licenseId;
  const [form, setForm] = useState(toFormValues({
    ...license,
    purchaseDate: toDateTimeInput(license.purchaseDate),
    expiryDate: toDateTimeInput(license.expiryDate),
  }, {
    customerId: '', productId: '', licenseQuantity: '', durationMonths: '', purchaseDate: '', expiryDate: '',
    salespersonId: isAdmin() ? '' : mySalesPersonId(),
  }));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    // Same rules as the backend
    if (!form.durationMonths && !form.expiryDate) {
      setError(new Error('Enter a term in months or an expiry date.'));
      return;
    }
    if (form.expiryDate && form.expiryDate <= form.purchaseDate) {
      setError(new Error('The expiry date must be after the purchase date.'));
      return;
    }

    setSaving(true);
    const data = prepareForApi({
      productId: form.productId, customerId: form.customerId, salespersonId: form.salespersonId,
      licenseQuantity: form.licenseQuantity, durationMonths: form.durationMonths,
      purchaseDate: form.purchaseDate, expiryDate: form.expiryDate,
    }, ['productId', 'customerId', 'salespersonId', 'licenseQuantity', 'durationMonths'], ['purchaseDate', 'expiryDate']);

    try {
      if (isNew) {
        await apiPost('/licenses', data);
        showToast('Licence created');
      } else {
        await apiPut('/licenses/' + license.licenseId, data);
        showToast('Licence updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('Delete licence #' + license.licenseId + '?')) return;
    try {
      await apiDelete('/licenses/' + license.licenseId);
      showToast('Licence deleted');
      onSaved();
    } catch (err) {
      showToast(err.status === 409 ? 'This licence still has renewals or tasks linked to it.' : err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New licence' : 'Licence #' + license.licenseId} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Customer *" error={fieldError('customerId')}>
            <select name="customerId" value={form.customerId} onChange={handleChange} required>
              <option value="">Select…</option>
              {customers.map((c) => <option key={c.customerId} value={c.customerId}>{c.customerName} · {c.companyName}</option>)}
            </select>
          </Field>
          <Field label="Product *" error={fieldError('productId')}>
            <select name="productId" value={form.productId} onChange={handleChange} required>
              <option value="">Select…</option>
              {products.map((p) => <option key={p.productId} value={p.productId}>{p.productName}</option>)}
            </select>
          </Field>
          <Field label="Seats / quantity *" error={fieldError('licenseQuantity')}>
            <input name="licenseQuantity" type="number" min={1} value={form.licenseQuantity} onChange={handleChange} required />
          </Field>
          <Field label="Term (months)" error={fieldError('durationMonths')}>
            <input name="durationMonths" type="number" min={1} value={form.durationMonths} onChange={handleChange} />
          </Field>
          <Field label="Purchase date *" error={fieldError('purchaseDate')}>
            <input name="purchaseDate" type="datetime-local" value={form.purchaseDate} onChange={handleChange} required />
          </Field>
          <Field label="Expiry date" error={fieldError('expiryDate')}>
            <input name="expiryDate" type="datetime-local" value={form.expiryDate} onChange={handleChange} />
          </Field>
          {isAdmin() && (
            <Field label="Salesperson">
              <select name="salespersonId" value={form.salespersonId} onChange={handleChange}>
                <option value="">— None —</option>
                {salespeople.map((s) => <option key={s.salesPersonId} value={s.salesPersonId}>{s.salesPersonName}</option>)}
              </select>
            </Field>
          )}
        </div>
        <p className="muted small">Give a term <strong>or</strong> an expiry date. With a term, the backend works out the expiry date.</p>

        <div className="form-actions">
          {!isNew && isAdmin() && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create licence' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
