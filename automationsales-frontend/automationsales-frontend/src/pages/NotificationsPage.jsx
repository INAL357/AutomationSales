// =====================================================================
// Notifications page — emails queued, sent or failed
// =====================================================================
// The backend's NotificationScheduler sends PENDING emails automatically.
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiDelete, isAdmin, mySalesPersonId } from '../api';
import { loadNotifications, loadCustomers, loadLeads } from '../lists';
import { formatDateTime, niceLabel, matchesSearch, prepareForApi, toFormValues, toDateTimeInput, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const STATUSES = ['PENDING', 'SENT', 'FAILED'];
const TYPES = ['GENERAL', 'LEAD_FOLLOW_UP', 'RENEWAL_REMINDER', 'TASK_REMINDER', 'WELCOME'];

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [leads, setLeads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [editing, setEditing] = useState(null);

  async function loadData() {
    try {
      const [list, customerList, leadList] = await Promise.all([loadNotifications(), loadCustomers(), loadLeads()]);
      list.sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt))); // newest first
      setNotifications(list);
      setCustomers(customerList);
      setLeads(leadList);
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

  const visible = notifications.filter((n) =>
    matchesSearch(`${n.subject} ${n.recipientEmail} ${n.type}`, search)
    && (statusFilter === '' || n.status === statusFilter));

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Notifications</h1>
          <p className="muted">Emails queued and sent by the system</p>
        </div>
        <button className="button button-primary" onClick={() => setEditing({})}>+ Queue an email</button>
      </div>

      <div className="card">
        <div className="toolbar">
          <input type="search" className="search" placeholder="Search subject or recipient…" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <EnumOptions values={STATUSES} emptyText="All statuses" />
          </select>
        </div>

        {loading && <Loading />}
        {loadError && <ErrorBox error={loadError} />}
        {!loading && !loadError && visible.length === 0 && <div className="empty">No notifications found.</div>}

        {visible.length > 0 && (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>Email</th><th className="hide-mobile">Type</th><th className="hide-mobile">Scheduled</th>
                  <th className="hide-mobile">Sent</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((n) => (
                  <tr key={n.notificationId} className="clickable" onClick={() => setEditing(n)}>
                    <td><strong>{n.subject}</strong><div className="muted small">{n.recipientEmail}</div></td>
                    <td className="hide-mobile">{niceLabel(n.type)}</td>
                    <td className="hide-mobile">{n.scheduledAt ? formatDateTime(n.scheduledAt) : 'As soon as possible'}</td>
                    <td className="hide-mobile">{formatDateTime(n.sentAt)}</td>
                    <td>
                      <Badge value={n.status} />
                      {n.status === 'FAILED' && n.failureReason && <div className="small text-danger">{n.failureReason}</div>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="table-count">{visible.length} of {notifications.length} notifications</p>
          </>
        )}
      </div>

      {editing && (
        <NotificationForm
          notification={editing}
          customers={customers}
          leads={leads}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); loadData(); }}
        />
      )}
    </>
  );
}

function NotificationForm({ notification, customers, leads, onClose, onSaved }) {
  const isNew = !notification.notificationId;
  const [form, setForm] = useState(toFormValues({
    ...notification,
    scheduledAt: toDateTimeInput(notification.scheduledAt),
  }, {
    recipientEmail: '', type: 'GENERAL', subject: '', message: '', scheduledAt: '', status: 'PENDING',
    customerId: '', leadId: '', licenseId: '', renewalId: '', salesPersonId: mySalesPersonId() || '',
  }));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  // Picking a customer fills in their email address
  function handleCustomerChange(event) {
    const customer = customers.find((c) => String(c.customerId) === event.target.value);
    setForm({ ...form, customerId: event.target.value, recipientEmail: customer ? customer.customerEmail : form.recipientEmail });
  }

  // Picking a lead fills in their email address
  function handleLeadChange(event) {
    const lead = leads.find((l) => String(l.leadId) === event.target.value);
    setForm({ ...form, leadId: event.target.value, recipientEmail: lead ? lead.email : form.recipientEmail });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);

    const data = prepareForApi({
      customerId: form.customerId, leadId: form.leadId, salesPersonId: form.salesPersonId,
      licenseId: form.licenseId, renewalId: form.renewalId, type: form.type, recipientEmail: form.recipientEmail,
      subject: form.subject, message: form.message, status: form.status,
    }, ['customerId', 'leadId', 'salesPersonId', 'licenseId', 'renewalId']);

    // scheduledAt is a java.sql.Timestamp in the backend: send a full date with time zone
    data.scheduledAt = form.scheduledAt ? new Date(form.scheduledAt).toISOString() : null;
    // Keep sentAt as it was when editing
    data.sentAt = notification.sentAt || null;

    try {
      if (isNew) {
        await apiPost('/notifications', data);
        showToast('Email queued. It will be sent automatically.');
      } else {
        await apiPut('/notifications/' + notification.notificationId, data);
        showToast('Notification updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('Delete this notification?')) return;
    try {
      await apiDelete('/notifications/' + notification.notificationId);
      showToast('Notification deleted');
      onSaved();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'Queue an email' : 'Notification'} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Customer">
            <select name="customerId" value={form.customerId} onChange={handleCustomerChange}>
              <option value="">— None —</option>
              {customers.map((c) => <option key={c.customerId} value={c.customerId}>{c.customerName} · {c.customerEmail}</option>)}
            </select>
          </Field>
          <Field label="Lead">
            <select name="leadId" value={form.leadId} onChange={handleLeadChange}>
              <option value="">— None —</option>
              {leads.map((l) => <option key={l.leadId} value={l.leadId}>{l.leadName} · {l.email}</option>)}
            </select>
          </Field>
          <Field label="To *" error={fieldError('recipientEmail')}>
            <input name="recipientEmail" type="email" value={form.recipientEmail} onChange={handleChange} required />
          </Field>
          <Field label="Type *" error={fieldError('type')}>
            <select name="type" value={form.type} onChange={handleChange} required><EnumOptions values={TYPES} /></select>
          </Field>
          <Field label="Subject *" error={fieldError('subject')} fullWidth>
            <input name="subject" value={form.subject} onChange={handleChange} required maxLength={255} />
          </Field>
          <Field label="Message *" error={fieldError('message')} fullWidth>
            <textarea name="message" rows={6} maxLength={2000} value={form.message} onChange={handleChange} required />
          </Field>
          <Field label="Send at (empty = as soon as possible)">
            <input name="scheduledAt" type="datetime-local" value={form.scheduledAt} onChange={handleChange} />
          </Field>
          <Field label="Status">
            <select name="status" value={form.status} onChange={handleChange}><EnumOptions values={STATUSES} /></select>
          </Field>
        </div>

        <div className="form-actions">
          {!isNew && isAdmin() && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Queue email' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
