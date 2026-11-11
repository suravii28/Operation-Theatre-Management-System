import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext.jsx';
import ReportsPage from '../pages/ReportsPage.jsx';

vi.mock('../api/client.js', () => ({
  getToken: vi.fn(() => localStorage.getItem('otms.token')),
  setToken: vi.fn(),
  clearToken: vi.fn(),
  parseToken: (token) => {
    try {
      const payload = token.split('.')[1];
      const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      return { email: json.sub, name: json.name, role: json.role };
    } catch {
      return null;
    }
  },
  api: {
    listSurgeries: vi.fn(),
  },
}));

import { api } from '../api/client.js';

function fakeToken(role) {
  const body = btoa(JSON.stringify({ sub: 'admin@otms.local', name: 'Asha Admin', role })).replace(/=/g, '');
  return `header.${body}.signature`;
}

function renderPage() {
  return render(
    <AuthProvider>
      <ReportsPage />
    </AuthProvider>,
  );
}

const SURGERIES = [
  {
    id: 1,
    patientName: 'Ravi Kumar',
    procedureName: 'Appendectomy',
    theatreName: 'OT-1',
    startTime: '2026-11-11T09:00:00',
    endTime: '2026-11-11T11:00:00',
    status: 'SCHEDULED',
    emergency: false,
  },
  {
    id: 2,
    patientName: 'Sunita Devi',
    procedureName: 'Angioplasty',
    theatreName: 'OT-1',
    startTime: '2026-11-11T13:00:00',
    endTime: '2026-11-11T14:30:00',
    status: 'IN_PROGRESS',
    emergency: false,
  },
  {
    id: 3,
    patientName: 'Lakshmi Rao',
    procedureName: 'Emergency laparotomy',
    theatreName: 'OT-2',
    startTime: '2026-11-11T10:00:00',
    endTime: '2026-11-11T12:00:00',
    status: 'SCHEDULED',
    emergency: true,
  },
];

describe('ReportsPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    localStorage.setItem('otms.token', fakeToken('ADMIN'));
  });

  it('aggregates utilisation per theatre and status (TC-REP-01)', async () => {
    api.listSurgeries.mockResolvedValue(SURGERIES);

    renderPage();

    expect(screen.getByRole('heading', { name: 'Reports' })).toBeInTheDocument();
    // OT-1: two surgeries, 2h + 1.5h = 3.5h; OT-2: one emergency, 2h.
    expect(await screen.findByText('2 · 3.5h')).toBeInTheDocument();
    expect(screen.getByText('1 · 2h')).toBeInTheDocument();
    expect(screen.getByText('SCHEDULED: 2')).toBeInTheDocument();
    expect(screen.getByText('IN PROGRESS: 1')).toBeInTheDocument();
  });

  it('shows the empty state when there are no surgeries', async () => {
    api.listSurgeries.mockResolvedValue([]);

    renderPage();

    expect(await screen.findByText('No data yet.')).toBeInTheDocument();
  });

  it('shows an error when the report request fails', async () => {
    api.listSurgeries.mockRejectedValue(new Error('Request failed (401)'));

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed (401)');
  });
});
