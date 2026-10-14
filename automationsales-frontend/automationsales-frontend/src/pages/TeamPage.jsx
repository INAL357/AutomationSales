// =====================================================================
// Team & logins page (ADMIN only)
// =====================================================================
//   1. Salespeople    -> /api/salespersons. Creating one also creates their login
//                        (SalesPersonServiceImpl.createSalesPerson does that).
//   2. Login accounts -> /api/users. Create other admins and turn sign-in on/off:
//                        PATCH /api/users/{id}/enabled  body: { "enabled": false }
// =====================================================================
import { useEffect, useState } from 'react';
import { apiGet, apiPost, apiPut, apiPatch, apiDelete, getCurrentUser } from '../api';
import { formatDate, formatDateTime, niceLabel, prepareForApi, toFormValues, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const JOB_TITLES = ['SALES', 'SENIOR_SALES', 'ACCOUNT_MANAGER', 'SALES_MANAGER'];

export default function TeamPage() {
  const [salespeople, setSalespeople] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [editingSalesperson, setEditingSalesperson] = useState(null);
  const [addingAdmin, setAddingAdmin] = useState(false);

  const me = getCurrentUser();

  async function loadData() {
    try {
      const [salespersonList, userList] = await Promise.all([apiGet('/salespersons'), apiGet('/users')]);
      setSalespeople(salespersonList);
      setUsers(userList);
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

  async function setEnabled(user, enabled) {
    try {
      await apiPatch('/users/' + user.userId + '/enabled', { enabled: enabled });
      showToast(enabled ? 'Account enabled' : 'Account disabled. They are signed out immediately.');
      loadData();
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Team & logins</h1>
          <p className="muted">Add salespeople and control who can sign in</p>
        </div>
        <div className="header-buttons">
          <button className="button button-light" onClick={() => setAddingAdmin(true)}>+ New admin</button>
          <button className="button button-primary" onClick={() => setEditingSalesperson({})}>+ New salesperson</button>
        </div>
      </div>

      {loading && <Loading />}
      {loadError && <ErrorBox error={loadError} />}

      {!loading && !loadError && (
        <>
          <h2 className="section-title">Salespeople</h2>
          <div className="card">
            {salespeople.length === 0 ? (
              <div className="empty">No salespeople yet. Click “New salesperson”.</div>
            ) : (
              <table className="table">
                <thead>
                  <tr><th>Name</th><th>Job title</th><th className="hide-mobile">Phone</th><th className="hide-mobile">Joined</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {salespeople.map((s) => (
                    <tr key={s.salesPersonId} className="clickable" onClick={() => setEditingSalesperson(s)}>
                      <td><strong>{s.salesPersonName}</strong><div className="muted small">{s.salesPersonEmail}</div></td>
                      <td>{niceLabel(s.salesPersonRole)}</td>
                      <td className="hide-mobile">{s.salesPersonNumber}</td>
                      <td className="hide-mobile">{formatDate(s.createdAt)}</td>
                      <td><Badge value={s.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <h2 className="section-title">Login accounts</h2>
          <div className="card">
            <table className="table">
              <thead>
                <tr><th>Account</th><th>Role</th><th className="hide-mobile">Last sign in</th><th>Access</th></tr>
              </thead>
              <tbody>
                {users.map((u) => {
                  const isMe = u.userId === me.userId;
                  return (
                    <tr key={u.userId}>
                      <td>
                        <strong>{u.fullName}</strong>{isMe && <span className="muted small"> (you)</span>}
                        <div className="muted small">{u.email}</div>
                      </td>
                      <td><Badge value={u.role} /></td>
                      <td className="hide-mobile">{u.lastLoginAt ? formatDateTime(u.lastLoginAt) : <span className="muted">Never</span>}</td>
                      <td>
                        <label className="toggle" title={isMe ? 'You cannot disable yourself' : 'Allow this account to sign in'}>
                          <input
                            type="checkbox"
                            checked={u.enabled}
                            disabled={isMe}
                            onChange={(event) => setEnabled(u, event.target.checked)}
                          />
                          <span>{u.enabled ? 'Can sign in' : 'Disabled'}</span>
                        </label>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

      {editingSalesperson && (
        <SalespersonForm
          person={editingSalesperson}
          onClose={() => setEditingSalesperson(null)}
          onSaved={() => { setEditingSalesperson(null); loadData(); }}
        />
      )}

      {addingAdmin && (
        <AdminForm onClose={() => setAddingAdmin(false)} onSaved={() => { setAddingAdmin(false); loadData(); }} />
      )}
    </>
  );
}

// Create or edit a salesperson
function SalespersonForm({ person, onClose, onSaved }) {
  const isNew = !person.salesPersonId;
  const [form, setForm] = useState(toFormValues(person, {
    salesPersonName: '', salesPersonEmail: '', salesPersonNumber: '', salesPersonRole: 'SALES', status: 'ACTIVE', password: '',
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
      salesPersonName: form.salesPersonName,
      salesPersonEmail: form.salesPersonEmail,
      salesPersonNumber: form.salesPersonNumber,
      salesPersonRole: form.salesPersonRole,
      status: form.status,
      // The password is only sent when creating (it creates their login)
      password: isNew ? form.password : '',
    });

    try {
      if (isNew) {
        await apiPost('/salespersons', data);
        showToast('Salesperson created. They can sign in now.');
      } else {
        await apiPut('/salespersons/' + person.salesPersonId, data);
        showToast('Salesperson updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete ${person.salesPersonName}? Tip: setting them to Inactive and disabling their login keeps their history.`)) return;
    try {
      await apiDelete('/salespersons/' + person.salesPersonId);
      showToast('Salesperson deleted');
      onSaved();
    } catch (err) {
      showToast(err.status === 409 ? 'This salesperson still has leads, customers or a login linked. Disable them instead.' : err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New salesperson' : 'Edit salesperson'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Full name *" error={fieldError('salesPersonName')}>
            <input name="salesPersonName" value={form.salesPersonName} onChange={handleChange} required maxLength={50} autoFocus />
          </Field>
          <Field label="Email (used to sign in) *" error={fieldError('salesPersonEmail')}>
            <input name="salesPersonEmail" type="email" value={form.salesPersonEmail} onChange={handleChange} required readOnly={!isNew} />
          </Field>
          <Field label="Phone" error={fieldError('salesPersonNumber')}>
            <input name="salesPersonNumber" type="tel" value={form.salesPersonNumber} onChange={handleChange} />
          </Field>
          <Field label="Job title *" error={fieldError('salesPersonRole')}>
            <select name="salesPersonRole" value={form.salesPersonRole} onChange={handleChange} required>
              <EnumOptions values={JOB_TITLES} />
            </select>
          </Field>

          {isNew ? (
            <Field label="Temporary password *" error={fieldError('password')}>
              <input name="password" type="password" value={form.password} onChange={handleChange} required minLength={8} autoComplete="new-password" />
              <small className="muted">At least 8 characters. Give it to them privately.</small>
            </Field>
          ) : (
            <Field label="Status">
              <select name="status" value={form.status} onChange={handleChange}><EnumOptions values={['ACTIVE', 'INACTIVE']} /></select>
            </Field>
          )}
        </div>
        <div className="form-actions">
          {!isNew && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create salesperson' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}

// Create another ADMIN login: POST /api/users with role ADMIN
function AdminForm({ onClose, onSaved }) {
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await apiPost('/users', { ...prepareForApi(form), role: 'ADMIN', salesPersonId: null });
      showToast('Admin account created');
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title="New admin" onClose={onClose}>
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <p className="muted">Admins can see everything and manage the team. To add a salesperson, use “New salesperson” instead.</p>
        <div className="form-grid">
          <Field label="Full name *" error={fieldError('fullName')}>
            <input name="fullName" value={form.fullName} onChange={handleChange} required maxLength={50} autoFocus />
          </Field>
          <Field label="Email *" error={fieldError('email')}>
            <input name="email" type="email" value={form.email} onChange={handleChange} required />
          </Field>
          <Field label="Password *" error={fieldError('password')}>
            <input name="password" type="password" value={form.password} onChange={handleChange} required minLength={8} maxLength={72} autoComplete="new-password" />
          </Field>
        </div>
        <div className="form-actions">
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>{saving ? 'Saving…' : 'Create admin'}</button>
        </div>
      </form>
    </Modal>
  );
}
