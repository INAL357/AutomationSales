// =====================================================================
// Knowledge base — guides, pricing sheets and FAQs for the team
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiDelete, isAdmin } from '../api';
import { loadDocuments, loadProducts } from '../lists';
import { formatDate, niceLabel, makeNameMap, matchesSearch, isSafeLink, prepareForApi, toFormValues, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const TYPES = ['GUIDE', 'FAQ', 'PRICING', 'DATASHEET', 'CONTRACT', 'PLAYBOOK', 'OTHER'];

export default function DocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [reading, setReading] = useState(null); // document being read
  const [editing, setEditing] = useState(null); // document being edited, {} for new

  async function loadData() {
    try {
      const [documentList, productList] = await Promise.all([loadDocuments(), loadProducts()]);
      setDocuments(documentList);
      setProducts(productList);
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

  const productNames = makeNameMap(products, 'productId', 'productName');

  // Salespeople only see active documents
  const visible = documents.filter((d) => (isAdmin() || d.active)
    && matchesSearch(`${d.title} ${d.description} ${d.content}`, search)
    && (typeFilter === '' || d.type === typeFilter));

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Knowledge base</h1>
          <p className="muted">Answers and material your team needs while selling</p>
        </div>
        {isAdmin() && <button className="button button-primary" onClick={() => setEditing({})}>+ New document</button>}
      </div>

      <div className="card">
        <div className="toolbar">
          <input type="search" className="search" placeholder="Search title or content…" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <EnumOptions values={TYPES} emptyText="All types" />
          </select>
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}
        {!loading && !loadError && visible.length === 0 && <div className="empty">No documents found.</div>}

        {visible.length > 0 && (
          <table className="table">
            <thead>
              <tr><th>Document</th><th>Type</th><th className="hide-mobile">Product</th><th className="hide-mobile">Updated</th><th></th></tr>
            </thead>
            <tbody>
              {visible.map((d) => (
                <tr key={d.documentId} className="clickable" onClick={() => setReading(d)}>
                  <td><strong>{d.title}</strong><div className="muted small">{d.description}</div></td>
                  <td>{niceLabel(d.type)}</td>
                  <td className="hide-mobile">{productNames[d.productId]}</td>
                  <td className="hide-mobile">{formatDate(d.modifiedAt)}</td>
                  <td>{!d.active && <Badge value="INACTIVE" />}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Read-only view for everyone */}
      {reading && (
        <Modal title={reading.title} onClose={() => setReading(null)} size="modal-large">
          <p className="muted">
            {niceLabel(reading.type)}{reading.productId ? ' · ' + (productNames[reading.productId] || '') : ''}
          </p>
          {reading.description && <p><strong>{reading.description}</strong></p>}
          {reading.content && <div className="document-content">{reading.content}</div>}
          {/* Only show real web links, never "javascript:" links */}
          {isSafeLink(reading.documentUrl) && (
            <p><a href={reading.documentUrl} target="_blank" rel="noopener noreferrer">Open the full document ↗</a></p>
          )}
          <div className="form-actions">
            {isAdmin()
              ? <button className="button button-primary" onClick={() => { setEditing(reading); setReading(null); }}>Edit</button>
              : <button className="button button-light" onClick={() => setReading(null)}>Close</button>}
          </div>
        </Modal>
      )}

      {editing && (
        <DocumentForm
          doc={editing}
          products={products}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

function DocumentForm({ doc, products, onClose, onSaved }) {
  const isNew = !doc.documentId;
  const [form, setForm] = useState(toFormValues(doc, {
    title: '', description: '', content: '', productId: '', softwareLicenseId: '', type: 'GUIDE', documentUrl: '', active: true,
  }));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function handleChange(event) {
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
    setForm({ ...form, [event.target.name]: value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    if (form.documentUrl && !isSafeLink(form.documentUrl)) {
      setError(new Error('The link must start with https:// or http://'));
      return;
    }

    setSaving(true);
    const data = prepareForApi({
      title: form.title, description: form.description, content: form.content, productId: form.productId,
      softwareLicenseId: form.softwareLicenseId, type: form.type, documentUrl: form.documentUrl, active: form.active,
    }, ['productId', 'softwareLicenseId']);

    try {
      if (isNew) {
        await apiPost('/knowledge-documents', data);
        showToast('Document created');
      } else {
        await apiPut('/knowledge-documents/' + doc.documentId, data);
        showToast('Document updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete "${doc.title}"?`)) return;
    try {
      await apiDelete('/knowledge-documents/' + doc.documentId);
      showToast('Document deleted');
      onSaved();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New document' : 'Edit document'} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Title *" error={fieldError('title')} fullWidth>
            <input name="title" value={form.title} onChange={handleChange} required maxLength={200} autoFocus />
          </Field>
          <Field label="Type">
            <select name="type" value={form.type} onChange={handleChange}><EnumOptions values={TYPES} /></select>
          </Field>
          <Field label="Product">
            <select name="productId" value={form.productId} onChange={handleChange}>
              <option value="">— None —</option>
              {products.map((p) => <option key={p.productId} value={p.productId}>{p.productName}</option>)}
            </select>
          </Field>
          <Field label="Link (optional)" error={fieldError('documentUrl')} fullWidth>
            <input name="documentUrl" type="url" maxLength={500} value={form.documentUrl} onChange={handleChange} placeholder="https://…" />
          </Field>
          <Field label="Short description" error={fieldError('description')} fullWidth>
            <input name="description" maxLength={1000} value={form.description} onChange={handleChange} />
          </Field>
          <Field label="Content" fullWidth>
            <textarea name="content" rows={8} value={form.content} onChange={handleChange} />
          </Field>
          <label className="checkbox-label full-width">
            <input type="checkbox" name="active" checked={form.active} onChange={handleChange} /> Visible to the team
          </label>
        </div>
        <div className="form-actions">
          {!isNew && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create document' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
