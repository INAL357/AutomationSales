// =====================================================================
// Tasks page — to-do list grouped by when things are due
// =====================================================================
// Ticking the checkbox marks a task as DONE. This endpoint takes the status in the URL:
//   PATCH /api/sales-tasks/{id}/status?taskStatus=DONE
// =====================================================================
import { useEffect, useState } from 'react';
import { apiPost, apiPut, apiPatch, apiDelete, isAdmin, mySalesPersonId } from '../api';
import { loadTasks, loadCustomers, loadLeads, loadSalespeople } from '../lists';
import { formatDate, niceLabel, daysFromToday, todayForInput, makeNameMap, matchesSearch, prepareForApi, toFormValues, showToast } from '../utils';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { Field, FormError, EnumOptions, Loading, ErrorBox } from '../components/FormParts';

const STATUSES = ['OPEN', 'IN_PROGRESS', 'DONE', 'CANCELLED'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
const TYPES = ['LEAD_FOLLOW_UP', 'RENEWAL_FOLLOW_UP', 'CALL', 'EMAIL', 'MEETING', 'DEMO', 'OTHER'];

// The groups shown on the page, top to bottom
const GROUPS = [
  { id: 'overdue', title: 'Overdue', className: 'group-overdue' },
  { id: 'today', title: 'Due today', className: 'group-today' },
  { id: 'upcoming', title: 'Upcoming', className: '' },
  { id: 'noDate', title: 'No due date', className: '' },
  { id: 'done', title: 'Completed', className: '' },
];

function isFinished(task) {
  return task.taskStatus === 'DONE' || task.taskStatus === 'CANCELLED';
}

// Which group does a task belong to?
function groupOf(task) {
  if (isFinished(task)) return 'done';
  if (!task.dueDate) return 'noDate';
  const days = daysFromToday(task.dueDate);
  if (days < 0) return 'overdue';
  if (days === 0) return 'today';
  return 'upcoming';
}

export default function TasksPage() {
  const [tasks, setTasks] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [leads, setLeads] = useState([]);
  const [salespeople, setSalespeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [search, setSearch] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');
  const [editing, setEditing] = useState(null);

  async function loadData() {
    try {
      const [taskList, customerList, leadList, salespersonList] = await Promise.all([
        loadTasks(), loadCustomers(), loadLeads(), loadSalespeople(),
      ]);
      setTasks(taskList);
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
  const salespersonNames = makeNameMap(salespeople, 'salesPersonId', 'salesPersonName');

  const visibleTasks = tasks
    .filter((t) => matchesSearch(`${t.title} ${t.description} ${t.taskType}`, search))
    .filter((t) => priorityFilter === '' || t.priority === priorityFilter)
    .sort((a, b) => (a.dueDate || '9999').localeCompare(b.dueDate || '9999')); // soonest first, no date last

  async function setStatus(task, newStatus) {
    try {
      const updated = await apiPatch('/sales-tasks/' + task.salesTaskId + '/status?taskStatus=' + newStatus);
      setTasks(tasks.map((t) => (t.salesTaskId === task.salesTaskId ? updated : t)));
      showToast(newStatus === 'DONE' ? 'Task completed ✓' : 'Task reopened');
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  // "Customer: Ahmed" / "Lead: Sara"
  function relatedTo(task) {
    if (task.customerId) return 'Customer: ' + (customerNames[task.customerId] || '#' + task.customerId);
    if (task.leadId) return 'Lead: ' + (leadNames[task.leadId] || '#' + task.leadId);
    return '';
  }

  function dueText(task) {
    if (!task.dueDate) return '';
    const days = daysFromToday(task.dueDate);
    if (!isFinished(task)) {
      if (days < 0) return -days + (days === -1 ? ' day overdue' : ' days overdue');
      if (days === 0) return 'Today';
      if (days === 1) return 'Tomorrow';
    }
    return formatDate(task.dueDate);
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Tasks</h1>
          <p className="muted">Follow-ups created by you and by the renewal automation</p>
        </div>
        <button className="button button-primary" onClick={() => setEditing({})}>+ New task</button>
      </div>

      <div className="toolbar toolbar-plain">
        <input type="search" className="search" placeholder="Search tasks…" value={search} onChange={(e) => setSearch(e.target.value)} />
        <select value={priorityFilter} onChange={(e) => setPriorityFilter(e.target.value)}>
          <EnumOptions values={PRIORITIES} emptyText="All priorities" />
        </select>
      </div>

      {loading && <Loading />}
      {loadError && <ErrorBox error={loadError} />}

      {!loading && !loadError && visibleTasks.length === 0 && (
        <div className="card"><div className="empty">No tasks. Nice work! 🎉</div></div>
      )}

      {GROUPS.map((group) => {
        let groupTasks = visibleTasks.filter((t) => groupOf(t) === group.id);
        if (groupTasks.length === 0) return null;
        const total = groupTasks.length;
        // Show only the last 20 completed tasks
        if (group.id === 'done') groupTasks = groupTasks.slice(-20).reverse();

        return (
          <section className={'task-group ' + group.className} key={group.id}>
            <h2>{group.title} <span className="muted">{total}</span></h2>
            <div className="card">
              {groupTasks.map((task) => (
                <div key={task.salesTaskId} className={isFinished(task) ? 'task-row finished' : 'task-row'} onClick={() => setEditing(task)}>
                  <input
                    type="checkbox"
                    aria-label="Mark as done"
                    checked={task.taskStatus === 'DONE'}
                    onClick={(event) => event.stopPropagation()}
                    onChange={(event) => setStatus(task, event.target.checked ? 'DONE' : 'OPEN')}
                  />
                  <div className="task-main">
                    <strong>{task.title}</strong>
                    <div className="muted small">
                      {niceLabel(task.taskType)}
                      {relatedTo(task) && ' · ' + relatedTo(task)}
                      {isAdmin() && task.salespersonId && ' · ' + (salespersonNames[task.salespersonId] || '')}
                    </div>
                  </div>
                  <span className={groupOf(task) === 'overdue' ? 'task-due text-danger' : 'task-due'}>{dueText(task)}</span>
                  <Badge value={task.priority} />
                </div>
              ))}
            </div>
          </section>
        );
      })}

      {editing && (
        <TaskForm
          task={editing}
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

function TaskForm({ task, customers, leads, salespeople, onClose, onSaved }) {
  const isNew = !task.salesTaskId;
  const [form, setForm] = useState(toFormValues(task, {
    title: '', description: '', taskType: 'CALL', priority: 'MEDIUM', taskStatus: 'OPEN', dueDate: todayForInput(),
    customerId: '', leadId: '', licenseId: '', renewalId: '', salespersonId: isAdmin() ? '' : mySalesPersonId(),
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
      customerId: form.customerId, leadId: form.leadId, licenseId: form.licenseId, renewalId: form.renewalId,
      salespersonId: form.salespersonId, title: form.title, description: form.description, taskType: form.taskType,
      priority: form.priority, taskStatus: form.taskStatus, dueDate: form.dueDate,
    }, ['customerId', 'leadId', 'licenseId', 'renewalId', 'salespersonId']);

    try {
      if (isNew) {
        await apiPost('/sales-tasks', data);
        showToast('Task created');
      } else {
        await apiPut('/sales-tasks/' + task.salesTaskId, data);
        showToast('Task updated');
      }
      onSaved();
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete the task "${task.title}"?`)) return;
    try {
      await apiDelete('/sales-tasks/' + task.salesTaskId);
      showToast('Task deleted');
      onSaved();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <Modal title={isNew ? 'New task' : 'Edit task'} onClose={onClose} size="modal-large">
      <form className="form" onSubmit={handleSubmit}>
        <FormError error={error} />
        <div className="form-grid">
          <Field label="Title *" error={fieldError('title')} fullWidth>
            <input name="title" value={form.title} onChange={handleChange} required maxLength={200} autoFocus
              placeholder="e.g. Call Ahmed about the renewal" />
          </Field>
          <Field label="Type">
            <select name="taskType" value={form.taskType} onChange={handleChange}><EnumOptions values={TYPES} /></select>
          </Field>
          <Field label="Priority">
            <select name="priority" value={form.priority} onChange={handleChange}><EnumOptions values={PRIORITIES} /></select>
          </Field>
          <Field label="Status">
            <select name="taskStatus" value={form.taskStatus} onChange={handleChange}><EnumOptions values={STATUSES} /></select>
          </Field>
          <Field label="Due date" error={fieldError('dueDate')}>
            <input name="dueDate" type="date" value={form.dueDate} onChange={handleChange} />
          </Field>
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
          {isAdmin() && (
            <Field label="Assigned to">
              <select name="salespersonId" value={form.salespersonId} onChange={handleChange}>
                <option value="">— None —</option>
                {salespeople.map((s) => <option key={s.salesPersonId} value={s.salesPersonId}>{s.salesPersonName}</option>)}
              </select>
            </Field>
          )}
          <Field label="Description" error={fieldError('description')} fullWidth>
            <textarea name="description" rows={4} maxLength={2000} value={form.description} onChange={handleChange} />
          </Field>
        </div>

        <div className="form-actions">
          {!isNew && isAdmin() && <button type="button" className="button button-danger-light" onClick={handleDelete}>Delete</button>}
          <button type="button" className="button button-light" onClick={onClose}>Cancel</button>
          <button type="submit" className="button button-primary" disabled={saving}>
            {saving ? 'Saving…' : isNew ? 'Create task' : 'Save changes'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
