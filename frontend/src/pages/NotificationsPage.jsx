import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client.js';
import StatusBadge from '../components/StatusBadge.jsx';
import { formatDateTime } from '../util/format.js';

const TYPE_CLASS = {
  SURGERY_SCHEDULED: 'badge-scheduled',
  EMERGENCY_SCHEDULED: 'badge-conflict',
  SURGERY_RESCHEDULED: 'badge-progress',
  SURGERY_STATUS_CHANGED: 'badge-completed',
};

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    api
      .listNotifications()
      .then(setNotifications)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function markRead(id) {
    try {
      await api.markNotificationRead(id);
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  const unread = notifications.filter((n) => !n.read).length;

  return (
    <section>
      <div className="page-header">
        <h1>Notifications</h1>
        <p>Schedule changes, emergency cases and status updates across the theatre.</p>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      {loading ? (
        <div className="card">
          <p>Loading…</p>
        </div>
      ) : notifications.length === 0 && !error ? (
        <div className="card">
          <p>No notifications yet. Schedule a surgery to see activity here.</p>
        </div>
      ) : (
        <div className="card table-card">
          <p className="muted">{unread} unread</p>
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Message</th>
                <th>When</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {notifications.map((notification) => (
                <tr key={notification.id} className={notification.read ? 'muted-row' : ''}>
                  <td>
                    <span className={`badge ${TYPE_CLASS[notification.type] ?? 'badge-scheduled'}`}>
                      {notification.type.replace(/_/g, ' ').toLowerCase()}
                    </span>
                  </td>
                  <td>{notification.message}</td>
                  <td>{formatDateTime(notification.createdAt)}</td>
                  <td>
                    <StatusBadge status={notification.read ? 'COMPLETED' : 'SCHEDULED'} />
                  </td>
                  <td>
                    {notification.read ? null : (
                      <button
                        type="button"
                        className="btn btn-small"
                        onClick={() => markRead(notification.id)}
                      >
                        Mark read
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
