// =====================================================================
// Vendors page — companies that make the software you sell
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiDelete, isAdmin } from '../api';
import { loadVendors } from '../lists';
import { formatDate, matchesSearch, prepareForApi, toFormValues, showToast } from '../utils';
import Modal from '../components/Modal';
import { Field, FormError, Loading, ErrorBox } from '../components/FormParts';

export default function VendorsPage() {
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [search, setSearch] = useState('');
  const [editing, setEditing] = useState(null);

  async function loadData() {
    try {
      setVendors(await loadVendors());
      setLoadError(null);
    } catch (error) {
      setLoadError(error);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  const visible = vendors.filter((v) => matchesSearch(`${v.vendorName} ${v.vendorEmail} ${v.vendorPhone}`, search));

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Vendors</h1>
          <p className="muted">Software makers whose products you sell</p>
        </div>
        {isAdmin() && <button className="button button-primary" onClick={() => setEditing({})}>+ New vendor</button>}
      </div>

      <div className="card">
        <div className="toolbar">
          <input type="search" className="search" placeholder="Search vendors…" value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}
        {!loading && !loadError && visible.length === 0 && <div className="empty">No vendors found.</div>}

        {visible.length > 0 && (
          <table className="table">
            <thead>
              <tr><th>Vendor</th><th>Email</th><th className="hide-mobile">Phone</th><th className="hide-mobile">Added</th></tr>
            </thead>
            <tbody>
              {visible.map((v) => (
                <tr key={v.vendorId} className={isAdmin() ? 'clickable' : ''} onClick={isAdmin() ? () => setEditing(v) : undefined}>
                  <td><strong>{v.vendorName}</strong><div className="muted small">{v.vendorDescription}</div></td>
                  <td>{v.vendorEmail && <a href={'mailto:' + v.vendorEmail} onClick={(e) => e.stopPropagation()}>{v.vendorEmail}</a>}</td>
                  <td className="hide-mobile">{v.vendorPhone}</td>
                  <td className="hide-mobile">{formatDate(v.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {editing && (
        <VendorForm vendor={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); loadData(); }} />
      )}
    </>
  );
}

function VendorForm({ vendor, onClose, onSaved }) {
  const isNew = !vendor.vendorId;
  const [form, setForm] = useState(toFormValues(vendor, { vendorName: '', vendorEmail: '', vendorPhone: '', vendorDescription: '' }));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    const data = prepareForApi({
      vendorName: form.vendorName, vendorEmail: form.vendorEmail, vendorPhone: form.vendorPhone, vendorDescription: form.vendorDescription,
    });
    try {
      if (isNew) {
        await apiPost('/vendors', data);
        showToast('Vendor created');
      } else {
        await apiPut('/vendors/' + vendor.vendorId, data);
        showToast('Vendor updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete ${vendor.vendorName}?`)) return;
    try {
      await apiDelete('/vendors/' + vendor.vendorId);
      showToast('Vendor deleted');
      onSaved();
    } catch (err) {
      showToast(err.status === 409 ? 'This vendor still has products. Delete or move those first.' : err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New vendor' : 'Edit vendor'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Vendor name *" error={fieldError('vendorName')}>
            <input name="vendorName" value={form.vendorName} onChange={handleChange} required maxLength={150} autoFocus />
          </Field>
          <Field label="Email" error={fieldError('vendorEmail')}>
            <input name="vendorEmail" type="email" value={form.vendorEmail} onChange={handleChange} />
          </Field>
          <Field label="Phone" error={fieldError('vendorPhone')}>
            <input name="vendorPhone" type="tel" value={form.vendorPhone} onChange={handleChange} />
          </Field>
          <Field label="Description" error={fieldError('vendorDescription')} fullWidth>
            <textarea name="vendorDescription" rows={3} maxLength={1000} value={form.vendorDescription} onChange={handleChange} />
          </Field>
        </div>
        <div className="form-actions">
          {!isNew && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create vendor' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
