// =====================================================================
// Products page — your software catalogue, shown as cards
// =====================================================================
// Everyone can see products. Only admins can add or edit them.
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiDelete, isAdmin } from '../api';
import { loadProducts, loadVendors } from '../lists';
import { formatMoney, niceLabel, makeNameMap, matchesSearch, prepareForApi, toFormValues, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const PLATFORMS = ['CLOUD', 'WINDOWS', 'MAC', 'LINUX', 'MOBILE', 'CROSS_PLATFORM'];

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [showInactive, setShowInactive] = useState(false);
  const [editing, setEditing] = useState(null);

  async function loadData() {
    try {
      const [productList, vendorList] = await Promise.all([loadProducts(), loadVendors()]);
      setProducts(productList);
      setVendors(vendorList);
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

  const vendorNames = makeNameMap(vendors, 'vendorId', 'vendorName');

  const visible = products.filter((p) =>
    matchesSearch(`${p.productName} ${p.category} ${p.version} ${p.platform} ${vendorNames[p.vendorId]}`, search)
    && (showInactive || p.active));

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Products</h1>
          <p className="muted">Your software catalogue</p>
        </div>
        {isAdmin() && <button className="button button-primary" onClick={() => setEditing({})}>+ New product</button>}
      </div>

      <div className="toolbar toolbar-plain">
        <input type="search" className="search" placeholder="Search name, category, vendor…" value={search} onChange={(e) => setSearch(e.target.value)} />
        <label className="checkbox-label">
          <input type="checkbox" checked={showInactive} onChange={(e) => setShowInactive(e.target.checked)} /> Show inactive
        </label>
      </div>

      {loading && <Loading />}
      {loadError && <ErrorBox error={loadError} />}
      {!loading && !loadError && visible.length === 0 && <div className="card"><div className="empty">No products found.</div></div>}

      <div className="product-grid">
        {visible.map((p) => (
          <article
            key={p.productId}
            className={isAdmin() ? 'product-card clickable' : 'product-card'}
            onClick={isAdmin() ? () => setEditing(p) : undefined}
          >
            <div className="product-card-top">
              <div className="product-initial">{p.productName.charAt(0).toUpperCase()}</div>
              {!p.active && <Badge value="INACTIVE" />}
            </div>
            <h3>{p.productName} {p.version && <span className="muted">{p.version}</span>}</h3>
            <p className="muted small">{vendorNames[p.vendorId]}{p.category ? ' · ' + p.category : ''}</p>
            <p className="product-description">{p.productDescription}</p>
            <div className="product-card-bottom">
              <span className="price">{p.productPrice !== null && p.productPrice !== undefined ? formatMoney(p.productPrice) : 'Price on request'}</span>
              <span className="muted small">{niceLabel(p.platform)}</span>
            </div>
          </article>
        ))}
      </div>

      {editing && (
        <ProductForm
          product={editing}
          vendors={vendors}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

function ProductForm({ product, vendors, onClose, onSaved }) {
  const isNew = !product.productId;
  const [form, setForm] = useState(toFormValues(product, {
    vendorId: '', productName: '', productDescription: '', productPrice: '', category: '', version: '', platform: '', active: true,
  }));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function handleChange(event) {
    // Checkboxes use "checked", every other input uses "value"
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
    setForm({ ...form, [event.target.name]: value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);

    const data = prepareForApi({
      vendorId: form.vendorId, productName: form.productName, productDescription: form.productDescription,
      productPrice: form.productPrice, category: form.category, version: form.version, platform: form.platform,
      active: form.active,
    }, ['vendorId', 'productPrice']);

    try {
      if (isNew) {
        await apiPost('/products', data);
        showToast('Product created');
      } else {
        await apiPut('/products/' + product.productId, data);
        showToast('Product updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete ${product.productName}?`)) return;
    try {
      await apiDelete('/products/' + product.productId);
      showToast('Product deleted');
      onSaved();
    } catch (err) {
      showToast(err.status === 409 ? 'Licences or leads still use this product. Mark it inactive instead.' : err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New product' : 'Edit product'} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        {vendors.length === 0 && <p className="warning-box">Add a vendor first (Vendors page). Every product needs one.</p>}
        <div className="form-grid">
          <Field label="Product name *" error={fieldError('productName')}>
            <input name="productName" value={form.productName} onChange={handleChange} required maxLength={150} autoFocus />
          </Field>
          <Field label="Vendor *" error={fieldError('vendorId')}>
            <select name="vendorId" value={form.vendorId} onChange={handleChange} required>
              <option value="">Select…</option>
              {vendors.map((v) => <option key={v.vendorId} value={v.vendorId}>{v.vendorName}</option>)}
            </select>
          </Field>
          <Field label="Price (AED)" error={fieldError('productPrice')}>
            <input name="productPrice" type="number" min={0} step="0.01" value={form.productPrice} onChange={handleChange} />
          </Field>
          <Field label="Category">
            <input name="category" value={form.category} onChange={handleChange} placeholder="e.g. CRM, Security" />
          </Field>
          <Field label="Version">
            <input name="version" value={form.version} onChange={handleChange} placeholder="e.g. 2026" />
          </Field>
          <Field label="Platform">
            <select name="platform" value={form.platform} onChange={handleChange}><EnumOptions values={PLATFORMS} emptyText="— None —" /></select>
          </Field>
          <Field label="Description" error={fieldError('productDescription')} fullWidth>
            <textarea name="productDescription" rows={4} maxLength={2000} value={form.productDescription} onChange={handleChange} />
          </Field>
          <label className="checkbox-label full-width">
            <input type="checkbox" name="active" checked={form.active} onChange={handleChange} /> Available for sale
          </label>
        </div>

        <div className="form-actions">
          {!isNew && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create product' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
