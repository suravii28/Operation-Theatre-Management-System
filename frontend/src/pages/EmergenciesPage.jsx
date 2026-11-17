import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client.js';
import StatusBadge from '../components/StatusBadge.jsx';
import { formatDateTime, toApiDateTime, todayInputValue } from '../util/format.js';

function RescheduleForm({ surgery, theatres, onDone, onCancel }) {
  const [slot, setSlot] = useState({
    theatreId: surgery.theatreId,
    date: todayInputValue(),
    startTime: '18:00',
    endTime: '20:00',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (event) => setSlot((current) => ({ ...current, [field]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (slot.endTime <= slot.startTime) {
      setError('End time must be after start time.');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      await api.rescheduleSurgery(surgery.id, {
        theatreId: Number(slot.theatreId),
        startTime: toApiDateTime(slot.date, slot.startTime),
        endTime: toApiDateTime(slot.date, slot.endTime),
      });
      onDone();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="reschedule-form" onSubmit={handleSubmit}>
      <h4>Reschedule “{surgery.patientName}”</h4>
      <label>
        New theatre
        <select required value={slot.theatreId} onChange={update('theatreId')}>
          {theatres.map((theatre) => (
            <option key={theatre.id} value={theatre.id}>
              {theatre.name}
            </option>
          ))}
        </select>
      </label>
      <label>
        Date
        <input required type="date" value={slot.date} onChange={update('date')} />
      </label>
      <label>
        Start time
        <input required type="time" value={slot.startTime} onChange={update('startTime')} />
      </label>
      <label>
        End time
        <input required type="time" value={slot.endTime} onChange={update('endTime')} />
      </label>
      {error ? <p className="form-error" role="alert">{error}</p> : null}
      <div className="form-actions">
        <button type="submit" className="btn btn-small btn-primary" disabled={submitting}>
          {submitting ? 'Rescheduling…' : 'Reschedule'}
        </button>
        <button type="button" className="btn btn-small btn-ghost" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

export default function EmergenciesPage() {
  const [emergencies, setEmergencies] = useState([]);
  const [theatres, setTheatres] = useState([]);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [impactFor, setImpactFor] = useState(null);
  const [impact, setImpact] = useState([]);
  const [impactLoading, setImpactLoading] = useState(false);
  const [rescheduling, setRescheduling] = useState(null);

  const load = useCallback(() => {
    api.listEmergencies().then(setEmergencies).catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    load();
    api.listTheatres().then(setTheatres).catch(() => {});
  }, [load]);

  async function advance(surgery, status) {
    setActionError('');
    try {
      await api.updateStatus(surgery.id, status);
      load();
    } catch (err) {
      setActionError(err.message);
    }
  }

  async function showImpact(surgery) {
    setActionError('');
    setImpactFor(surgery.id);
    setRescheduling(null);
    setImpactLoading(true);
    try {
      setImpact(await api.surgeryImpact(surgery.id));
    } catch (err) {
      setActionError(err.message);
      setImpact([]);
    } finally {
      setImpactLoading(false);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1>Emergencies</h1>
        <p>Emergency cases take priority and can be moved through their lifecycle here.</p>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}
      {actionError ? <p className="banner-error" role="alert">{actionError}</p> : null}

      {emergencies.length === 0 && !error ? (
        <div className="card">
          <p>No active emergency cases.</p>
        </div>
      ) : (
        <div className="card table-card">
          <table>
            <thead>
              <tr>
                <th>Patient</th>
                <th>Procedure</th>
                <th>Theatre</th>
                <th>Lead surgeon</th>
                <th>Window</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {emergencies.map((surgery) => (
                <tr key={surgery.id}>
                  <td>{surgery.patientName}</td>
                  <td>{surgery.procedureName}</td>
                  <td>{surgery.theatreName}</td>
                  <td>{surgery.leadSurgeonName}</td>
                  <td>
                    {formatDateTime(surgery.startTime)} – {formatDateTime(surgery.endTime)}
                  </td>
                  <td>
                    <StatusBadge status={surgery.status} />
                  </td>
                  <td className="actions">
                    {surgery.status === 'SCHEDULED' ? (
                      <button type="button" className="btn btn-small" onClick={() => advance(surgery, 'IN_PROGRESS')}>
                        Start
                      </button>
                    ) : null}
                    {surgery.status === 'IN_PROGRESS' ? (
                      <button type="button" className="btn btn-small" onClick={() => advance(surgery, 'COMPLETED')}>
                        Complete
                      </button>
                    ) : null}
                    {surgery.status !== 'CANCELLED' ? (
                      <button
                        type="button"
                        className="btn btn-small btn-danger"
                        onClick={() => advance(surgery, 'CANCELLED')}
                      >
                        Cancel
                      </button>
                    ) : null}
                    <button type="button" className="btn btn-small" onClick={() => showImpact(surgery)}>
                      Impact
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {impactFor ? (
        <div className="card">
          <h3>Impact analysis</h3>
          <p className="muted">
            Non-emergency surgeries displaced by this emergency case (same theatre or lead surgeon, overlapping window).
          </p>
          {impactLoading ? (
            <p>Loading…</p>
          ) : impact.length === 0 ? (
            <p>No other surgeries are affected.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Patient</th>
                  <th>Procedure</th>
                  <th>Theatre</th>
                  <th>Lead surgeon</th>
                  <th>Window</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {impact.map((surgery) => (
                  <tr key={surgery.id}>
                    <td>{surgery.patientName}</td>
                    <td>{surgery.procedureName}</td>
                    <td>{surgery.theatreName}</td>
                    <td>{surgery.leadSurgeonName}</td>
                    <td>
                      {formatDateTime(surgery.startTime)} – {formatDateTime(surgery.endTime)}
                    </td>
                    <td>
                      {rescheduling === surgery.id ? null : (
                        <button
                          type="button"
                          className="btn btn-small btn-primary"
                          onClick={() => setRescheduling(surgery.id)}
                        >
                          Reschedule
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {impact.map((surgery) =>
            rescheduling === surgery.id ? (
              <RescheduleForm
                key={surgery.id}
                surgery={surgery}
                theatres={theatres}
                onDone={() => {
                  setRescheduling(null);
                  setImpactFor(null);
                  load();
                }}
                onCancel={() => setRescheduling(null)}
              />
            ) : null,
          )}
        </div>
      ) : null}
    </section>
  );
}
