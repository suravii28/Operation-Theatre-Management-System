
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    concurrent_booking: {
      executor: 'per-vu-iterations',
      vus: 2,
      iterations: 1,
      maxDuration: '30s',
      startTime: '0s',
    },
  },
  thresholds: {
    checks: ['rate==1'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EMAIL = __ENV.EMAIL || 'admin@otms.local';
const PASSWORD = __ENV.PASSWORD || 'Admin123!';

export function setup() {
  const login = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: EMAIL,
      password: PASSWORD,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  if (login.status !== 200) {
    throw new Error(`Login failed: HTTP ${login.status}`);
  }

  return { token: login.json('token') };
}

export default function (data) {
  const payload = JSON.stringify({
    patientName: `Concurrent Patient ${__VU}`,
    procedureName: 'Concurrency Verification',
    leadSurgeonId: 3,
    theatreId: 1,
    startTime: '2030-05-15T10:00',
    endTime: '2030-05-15T11:00',
    emergency: false,
    notes: 'TC-Conc-01',
  });

  const response = http.post(
    `${BASE_URL}/api/surgeries`,
    payload,
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${data.token}`,
      },
    }
  );

  console.log(`VU ${__VU}: HTTP ${response.status}`);

  check(response, {
    'booking succeeded or conflicted': (r) =>
      r.status === 201 || r.status === 409,
  });
}

export function handleSummary(data) {
  return {
    'docs/reports/concurrency-summary.json':
      JSON.stringify(data, null, 2),
  };
}
