
import { useCallback, useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { api } from '../api/client.js';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/surgeries', label: 'Surgeries' },
  { to: '/theatres', label: 'Theatres' },
  { to: '/emergencies', label: 'Emergencies' },
  { to: '/notifications', label: 'Notifications' },
  { to: '/reports', label: 'Reports' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [unreadCount, setUnreadCount] = useState(0);

  const loadUnreadCount = useCallback(() => {
    api.listNotifications()
      .then((notifications) => {
        setUnreadCount(notifications.filter((notification) => !notification.read).length);
      })
      .catch(() => {
        setUnreadCount(0);
      });
  }, []);

  useEffect(() => {
    loadUnreadCount();
  }, [loadUnreadCount, location.pathname]);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  function openNotifications() {
    navigate('/notifications');
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">OT</span>
          <div>
            <strong>OTMS</strong>
            <small>Operation Theatre Management</small>
          </div>
        </div>

        <nav className="nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main">
        <header className="topbar">
          <div>
            <strong>{user?.name}</strong>
            <span className="role-chip">{user?.role}</span>
          </div>

          <div className="topbar-actions">
            <button
              type="button"
              className="notification-bell"
              onClick={openNotifications}
              aria-label={`Notifications, ${unreadCount} unread`}
              title="Notifications"
            >
              <span aria-hidden="true">🔔</span>

              {unreadCount > 0 ? (
                <span className="notification-count">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              ) : null}
            </button>

            <button type="button" className="btn btn-ghost" onClick={handleLogout}>
              Sign out
            </button>
          </div>
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
