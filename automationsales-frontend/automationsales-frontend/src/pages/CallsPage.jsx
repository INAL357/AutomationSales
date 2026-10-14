// =====================================================================
// Calls page — every call with leads and customers
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiDelete, isAdmin, mySalesPersonId } from '../api';
import { loadCalls, loadCustomers, loadLeads, loadSalespeople } from '../lists';
import { formatDateTime, niceLabel, makeNameMap, matchesSearch, prepareForApi, toFormValues, toDateTimeInput, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const CALL_TYPES = ['OUTBOUND', 'INBOUND', 'FOLLOW_UP', 'DEMO', 'RENEWAL'];
const CALL_STATUSES = ['SCHEDULED', 'COMPLETED', 'NO_ANSWER', 'CANCELLED'];
const OUTCOMES = ['INTERESTED', 'NOT_INTERESTED', 'CALL_BACK', 'DEAL_WON', 'DEAL_LOST', 'NO_DECISION'];

export default function CallsPage() {
  const [calls, setCalls] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [leads, setLeads] = useState([]);
  const [salespeople, setSalespeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [editing, setEditing] = useState(null);

  async function loadData() {
    try {
      const [callList, customerList, leadList, salespersonList] = await Promise.all([
        loadCalls(), loadCustomers(), loadLeads(), loadSalespeople(),
      ]);
      // Newest calls first
      callList.sort((a, b) => String(b.startTime || b.createdAt).localeCompare(String(a.startTime || a.createdAt)));
      setCalls(callList);
      setCustomers(customerList);
      setLeads(leadList);
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

  const customerNames = makeNameMap(customers, 'customerId', 'customerName');
  const leadNames = makeNameMap(leads, 'leadId', 'leadName');

  function callWith(call) {
    if (call.customerId) return customerNames[call.customerId] || 'Customer #' + call.customerId;
    if (call.leadId) return (leadNames[call.leadId] || 'Lead #' + call.leadId) + ' (lead)';
    return '';
  }

  const visibleCalls = calls.filter((c) =>
    matchesSearch(`${c.callType} ${c.callSummary} ${c.outcome} ${c.nextAction} ${callWith(c)}`, search)
    && (statusFilter === '' || c.callStatus === statusFilter));

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Calls</h1>
          <p className="muted">Every conversation with leads and customers</p>
        </div>
        <button className="button button-primary" onClick={() => setEditing({})}>+ Log a call</button>
      </div>

      <div className="card">
        <div className="toolbar">
          <input type="search" className="search" placeholder="Search summary, outcome…" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <EnumOptions values={CALL_STATUSES} emptyText="All statuses" />
          </select>
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}
        {!loading && !loadError && visibleCalls.length === 0 && <div className="empty">No calls found.</div>}

        {visibleCalls.length > 0 && (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>When</th><th>With</th><th className="hide-mobile">Type</th>
                  <th className="hide-mobile">Duration</th><th className="hide-mobile">Outcome</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visibleCalls.map((c) => (
                  <tr key={c.salesCallId} className="clickable" onClick={() => setEditing(c)}>
                    <td>{c.startTime ? formatDateTime(c.startTime) : <span className="muted">Not set</span>}</td>
                    <td>{callWith(c)}</td>
                    <td className="hide-mobile">{niceLabel(c.callType)}</td>
                    <td className="hide-mobile">{c.durationMinutes !== null && c.durationMinutes !== undefined ? c.durationMinutes + ' min' : ''}</td>
                    <td className="hide-mobile">{niceLabel(c.outcome)}</td>
                    <td><Badge value={c.callStatus} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="table-count">{visibleCalls.length} of {calls.length} calls</p>
          </>
        )}
      </div>

      {editing && (
        <CallForm
          call={editing}
          customers={customers}
          leads={leads}
          salespeople={salespeople}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

function CallForm({ call, customers, leads, salespeople, onClose, onSaved }) {
  const isNew = !call.salesCallId;
  const [form, setForm] = useState(toFormValues({
    ...call,
    startTime: toDateTimeInput(call.startTime),
    endTime: toDateTimeInput(call.endTime),
  }, {
    customerId: '', leadId: '', renewalId: '', licenseId: '', callType: 'OUTBOUND', callStatus: 'COMPLETED',
    startTime: '', endTime: '', transcript: '', callSummary: '', outcome: '', nextAction: '',
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

    // The same rules as the backend, checked here for a quicker message
    if (!form.customerId && !form.leadId) {
      setError(new Error('Choose a customer or a lead for this call.'));
      return;
    }
    if (form.startTime && form.endTime && form.endTime < form.startTime) {
      setError(new Error('The end time must be after the start time.'));
      return;
    }

    setSaving(true);
    const data = prepareForApi({
      customerId: form.customerId, salespersonId: form.salespersonId, leadId: form.leadId,
      renewalId: form.renewalId, licenseId: form.licenseId, callType: form.callType, callStatus: form.callStatus,
      startTime: form.startTime, endTime: form.endTime, transcript: form.transcript,
      callSummary: form.callSummary, outcome: form.outcome, nextAction: form.nextAction,
    }, ['customerId', 'salespersonId', 'leadId', 'renewalId', 'licenseId'], ['startTime', 'endTime']);

    try {
      if (isNew) {
        await apiPost('/sales-calls', data);
        showToast('Call saved');
      } else {
        await apiPut('/sales-calls/' + call.salesCallId, data);
        showToast('Call updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('Delete this call record?')) return;
    try {
      await apiDelete('/sales-calls/' + call.salesCallId);
      showToast('Call deleted');
      onSaved();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'Log a call' : 'Edit call'} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <p className="muted small">Choose a customer <strong>or</strong> a lead.</p>
        <div className="form-grid">
          <Field label="Customer">
            <select name="customerId" value={form.customerId} onChange={handleChange}>
              <option value="">— None —</option>
              {customers.map((c) => <option key={c.customerId} value={c.customerId}>{c.customerName} · {c.companyName}</option>)}
            </select>
          </Field>
          <Field label="Lead">
            <select name="leadId" value={form.leadId} onChange={handleChange}>
              <option value="">— None —</option>
              {leads.map((l) => <option key={l.leadId} value={l.leadId}>{l.leadName}{l.companyName ? ' · ' + l.companyName : ''}</option>)}
            </select>
          </Field>
          <Field label="Call type *" error={fieldError('callType')}>
            <select name="callType" value={form.callType} onChange={handleChange} required><EnumOptions values={CALL_TYPES} /></select>
          </Field>
          <Field label="Status">
            <select name="callStatus" value={form.callStatus} onChange={handleChange}><EnumOptions values={CALL_STATUSES} /></select>
          </Field>
          <Field label="Start">
            <input name="startTime" type="datetime-local" value={form.startTime} onChange={handleChange} />
          </Field>
          <Field label="End">
            <input name="endTime" type="datetime-local" value={form.endTime} onChange={handleChange} />
          </Field>
          <Field label="Outcome">
            <select name="outcome" value={form.outcome} onChange={handleChange}><EnumOptions values={OUTCOMES} emptyText="— None —" /></select>
          </Field>
          {isAdmin() && (
            <Field label="Salesperson">
              <select name="salespersonId" value={form.salespersonId} onChange={handleChange}>
                <option value="">— None —</option>
                {salespeople.map((s) => <option key={s.salesPersonId} value={s.salesPersonId}>{s.salesPersonName}</option>)}
              </select>
            </Field>
          )}
          <Field label="Summary" error={fieldError('callSummary')} fullWidth>
            <textarea name="callSummary" rows={3} maxLength={2000} value={form.callSummary} onChange={handleChange} />
          </Field>
          <Field label="Next action" fullWidth>
            <input name="nextAction" value={form.nextAction} onChange={handleChange} placeholder="e.g. Send the proposal on Thursday" />
          </Field>
          <Field label="Transcript" fullWidth>
            <textarea name="transcript" rows={4} value={form.transcript} onChange={handleChange} />
          </Field>
        </div>

        <div className="form-actions">
          {!isNew && isAdmin() && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Save call' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
