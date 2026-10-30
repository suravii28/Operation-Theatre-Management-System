import { useCallback, useEffect, useMemo, useState } from 'react';
import { api } from '../api/client.js';
import ConflictBadge from '../components/ConflictBadge.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import SurgeryForm from '../components/SurgeryForm.jsx';
import { findConflictingIds } from '../util/conflicts.js';
import { formatDateTime, todayInputValue } from '../util/format.js';

export default function SurgeriesPage() {
  const [filters, setFilters] = useState({ date: todayInputValue(), theatreId: '', status: '' });
  const [surgeries, setSurgeries] = useState([]);
  const [theatres, setTheatres] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    api
      .listSurgeries({ date: filters.date || undefined, theatreId: filters.theatreId || undefined, status: filters.status || undefined })
      .then(setSurgeries)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [filters]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    api.listTheatres().then(setTheatres).catch(() => {});
    api.listDoctors().then(setDoctors).catch(() => {});
  }, []);

  const conflictingIds = useMemo(() => findConflictingIds(surgeries), [surgeries]);

  async function handleCreate(payload) {
    setSubmitting(true);
    setFormError('');
    try {
      await api.createSurgery(payload);
      setShowForm(false);
      load();
    } catch (err) {
      setFormError(err.status === 409 ? err.message : err.message || 'Failed to schedule surgery.');
    } finally {
      setSubmitting(false);
    }
  }

  function updateFilter(field) {
    return (event) => setFilters((current) => ({ ...current, [field]: event.target.value }));
  }

  return (
    <section>
      <div className="page-header">
        <h1>Surgeries</h1>
        <p>View and schedule operations. Overlapping bookings are flagged automatically.</p>
      </div>

      <div className="toolbar">
        <label>
          Date
          <input type="date" value={filters.date} onChange={updateFilter('date')} />
        </label>
        <label>
          Theatre
          <select value={filters.theatreId} onChange={updateFilter('theatreId')}>
            <option value="">All theatres</option>
            {theatres.map((theatre) => (
              <option key={theatre.id} value={theatre.id}>
                {theatre.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Status
          <select value={filters.status} onChange={updateFilter('status')}>
            <option value="">All statuses</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </label>
        <button type="button" className="btn btn-primary" onClick={() => setShowForm((v) => !v)}>
          {showForm ? 'Close form' : 'Schedule surgery'}
        </button>
      </div>

      {showForm ? (
        <SurgeryForm theatres={theatres} doctors={doctors} onSubmit={handleCreate} submitting={submitting} />
      ) : null}
      {formError ? <p className="banner-error" role="alert">{formError}</p> : null}

      {loading ? <p>Loading…</p> : null}
      {error ? <p className="form-error" role="alert">{error}</p> : null}

      {!loading && !error ? (
        <div className="card table-card">
          {surgeries.length === 0 ? (
            <p>No surgeries match the selected filters.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Start</th>
                  <th>End</th>
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
                    <td>{formatDateTime(surgery.startTime)}</td>
                    <td>{formatDateTime(surgery.endTime)}</td>
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
