import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client.js';
import StatusBadge from '../components/StatusBadge.jsx';
import { formatDateTime } from '../util/format.js';

export default function EmergenciesPage() {
  const [emergencies, setEmergencies] = useState([]);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  const load = useCallback(() => {
    api.listEmergencies().then(setEmergencies).catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    load();
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
