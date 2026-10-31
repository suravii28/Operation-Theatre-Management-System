import { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client.js';

function hoursBetween(start, end) {
  return Math.max(0, (new Date(end) - new Date(start)) / 3600000);
}

export default function ReportsPage() {
  const [surgeries, setSurgeries] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .listSurgeries({})
      .then(setSurgeries)
      .catch((err) => setError(err.message));
  }, []);

  const byTheatre = useMemo(() => {
    const map = new Map();
    for (const s of surgeries) {
      const entry = map.get(s.theatreName) ?? { count: 0, hours: 0 };
      entry.count += 1;
      entry.hours += hoursBetween(s.startTime, s.endTime);
      map.set(s.theatreName, entry);
    }
    return [...map.entries()];
  }, [surgeries]);

  const byStatus = useMemo(() => {
    const map = new Map();
    for (const s of surgeries) {
      map.set(s.status, (map.get(s.status) ?? 0) + 1);
    }
    return [...map.entries()];
  }, [surgeries]);

  const maxCount = Math.max(1, ...byTheatre.map(([, value]) => value.count));

  return (
    <section>
      <div className="page-header">
        <h1>Reports</h1>
        <p>Operation theatre utilisation across all scheduled surgeries.</p>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      <div className="stat-grid">
        <div className="card stat-card">
          <span className="stat-value">{surgeries.length}</span>
          <span className="stat-label">Total surgeries</span>
        </div>
        <div className="card stat-card">
          <span className="stat-value">{surgeries.filter((s) => s.emergency).length}</span>
          <span className="stat-label">Emergency cases</span>
        </div>
        <div className="card stat-card">
          <span className="stat-value">
            {byTheatre.length ? Math.round((byTheatre.reduce((sum, [, v]) => sum + v.hours, 0) / byTheatre.length) * 10) / 10 : 0}
          </span>
          <span className="stat-label">Avg. hours per theatre</span>
        </div>
      </div>

      <div className="card">
        <h3>Surgeries per theatre</h3>
        {byTheatre.length === 0 ? (
          <p>No data yet.</p>
        ) : (
          <div className="bar-chart">
            {byTheatre.map(([name, value]) => (
              <div key={name} className="bar-row">
                <span className="bar-label">{name}</span>
                <div className="bar-track">
                  <div className="bar-fill" style={{ width: `${(value.count / maxCount) * 100}%` }} />
                </div>
                <span className="bar-value">
                  {value.count} · {Math.round(value.hours * 10) / 10}h
                </span>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="card">
        <h3>By status</h3>
        <div className="chip-row">
          {byStatus.map(([status, count]) => (
            <span key={status} className="role-chip">
              {status.replace(/_/g, ' ')}: {count}
            </span>
          ))}
        </div>
      </div>
    </section>
  );
}
