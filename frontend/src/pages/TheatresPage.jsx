import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { formatTime, todayInputValue } from '../util/format.js';

export default function TheatresPage() {
  const [theatres, setTheatres] = useState([]);
  const [surgeries, setSurgeries] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .listTheatres()
      .then(setTheatres)
      .catch((err) => setError(err.message));
    api
      .listSurgeries({ date: todayInputValue() })
      .then(setSurgeries)
      .catch(() => {});
  }, []);

  function bookingsFor(theatreId) {
    return surgeries.filter((s) => s.theatreId === theatreId);
  }

  return (
    <section>
      <div className="page-header">
        <h1>Theatres</h1>
        <p>Occupancy per operation theatre for today.</p>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

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
            </div>
          );
        })}
      </div>
    </section>
  );
}
