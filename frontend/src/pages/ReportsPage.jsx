import { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client.js';
import {
  buildTheatreUtilisation, dailySurgeryCsv, downloadCsv, filterSurgeriesByDateRange,
  theatreUtilisationCsv,
} from '../util/reports.js';

function hoursBetween(start, end) {
  return Math.max(0, (new Date(end) - new Date(start)) / 3600000);
}

export default function ReportsPage() {
  const [surgeries, setSurgeries] = useState([]);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.listSurgeries({})
      .then(setSurgeries)
      .catch((err) => setError(err.message));
  }, []);

  const filteredSurgeries = useMemo(
    () => filterSurgeriesByDateRange(surgeries, fromDate, toDate),
    [surgeries, fromDate, toDate],
  );
  const byTheatre = useMemo(() => buildTheatreUtilisation(filteredSurgeries), [filteredSurgeries]);
  const byStatus = useMemo(() => {
    const map = new Map();
    for (const surgery of filteredSurgeries) map.set(surgery.status, (map.get(surgery.status) ?? 0) + 1);
    return [...map.entries()];
  }, [filteredSurgeries]);
  const maxCount = Math.max(1, ...byTheatre.map((item) => item.surgeryCount));

  function printReport() {
    window.print();
  }

  return (
    <section>
      <div className="page-header">
        <h1>Reports</h1>
        <p>Operation theatre utilisation across surgeries in the selected date range.</p>
      </div>

      <div className="toolbar no-print" aria-label="Report filters">
        <label>
          From date
          <input type="date" value={fromDate} max={toDate || undefined}
            onChange={(event) => setFromDate(event.target.value)} />
        </label>
        <label>
          To date
          <input type="date" value={toDate} min={fromDate || undefined}
            onChange={(event) => setToDate(event.target.value)} />
        </label>
        <button type="button" className="btn" onClick={() => { setFromDate(''); setToDate(''); }}>
          Clear dates
        </button>
        <button type="button" className="btn btn-primary"
          onClick={() => downloadCsv('otms-theatre-utilisation.csv', theatreUtilisationCsv(filteredSurgeries))}>
          Export theatre utilisation CSV
        </button>
        <button type="button" className="btn btn-primary"
          onClick={() => downloadCsv('otms-daily-surgery-list.csv', dailySurgeryCsv(filteredSurgeries))}>
          Export surgery list CSV
        </button>
        <button type="button" className="btn" onClick={printReport}>Print report</button>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      <div className="reports-print-area">
        <p className="muted report-period">
          Date range: {fromDate || 'All dates'} to {toDate || 'All dates'}
        </p>
        <div className="stat-grid">
          <div className="card stat-card">
            <span className="stat-value">{filteredSurgeries.length}</span>
            <span className="stat-label">Total surgeries</span>
          </div>
          <div className="card stat-card">
            <span className="stat-value">{filteredSurgeries.filter((s) => s.emergency).length}</span>
            <span className="stat-label">Emergency cases</span>
          </div>
          <div className="card stat-card">
            <span className="stat-value">
              {byTheatre.length ? Math.round((byTheatre.reduce((sum, item) => sum + item.hours, 0) / byTheatre.length) * 10) / 10 : 0}
            </span>
            <span className="stat-label">Avg. hours per theatre</span>
          </div>
        </div>

        <div className="card">
          <h3>Surgeries per theatre</h3>
          {byTheatre.length === 0 ? <p>No data for the selected date range.</p> : (
            <div className="bar-chart">
              {byTheatre.map((item) => (
                <div key={item.theatreName} className="bar-row">
                  <span className="bar-label">{item.theatreName}</span>
                  <div className="bar-track">
                    <div className="bar-fill" style={{ width: `${(item.surgeryCount / maxCount) * 100}%` }} />
                  </div>
                  <span className="bar-value">{item.surgeryCount} · {Math.round(item.hours * 10) / 10}h</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <h3>By status</h3>
          <div className="chip-row">
            {byStatus.map(([status, count]) => (
              <span key={status} className="role-chip">{status.replace(/_/g, ' ')}: {count}</span>
            ))}
          </div>
        </div>

        <div className="card table-card">
          <h3>Surgery list</h3>
          {filteredSurgeries.length === 0 ? <p>No surgeries for the selected date range.</p> : (
            <table>
              <thead><tr><th>Date</th><th>Patient</th><th>Procedure</th><th>Theatre</th><th>Surgeon</th><th>Start</th><th>End</th><th>Status</th></tr></thead>
              <tbody>
                {[...filteredSurgeries].sort((a, b) => a.startTime.localeCompare(b.startTime)).map((s) => (
                  <tr key={s.id}>
                    <td>{s.startTime.slice(0, 10)}</td><td>{s.patientName}</td><td>{s.procedureName}</td>
                    <td>{s.theatreName}</td><td>{s.leadSurgeonName}</td><td>{s.startTime}</td>
                    <td>{s.endTime}</td><td>{s.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </section>
  );
}
