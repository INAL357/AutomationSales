// The frame around every logged-in page: sidebar menu on the left, page on the right.
// <Outlet /> is where React Router puts the current page.
import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { getCurrentUser, isAdmin, logout } from '../api';

// The menu. "group" starts a new section heading.
const MENU = [
  { to: '/', label: 'Dashboard', icon: '◧' },
  { to: '/leads', label: 'Leads', icon: '◎' },
  { to: '/customers', label: 'Customers', icon: '◉' },
  { to: '/tasks', label: 'Tasks', icon: '✓' },
  { to: '/calls', label: 'Calls', icon: '☏' },
  { to: '/licenses', label: 'Licences', icon: '⚿', group: 'Accounts' },
  { to: '/renewals', label: 'Renewals', icon: '↻' },
  { to: '/notifications', label: 'Notifications', icon: '✉' },
  { to: '/products', label: 'Products', icon: '▣', group: 'Catalogue' },
  { to: '/vendors', label: 'Vendors', icon: '⌂' },
  { to: '/documents', label: 'Knowledge base', icon: '☰' },
  { to: '/team', label: 'Team & logins', icon: '☺', group: 'Admin', adminOnly: true },
];

export default function Layout() {
  const user = getCurrentUser();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false); // for phones

  function handleLogout() {
    logout();
    navigate('/login');
  }

  const visibleMenu = MENU.filter((item) => !item.adminOnly || isAdmin());

  return (
    <div className={menuOpen ? 'layout menu-open' : 'layout'}>
      <aside className="sidebar">
        <div className="brand-logo"><span className="logo-mark">S</span> SalesSoftware</div>

        <nav className="menu">
          {visibleMenu.map((item) => (
            <div key={item.to}>
              {item.group && <div className="menu-group">{item.group}</div>}
              <NavLink
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) => (isActive ? 'menu-link active' : 'menu-link')}
                onClick={() => setMenuOpen(false)}
              >
                <span className="menu-icon" aria-hidden="true">{item.icon}</span>
                {item.label}
              </NavLink>
            </div>
          ))}
        </nav>

        <div className="sidebar-user">
          <div className="user-avatar">{user.fullName.charAt(0).toUpperCase()}</div>
          <div className="user-info">
            <strong>{user.fullName}</strong>
            <span>{user.role === 'ADMIN' ? 'Administrator' : 'Salesperson'}</span>
          </div>
          <button className="icon-button" onClick={handleLogout} title="Sign out" aria-label="Sign out">⏻</button>
        </div>
      </aside>

      {/* Dark background behind the open menu on phones; click it to close the menu */}
      {menuOpen && <div className="menu-backdrop" onClick={() => setMenuOpen(false)} />}

      <div className="content">
        <header className="topbar">
          <button className="icon-button" onClick={() => setMenuOpen(true)} aria-label="Open menu">☰</button>
          <span className="topbar-brand">SalesSoftware</span>
        </header>
        <main className="main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
