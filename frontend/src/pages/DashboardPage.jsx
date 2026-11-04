import { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client.js';
import ConflictBadge from '../components/ConflictBadge.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { findConflictingIds } from '../util/conflicts.js';
import { formatTime, todayInputValue } from '../util/format.js';

export default function DashboardPage() {
  const [surgeries, setSurgeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    api
      .listSurgeries({ date: todayInputValue() })
      .then((data) => {
        if (!cancelled) setSurgeries(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const conflictingIds = useMemo(() => findConflictingIds(surgeries), [surgeries]);
  const inProgress = surgeries.filter((s) => s.status === 'IN_PROGRESS').length;
  const emergencies = surgeries.filter((s) => s.emergency).length;

  return (
    <section>
      <div className="page-header">
        <h1>Dashboard</h1>
        <p>Live overview of today&apos;s operation theatre activity.</p>
      </div>

      <div className="stat-grid">
        <div className="card stat-card">
          <span className="stat-value">{surgeries.length}</span>
          <span className="stat-label">Surgeries today</span>
        </div>
        <div className="card stat-card">
          <span className="stat-value">{inProgress}</span>
          <span className="stat-label">In progress</span>
        </div>
        <div className="card stat-card">
          <span className="stat-value">{emergencies}</span>
          <span className="stat-label">Emergencies</span>
        </div>
        <div className="card stat-card">
          <span className="stat-value stat-danger">{conflictingIds.size}</span>
          <span className="stat-label">Conflicting slots</span>
        </div>
      </div>

      {loading ? <p>Loading…</p> : null}
      {error ? <p className="form-error" role="alert">{error}</p> : null}

      {!loading && !error ? (
        <div className="card table-card">
          <h3>Today&apos;s schedule</h3>
          {surgeries.length === 0 ? (
            <p>No surgeries scheduled today.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Patient</th>
                  <th>Procedure</th>
                  <th>Theatre</th>
                  <th>Lead surgeon</th>
                  <th>Status</th>
                  <th>Flags</th>
                </tr>
              </thead>
              <tbody>
                {surgeries.map((surgery) => (
                  <tr key={surgery.id} className={conflictingIds.has(surgery.id) ? 'row-conflict' : ''}>
                    <td>
                      {formatTime(surgery.startTime)} – {formatTime(surgery.endTime)}
                    </td>
                    <td>{surgery.patientName}</td>
                    <td>{surgery.procedureName}</td>
                    <td>{surgery.theatreName}</td>
                    <td>{surgery.leadSurgeonName}</td>
                    <td>
                      <StatusBadge status={surgery.status} />
                    </td>
                    <td>
                      {surgery.emergency ? <span className="badge badge-emergency">Emergency</span> : null}{' '}
                      {conflictingIds.has(surgery.id) ? <ConflictBadge /> : null}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ) : null}
    </section>
  );
}
