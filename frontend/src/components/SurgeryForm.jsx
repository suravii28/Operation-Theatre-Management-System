import { useState } from 'react';
import { toApiDateTime, todayInputValue } from '../util/format.js';

const EMPTY = {
  patientName: '',
  procedureName: '',
  leadSurgeonId: '',
  theatreId: '',
  date: todayInputValue(),
  startTime: '09:00',
  endTime: '11:00',
  emergency: false,
  notes: '',
};

export default function SurgeryForm({ theatres, doctors, onSubmit, submitting }) {
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');

  function update(field) {
    return (event) => {
      const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
      setForm((current) => ({ ...current, [field]: value }));
    };
  }

  function handleSubmit(event) {
    event.preventDefault();
    if (form.endTime <= form.startTime) {
      setError('End time must be after start time.');
      return;
    }
    setError('');
    onSubmit({
      patientName: form.patientName.trim(),
      procedureName: form.procedureName.trim(),
      leadSurgeonId: Number(form.leadSurgeonId),
      theatreId: Number(form.theatreId),
      startTime: toApiDateTime(form.date, form.startTime),
      endTime: toApiDateTime(form.date, form.endTime),
      emergency: form.emergency,
      notes: form.notes.trim() || null,
    });
  }

  return (
    <form className="card form-grid" onSubmit={handleSubmit}>
      <h3>Schedule a surgery</h3>

      <label>
        Patient name
        <input
          required
          value={form.patientName}
          onChange={update('patientName')}
          placeholder="e.g. Ravi Kumar"
        />
      </label>

      <label>
        Procedure
        <input
          required
          value={form.procedureName}
          onChange={update('procedureName')}
          placeholder="e.g. Appendectomy"
        />
      </label>

      <label>
        Lead surgeon
        <select required value={form.leadSurgeonId} onChange={update('leadSurgeonId')}>
          <option value="">Select surgeon</option>
          {doctors.map((doctor) => (
            <option key={doctor.id} value={doctor.id}>
              {doctor.name}
            </option>
          ))}
        </select>
      </label>

      <label>
        Theatre
        <select required value={form.theatreId} onChange={update('theatreId')}>
          <option value="">Select theatre</option>
          {theatres.map((theatre) => (
            <option key={theatre.id} value={theatre.id}>
              {theatre.name} · {theatre.speciality}
            </option>
          ))}
        </select>
      </label>

      <label>
        Date
        <input required type="date" value={form.date} onChange={update('date')} />
      </label>

      <label>
        Start time
        <input required type="time" value={form.startTime} onChange={update('startTime')} />
      </label>

      <label>
        End time
        <input required type="time" value={form.endTime} onChange={update('endTime')} />
      </label>

      <label>
        Notes
        <input value={form.notes} onChange={update('notes')} placeholder="Optional" />
      </label>

      <label className="checkbox">
        <input type="checkbox" checked={form.emergency} onChange={update('emergency')} />
        Emergency case (prioritise)
      </label>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Scheduling…' : 'Schedule surgery'}
        </button>
      </div>
    </form>
  );
}
