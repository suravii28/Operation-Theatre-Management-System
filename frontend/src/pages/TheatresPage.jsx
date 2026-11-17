import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext.jsx';
import { api } from '../api/client.js';
import { formatTime, todayInputValue } from '../util/format.js';

const CAN_MANAGE_AVAILABILITY = ['ADMIN', 'TECHNICIAN'];

export default function TheatresPage() {
  const { user } = useAuth();
  const canManage = CAN_MANAGE_AVAILABILITY.includes(user?.role);
  const [theatres, setTheatres] = useState([]);
  const [surgeries, setSurgeries] = useState([]);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  const load = useCallback(() => {
    const request = canManage ? api.listTheatres(true) : api.listTheatres();
    request
      .then(setTheatres)
      .catch((err) => setError(err.message));
    api
      .listSurgeries({ date: todayInputValue() })
      .then(setSurgeries)
      .catch(() => {});
  }, [canManage]);

  useEffect(() => {
    load();
  }, [load]);

  function bookingsFor(theatreId) {
    return surgeries.filter((s) => s.theatreId === theatreId);
  }

  async function toggleAvailability(theatre) {
    setActionError('');
    try {
      await api.setTheatreAvailability(theatre.id, !theatre.active);
      load();
    } catch (err) {
      setActionError(err.message);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1>Theatres</h1>
        <p>Occupancy per operation theatre for today.</p>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}
      {actionError ? <p className="banner-error" role="alert">{actionError}</p> : null}

      <div className="card-grid">
        {theatres.map((theatre) => {
          const bookings = bookingsFor(theatre.id);
          return (
            <div key={theatre.id} className="card theatre-card">
              <div className="theatre-card-header">
                <h3>{theatre.name}</h3>
                <span className="role-chip">{theatre.speciality}</span>
              </div>
              <p>
                {bookings.length} booking{bookings.length === 1 ? '' : 's'} today
              </p>
              {bookings.length === 0 ? (
                <p className="muted">Available all day.</p>
              ) : (
                <ul className="booking-list">
                  {bookings.map((surgery) => (
                    <li key={surgery.id}>
                      <span>
                        {formatTime(surgery.startTime)} – {formatTime(surgery.endTime)}
                      </span>
                      <span>{surgery.procedureName}</span>
                    </li>
                  ))}
                </ul>
              )}
              {canManage ? (
                <div className="theatre-actions">
                  <span className={theatre.active ? 'badge badge-scheduled' : 'badge badge-cancelled'}>
                    {theatre.active ? 'Available' : 'Unavailable'}
                  </span>
                  <button
                    type="button"
                    className={`btn btn-small ${theatre.active ? 'btn-danger' : ''}`}
                    onClick={() => toggleAvailability(theatre)}
                  >
                    {theatre.active ? 'Mark unavailable' : 'Mark available'}
                  </button>
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
    </section>
  );
}
