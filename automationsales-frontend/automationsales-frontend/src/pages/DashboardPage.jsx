// =====================================================================
// Dashboard — the first page after signing in
// =====================================================================
// Admins see the whole team's numbers; salespeople see only their own
// (lists.js picks the right endpoints).
// =====================================================================
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getCurrentUser, isAdmin } from '../api';
import { loadLeads, loadCustomers, loadLicenses, loadTasks, loadRenewals } from '../lists';
import { formatDate, niceLabel, daysFromToday, makeNameMap } from '../utils';
import Badge from '../components/Badge';
import { Loading, ErrorBox } from '../components/FormParts';

const STAGES = ['NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED', 'UNQUALIFIED', 'LOST'];

export default function DashboardPage() {
  const user = getCurrentUser();
  const [data, setData] = useState(null); // { leads, customers, licenses, tasks, renewals }
  const [loadError, setLoadError] = useState(null);

  useEffect(() => {
    Promise.all([loadLeads(), loadCustomers(), loadLicenses(), loadTasks(), loadRenewals()])
      .then(([leads, customers, licenses, tasks, renewals]) => setData({ leads, customers, licenses, tasks, renewals }))
      .catch(setLoadError);
  }, []);

  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{greeting}, {user.fullName.split(' ')[0]}</h1>
          <p className="muted">{isAdmin() ? 'Here is how the whole team is doing today.' : 'Here is what needs your attention today.'}</p>
        </div>
      </div>

      {loadError && <ErrorBox error={loadError} />}
      {!loadError && !data && <Loading />}
      {data && <DashboardContent {...data} />}
    </>
  );
}

function DashboardContent({ leads, customers, licenses, tasks, renewals }) {
  const customerNames = makeNameMap(customers, 'customerId', 'customerName');

  // ---------- Numbers for the four cards ----------
  const openLeads = leads.filter((l) => ['NEW', 'CONTACTED', 'QUALIFIED'].includes(l.status));
  const activeLicenses = licenses.filter((l) => l.status === 'ACTIVE' || l.status === 'EXPIRING_SOON');
  const expiringSoon = licenses
    .filter((l) => l.daysRemaining !== null && l.daysRemaining >= 0 && l.daysRemaining <= 90)
    .sort((a, b) => a.daysRemaining - b.daysRemaining);
  const openTasks = tasks.filter((t) => t.taskStatus === 'OPEN' || t.taskStatus === 'IN_PROGRESS');
  const overdueTasks = openTasks.filter((t) => t.dueDate && daysFromToday(t.dueDate) < 0);
  const activeRenewals = renewals.filter((r) => r.status === 'PENDING' || r.status === 'IN_PROGRESS');

  // Conversion rate = converted / leads that reached an end (converted + lost + unqualified)
  const converted = leads.filter((l) => l.status === 'CONVERTED').length;
  const closed = leads.filter((l) => ['CONVERTED', 'LOST', 'UNQUALIFIED'].includes(l.status)).length;
  const conversionRate = closed > 0 ? Math.round((converted / closed) * 100) : null;

  // ---------- Pipeline: number of leads in each stage ----------
  const stageCounts = STAGES.map((stage) => ({ stage, count: leads.filter((l) => l.status === stage).length }));
  const biggestStage = Math.max(1, ...stageCounts.map((s) => s.count));

  // ---------- Tasks due within 7 days (overdue first) ----------
  const tasksDue = openTasks
    .filter((t) => t.dueDate && daysFromToday(t.dueDate) <= 7)
    .sort((a, b) => a.dueDate.localeCompare(b.dueDate))
    .slice(0, 6);

  // ---------- Newest 5 leads ----------
  const newestLeads = [...leads].sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt))).slice(0, 5);

  function dueLabel(task) {
    const days = daysFromToday(task.dueDate);
    if (days < 0) return <span className="text-danger">{-days}d overdue</span>;
    if (days === 0) return <span className="text-warning">Today</span>;
    if (days === 1) return 'Tomorrow';
    return formatDate(task.dueDate);
  }

  return (
    <>
      <div className="stat-row">
        <Link className="stat-card" to="/leads">
          <span className="stat-label">Open leads</span>
          <span className="stat-value">{openLeads.length}</span>
          <span className="stat-note">{conversionRate === null ? 'No closed leads yet' : conversionRate + '% conversion rate'}</span>
        </Link>
        <Link className="stat-card" to="/customers">
          <span className="stat-label">Customers</span>
          <span className="stat-value">{customers.length}</span>
          <span className="stat-note">{activeLicenses.length} active licences</span>
        </Link>
        <Link className={expiringSoon.length > 0 ? 'stat-card stat-warning' : 'stat-card'} to="/licenses">
          <span className="stat-label">Expiring in 90 days</span>
          <span className="stat-value">{expiringSoon.length}</span>
          <span className="stat-note">{activeRenewals.length} renewals in progress</span>
        </Link>
        <Link className={overdueTasks.length > 0 ? 'stat-card stat-danger' : 'stat-card'} to="/tasks">
          <span className="stat-label">Open tasks</span>
          <span className="stat-value">{openTasks.length}</span>
          <span className="stat-note">{overdueTasks.length} overdue</span>
        </Link>
      </div>

      <div className="dashboard-grid">
        <section className="card card-padded">
          <div className="card-title"><h2>Lead pipeline</h2><Link to="/leads">Open board →</Link></div>
          <div className="pipeline">
            {stageCounts.map(({ stage, count }) => (
              <Link className="pipeline-row" to="/leads" key={stage}>
                <span className="pipeline-label">{niceLabel(stage)}</span>
                <span className="pipeline-bar">
                  <span className={'fill fill-' + stage.toLowerCase()} style={{ width: (count / biggestStage) * 100 + '%' }} />
                </span>
                <span className="pipeline-count">{count}</span>
              </Link>
            ))}
          </div>
        </section>

        <section className="card card-padded">
          <div className="card-title"><h2>Tasks due this week</h2><Link to="/tasks">All tasks →</Link></div>
          {tasksDue.length === 0 ? <p className="muted">Nothing due this week. 🎉</p> : (
            <ul className="mini-list">
              {tasksDue.map((t) => (
                <li key={t.salesTaskId}><span>{t.title}</span><span className="small">{dueLabel(t)}</span></li>
              ))}
            </ul>
          )}
        </section>

        <section className="card card-padded">
          <div className="card-title"><h2>Licences expiring soon</h2><Link to="/licenses">All licences →</Link></div>
          {expiringSoon.length === 0 ? <p className="muted">No licences expire in the next 90 days.</p> : (
            <ul className="mini-list">
              {expiringSoon.slice(0, 6).map((l) => (
                <li key={l.licenseId}>
                  <span>{customerNames[l.customerId] || 'Customer #' + l.customerId} <span className="muted small">· #{l.licenseId}</span></span>
                  <span className={l.daysRemaining <= 30 ? 'small text-danger' : 'small text-warning'}>
                    {l.daysRemaining === 0 ? 'Today' : l.daysRemaining + ' days'}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card card-padded">
          <div className="card-title"><h2>Newest leads</h2><Link to="/leads">All leads →</Link></div>
          {newestLeads.length === 0 ? <p className="muted">No leads yet.</p> : (
            <ul className="mini-list">
              {newestLeads.map((l) => (
                <li key={l.leadId}>
                  <span>{l.leadName} <span className="muted small">{l.companyName}</span></span>
                  <Badge value={l.status} />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </>
  );
}
