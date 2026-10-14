// =====================================================================
// Renewals page
// =====================================================================
// Renewals are usually created by the daily RenewalScheduler (90/60/30/14/7 days before expiry).
// Admins can start that automation now with "Run automation now".
// The status dropdown in each row saves straight away:
//   PATCH /api/renewals/{id}/status?status=RENEWED
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiPatch, apiDelete, isAdmin } from '../api';
import { loadRenewals, loadCustomers, loadLicenses } from '../lists';
import { formatDate, daysFromToday, niceLabel, makeNameMap, matchesSearch, prepareForApi, toFormValues, toDateTimeInput, showToast } from '../utils';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const STATUSES = ['PENDING', 'IN_PROGRESS', 'RENEWED', 'LOST'];
const INTENTS = ['RENEW', 'UPGRADE', 'DOWNGRADE', 'UNDECIDED', 'CANCEL'];

export default function RenewalsPage() {
  const [renewals, setRenewals] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [licenses, setLicenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [editing, setEditing] = useState(null);
  const [automationResult, setAutomationResult] = useState(null);
  const [running, setRunning] = useState(false);

  async function loadData() {
    try {
      const [renewalList, customerList, licenseList] = await Promise.all([loadRenewals(), loadCustomers(), loadLicenses()]);
      // Soonest renewal date first
      renewalList.sort((a, b) => String(a.nextRenewalDate || '9999').localeCompare(String(b.nextRenewalDate || '9999')));
      setRenewals(renewalList);
      setCustomers(customerList);
      setLicenses(licenseList);
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

  const customerNames = makeNameMap(customers, 'customerId', 'customerName');

  const visibleRenewals = renewals.filter((r) =>
    matchesSearch(`${r.renewalId} ${customerNames[r.customerId]} ${r.notes} ${r.customerIntent}`, search)
    && (statusFilter === '' || r.status === statusFilter));

  // How many renewals in each status, e.g. { PENDING: 4, RENEWED: 2 }
  const counts = {};
  for (const r of renewals) {
    counts[r.status] = (counts[r.status] || 0) + 1;
  }

  async function changeStatus(renewal, newStatus) {
    try {
      const updated = await apiPatch('/renewals/' + renewal.renewalId + '/status?status=' + newStatus);
      setRenewals(renewals.map((r) => (r.renewalId === renewal.renewalId ? updated : r)));
      showToast('Renewal marked as ' + niceLabel(newStatus));
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  async function runAutomation() {
    setRunning(true);
    try {
      // Returns a RenewalAutomationResultDto
      const result = await apiPost('/renewals/automation/run');
      setAutomationResult(result);
      loadData();
    } catch (error) {
      showToast(error.message, 'error');
    } finally {
      setRunning(false);
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Renewals</h1>
          <p className="muted">Customers whose licences are coming up for renewal</p>
        </div>
        <div className="header-buttons">
          {isAdmin() && (
            <button className="button button-light" onClick={runAutomation} disabled={running}>
              {running ? 'Running…' : '↻ Run automation now'}
            </button>
          )}
          <button className="button button-primary" onClick={() => setEditing({})}>+ New renewal</button>
        </div>
      </div>

      <div className="stat-row">
        {STATUSES.map((s) => (
          <div className="stat-card small-stat" key={s}>
            <span className="stat-label">{niceLabel(s)}</span>
            <span className="stat-value">{counts[s] || 0}</span>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="toolbar">
          <input type="search" className="search" placeholder="Search customer or notes…" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <EnumOptions values={STATUSES} emptyText="All statuses" />
          </select>
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}
        {!loading && !loadError && visibleRenewals.length === 0 && (
          <div className="empty">No renewals here. The daily automation creates them before licences expire.</div>
        )}

        {visibleRenewals.length > 0 && (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>Renewal</th><th>Customer</th><th className="hide-mobile">Licence</th>
                  <th>Renewal date</th><th className="hide-mobile">Intent</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visibleRenewals.map((r) => {
                  const days = daysFromToday(r.nextRenewalDate);
                  let dateClass = '';
                  if (days !== null && days < 0) dateClass = 'text-danger';
                  else if (days !== null && days <= 30) dateClass = 'text-warning';

                  return (
                    <tr key={r.renewalId} className="clickable" onClick={() => setEditing(r)}>
                      <td><strong>#{r.renewalId}</strong></td>
                      <td>{customerNames[r.customerId] || (r.customerId ? 'Customer #' + r.customerId : '')}</td>
                      <td className="hide-mobile">Licence #{r.licenseId}</td>
                      <td className={dateClass}>
                        {formatDate(r.nextRenewalDate)}
                        {days !== null && <div className="muted small">{days < 0 ? -days + ' days ago' : 'in ' + days + ' days'}</div>}
                      </td>
                      <td className="hide-mobile">{niceLabel(r.customerIntent)}</td>
                      <td>
                        <select
                          value={r.status}
                          aria-label="Change status"
                          onClick={(event) => event.stopPropagation()} /* don't open the form */
                          onChange={(event) => changeStatus(r, event.target.value)}
                        >
                          <EnumOptions values={STATUSES} />
                        </select>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            <p className="table-count">{visibleRenewals.length} of {renewals.length} renewals</p>
          </>
        )}
      </div>

      {automationResult && (
        <Modal title="Renewal automation finished" onClose={() => setAutomationResult(null)} size="modal-small">
          <dl className="details">
            <dt>Licences checked</dt><dd>{automationResult.licensesChecked}</dd>
            <dt>Statuses updated</dt><dd>{automationResult.statusesUpdated}</dd>
            <dt>Renewals created</dt><dd>{automationResult.renewalsCreated}</dd>
            <dt>Tasks created</dt><dd>{automationResult.tasksCreated}</dd>
            <dt>Notifications queued</dt><dd>{automationResult.notificationsCreated}</dd>
            <dt>Skipped (already reminded)</dt><dd>{automationResult.skippedAlreadyReminded}</dd>
          </dl>
          <div className="form-actions">
            <button className="button button-primary" onClick={() => setAutomationResult(null)}>OK</button>
          </div>
        </Modal>
      )}

      {editing && (
        <RenewalForm
          renewal={editing}
          customers={customers}
          licenses={licenses}
          customerNames={customerNames}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

function RenewalForm({ renewal, customers, licenses, customerNames, onClose, onSaved }) {
  const isNew = !renewal.renewalId;
  const [form, setForm] = useState(toFormValues({
    ...renewal,
    nextRenewalDate: toDateTimeInput(renewal.nextRenewalDate),
  }, {
    licenseId: '', customerId: '', status: 'PENDING', nextRenewalDate: '', customerIntent: '',
    requestedLicenseCount: '', requestedDurationMonths: '', notes: '',
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
      licenseId: form.licenseId, customerId: form.customerId, status: form.status,
      nextRenewalDate: form.nextRenewalDate, customerIntent: form.customerIntent,
      requestedLicenseCount: form.requestedLicenseCount, requestedDurationMonths: form.requestedDurationMonths,
      notes: form.notes,
    }, ['licenseId', 'customerId', 'requestedLicenseCount', 'requestedDurationMonths'], ['nextRenewalDate']);

    try {
      if (isNew) {
        await apiPost('/renewals', data);
        showToast('Renewal created');
      } else {
        await apiPut('/renewals/' + renewal.renewalId, data);
        showToast('Renewal updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('Delete renewal #' + renewal.renewalId + '?')) return;
    try {
      await apiDelete('/renewals/' + renewal.renewalId);
      showToast('Renewal deleted');
      onSaved();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New renewal' : 'Renewal #' + renewal.renewalId} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Licence *" error={fieldError('licenseId')}>
            <select name="licenseId" value={form.licenseId} onChange={handleChange} required>
              <option value="">Select…</option>
              {licenses.map((l) => (
                <option key={l.licenseId} value={l.licenseId}>
                  Licence #{l.licenseId} · {customerNames[l.customerId] || 'Customer #' + l.customerId}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Customer">
            <select name="customerId" value={form.customerId} onChange={handleChange}>
              <option value="">— None —</option>
              {customers.map((c) => <option key={c.customerId} value={c.customerId}>{c.customerName} · {c.companyName}</option>)}
            </select>
          </Field>
          <Field label="Status">
            <select name="status" value={form.status} onChange={handleChange}><EnumOptions values={STATUSES} /></select>
          </Field>
          <Field label="Renewal date">
            <input name="nextRenewalDate" type="datetime-local" value={form.nextRenewalDate} onChange={handleChange} />
          </Field>
          <Field label="Customer intent">
            <select name="customerIntent" value={form.customerIntent} onChange={handleChange}><EnumOptions values={INTENTS} emptyText="— None —" /></select>
          </Field>
          <Field label="Requested seats" error={fieldError('requestedLicenseCount')}>
            <input name="requestedLicenseCount" type="number" min={0} value={form.requestedLicenseCount} onChange={handleChange} />
          </Field>
          <Field label="Requested term (months)" error={fieldError('requestedDurationMonths')}>
            <input name="requestedDurationMonths" type="number" min={1} value={form.requestedDurationMonths} onChange={handleChange} />
          </Field>
          <Field label="Notes" error={fieldError('notes')} fullWidth>
            <textarea name="notes" rows={4} maxLength={2000} value={form.notes} onChange={handleChange} />
          </Field>
        </div>

        <div className="form-actions">
          {!isNew && isAdmin() && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create renewal' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
