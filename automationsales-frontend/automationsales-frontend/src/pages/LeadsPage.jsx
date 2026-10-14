// =====================================================================
// Leads page — a pipeline board with one column per stage, or a list
// =====================================================================
// Changing the dropdown on a card moves the lead to another stage:
//   PATCH /api/leads/{id}/status   body: { "status": "QUALIFIED" }
// Clicking a card opens the edit form.
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiPatch, apiDelete, isAdmin, mySalesPersonId } from '../api';
import { loadLeads, loadProducts, loadSalespeople } from '../lists';
import { formatDate, niceLabel, makeNameMap, matchesSearch, prepareForApi, toFormValues, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

// Same values as the LeadStatus enum in Java, in pipeline order
const STAGES = ['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED', 'UNQUALIFIED', 'LOST'];
const SOURCES = ['WEBSITE', 'REFERRAL', 'EVENT', 'COLD_CALL', 'PARTNER', 'SOCIAL', 'OTHER'];
const TIMELINES = ['IMMEDIATE', 'WITHIN_1_MONTH', 'WITHIN_3_MONTHS', 'WITHIN_6_MONTHS', 'LATER', 'UNKNOWN'];

export default function LeadsPage() {
  const [leads, setLeads] = useState([]);
  const [products, setProducts] = useState([]);
  const [salespeople, setSalespeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [view, setView] = useState('board'); // 'board' or 'list'
  const [editing, setEditing] = useState(null); // lead being edited, {} for new

  async function loadData() {
    try {
      const [leadList, productList, salespersonList] = await Promise.all([loadLeads(), loadProducts(), loadSalespeople()]);
      setLeads(leadList);
      setProducts(productList);
      setSalespeople(salespersonList);
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
  const salespersonNames = makeNameMap(salespeople, 'salesPersonId', 'salesPersonName');

  const visibleLeads = leads.filter((l) => matchesSearch(`${l.leadName} ${l.companyName} ${l.email} ${l.phone} ${l.source}`, search));

  async function changeStage(lead, newStatus) {
    try {
      const updated = await apiPatch('/leads/' + lead.leadId + '/status', { status: newStatus });
      // Swap the old lead for the updated one in our list
      setLeads(leads.map((l) => (l.leadId === lead.leadId ? updated : l)));
      showToast('Moved to ' + niceLabel(newStatus));
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Leads</h1>
          <p className="muted">Move leads through the pipeline until they convert</p>
        </div>
        <button className="button button-primary" onClick={() => setEditing({})}>+ New lead</button>
      </div>

      <div className="toolbar toolbar-plain">
        <input type="search" className="search" placeholder="Search name, company, email…"
          value={search} onChange={(event) => setSearch(event.target.value)} />
        <div className="switch-buttons">
          <button className={view === 'board' ? 'switch-button active' : 'switch-button'} onClick={() => setView('board')}>Board</button>
          <button className={view === 'list' ? 'switch-button active' : 'switch-button'} onClick={() => setView('list')}>List</button>
        </div>
      </div>

      {loading && <Loading />}
      {loadError && <ErrorBox error={loadError} />}

      {/* ---------- Board view ---------- */}
      {!loading && !loadError && view === 'board' && (
        <div className="board">
          {STAGES.map((stage) => {
            const stageLeads = visibleLeads.filter((l) => l.status === stage);
            return (
              <section className="board-column" key={stage}>
                <header className="board-column-header">
                  <Badge value={stage} />
                  <span className="muted">{stageLeads.length}</span>
                </header>

                <div className="board-cards">
                  {stageLeads.length === 0 && <p className="muted small empty-column">No leads</p>}

                  {stageLeads.map((lead) => (
                    <article className="lead-card" key={lead.leadId} onClick={() => setEditing(lead)}>
                      <div className="lead-card-top">
                        <strong>{lead.leadName}</strong>
                        {lead.leadScore !== null && <span className="score" title="Lead score">{lead.leadScore}</span>}
                      </div>
                      <div className="muted small">{lead.companyName || lead.email}</div>
                      {lead.productId && <div className="small">{productNames[lead.productId]}</div>}

                      <div className="lead-card-bottom">
                        <span className="muted small">{formatDate(lead.createdAt)}</span>
                        <select
                          value={lead.status}
                          aria-label="Move to stage"
                          onClick={(event) => event.stopPropagation()} /* don't open the form */
                          onChange={(event) => changeStage(lead, event.target.value)}
                        >
                          <EnumOptions values={STAGES} />
                        </select>
                      </div>
                    </article>
                  ))}
                </div>
              </section>
            );
          })}
        </div>
      )}

      {/* ---------- List view ---------- */}
      {!loading && !loadError && view === 'list' && (
        <div className="card">
          {visibleLeads.length === 0 ? (
            <div className="empty">No leads found.</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Lead</th><th>Company</th><th className="hide-mobile">Product</th>
                  <th className="hide-mobile">Source</th><th className="hide-mobile">Owner</th><th>Stage</th>
                </tr>
              </thead>
              <tbody>
                {visibleLeads.map((l) => (
                  <tr key={l.leadId} className="clickable" onClick={() => setEditing(l)}>
                    <td><strong>{l.leadName}</strong><div className="muted small">{l.email}</div></td>
                    <td>{l.companyName}</td>
                    <td className="hide-mobile">{productNames[l.productId]}</td>
                    <td className="hide-mobile">{niceLabel(l.source)}</td>
                    <td className="hide-mobile">{salespersonNames[l.salespersonId]}</td>
                    <td><Badge value={l.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {editing && (
        <LeadForm
          lead={editing}
          products={products}
          salespeople={salespeople}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

// =====================================================================
// Create / edit form
// =====================================================================
function LeadForm({ lead, products, salespeople, onClose, onSaved }) {
  const isNew = !lead.leadId;

  const [form, setForm] = useState(toFormValues(lead, {
    leadName: '', companyName: '', email: '', phone: '', productId: '', source: 'OTHER', status: 'NEW',
    leadScore: '', requiredLicenses: '', existingLicenses: '', preferredDurationMonths: '', purchaseTimeline: '',
    message: '', salespersonId: isAdmin() ? '' : mySalesPersonId(),
  }));
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
      leadName: form.leadName, companyName: form.companyName, email: form.email, phone: form.phone,
      productId: form.productId, salespersonId: form.salespersonId, source: form.source, message: form.message,
      status: form.status, leadScore: form.leadScore, requiredLicenses: form.requiredLicenses,
      existingLicenses: form.existingLicenses, preferredDurationMonths: form.preferredDurationMonths,
      purchaseTimeline: form.purchaseTimeline,
    }, ['productId', 'salespersonId', 'leadScore', 'requiredLicenses', 'existingLicenses', 'preferredDurationMonths']);

    try {
      if (isNew) {
        await apiPost('/leads', data);
        showToast('Lead created');
      } else {
        await apiPut('/leads/' + lead.leadId, data);
        showToast('Lead updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete the lead "${lead.leadName}"?`)) return;
    try {
      await apiDelete('/leads/' + lead.leadId);
      showToast('Lead deleted');
      onSaved();
    } catch (err) {
      showToast(err.status === 409 ? 'This lead still has tasks or calls linked to it.' : err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New lead' : lead.leadName} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Contact name *" error={fieldError('leadName')}>
            <input name="leadName" value={form.leadName} onChange={handleChange} required maxLength={100} autoFocus />
          </Field>
          <Field label="Company" error={fieldError('companyName')}>
            <input name="companyName" value={form.companyName} onChange={handleChange} maxLength={150} />
          </Field>
          <Field label="Email *" error={fieldError('email')}>
            <input name="email" type="email" value={form.email} onChange={handleChange} required />
          </Field>
          <Field label="Phone" error={fieldError('phone')}>
            <input name="phone" type="tel" value={form.phone} onChange={handleChange} />
          </Field>
          <Field label="Interested in">
            <select name="productId" value={form.productId} onChange={handleChange}>
              <option value="">— None —</option>
              {products.map((p) => <option key={p.productId} value={p.productId}>{p.productName}</option>)}
            </select>
          </Field>
          <Field label="Stage">
            <select name="status" value={form.status} onChange={handleChange}>
              <EnumOptions values={STAGES} />
            </select>
          </Field>
          <Field label="Source">
            <select name="source" value={form.source} onChange={handleChange}>
              <EnumOptions values={SOURCES} emptyText="— None —" />
            </select>
          </Field>
          <Field label="Lead score (0–100)" error={fieldError('leadScore')}>
            <input name="leadScore" type="number" min={0} max={100} value={form.leadScore} onChange={handleChange} />
          </Field>
          <Field label="Licences needed" error={fieldError('requiredLicenses')}>
            <input name="requiredLicenses" type="number" min={1} value={form.requiredLicenses} onChange={handleChange} />
          </Field>
          <Field label="Licences they already have" error={fieldError('existingLicenses')}>
            <input name="existingLicenses" type="number" min={0} value={form.existingLicenses} onChange={handleChange} />
          </Field>
          <Field label="Preferred term (months)" error={fieldError('preferredDurationMonths')}>
            <input name="preferredDurationMonths" type="number" min={1} value={form.preferredDurationMonths} onChange={handleChange} />
          </Field>
          <Field label="Buying timeline">
            <select name="purchaseTimeline" value={form.purchaseTimeline} onChange={handleChange}>
              <EnumOptions values={TIMELINES} emptyText="— None —" />
            </select>
          </Field>
          {isAdmin() && (
            <Field label="Owner (salesperson)">
              <select name="salespersonId" value={form.salespersonId} onChange={handleChange}>
                <option value="">— None —</option>
                {salespeople.map((s) => <option key={s.salesPersonId} value={s.salesPersonId}>{s.salesPersonName}</option>)}
              </select>
            </Field>
          )}
          <Field label="Notes / message" error={fieldError('message')} fullWidth>
            <textarea name="message" rows={4} maxLength={2000} value={form.message} onChange={handleChange} />
          </Field>
        </div>

        <div className="form-actions">
          {!isNew && isAdmin() && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create lead' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
