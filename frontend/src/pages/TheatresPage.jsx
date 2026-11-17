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
  const [selectedDate, setSelectedDate] = useState(todayInputValue);
  const [bookingsLoading, setBookingsLoading] = useState(true);
  const [bookingsError, setBookingsError] = useState('');
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  const load = useCallback(() => {
    const request = canManage ? api.listTheatres(true) : api.listTheatres();
    request
      .then(setTheatres)
      .catch((err) => setError(err.message));
  }, [canManage]);

  useEffect(() => {
    let active = true;
    api.listSurgeries({ date: selectedDate })
      .then((data) => { if (active) { setSurgeries(data); setBookingsError(''); } })
      .catch((err) => { if (active) { setSurgeries([]); setBookingsError(err.message); } })
      .finally(() => { if (active) setBookingsLoading(false); });
    return () => { active = false; };
  }, [selectedDate]);

  function changeDate(event) {
    setSelectedDate(event.target.value);
    setBookingsLoading(true);
    setBookingsError('');
  }

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
        <p>View bookings and occupancy for a selected date.</p>
      </div>

      <div className="card">
        <label htmlFor="theatre-booking-date">Booking date</label>
        <input id="theatre-booking-date" type="date" value={selectedDate} onChange={changeDate} />
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}
      {bookingsError ? <p className="form-error" role="alert">Could not load bookings: {bookingsError}</p> : null}
      {bookingsLoading ? <p role="status">Loading bookings…</p> : null}
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
                {bookingsLoading ? 'Loading bookings…' : `${bookings.length} booking${bookings.length === 1 ? '' : 's'} on selected date`}
              </p>
              {bookingsLoading || bookingsError ? null : bookings.length === 0 ? (
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
