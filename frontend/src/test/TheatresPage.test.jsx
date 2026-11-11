import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext.jsx';
import TheatresPage from '../pages/TheatresPage.jsx';

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
    listTheatres: vi.fn(),
    setTheatreAvailability: vi.fn(),
    listSurgeries: vi.fn(),
  },
}));

import { api } from '../api/client.js';

function fakeToken(role) {
  const body = btoa(JSON.stringify({ sub: 'tech@otms.local', name: 'Arun Technician', role })).replace(/=/g, '');
  return `header.${body}.signature`;
}

function renderPage() {
  return render(
    <AuthProvider>
      <TheatresPage />
    </AuthProvider>,
  );
}

const THEATRES = [
  { id: 1, name: 'OT-1', speciality: 'General Surgery', active: true },
  { id: 2, name: 'OT-2', speciality: 'Cardiac', active: false },
];

describe('TheatresPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    api.listSurgeries.mockResolvedValue([]);
  });

  it('shows the availability toggle to technicians and lists inactive theatres', async () => {
    localStorage.setItem('otms.token', fakeToken('TECHNICIAN'));
    api.listTheatres.mockResolvedValue(THEATRES);

    renderPage();

    expect(await screen.findByText('OT-2')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Mark unavailable' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Mark available' })).toBeInTheDocument();
  });

  it('calls the availability endpoint when toggling', async () => {
    localStorage.setItem('otms.token', fakeToken('TECHNICIAN'));
    api.listTheatres.mockResolvedValue(THEATRES);
    api.setTheatreAvailability.mockResolvedValue({ ...THEATRES[0], active: false });

    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Mark unavailable' }));

    await waitFor(() => {
      expect(api.setTheatreAvailability).toHaveBeenCalledWith(1, false);
    });
  });

  it('hides the toggle for nurses and requests active theatres only', async () => {
    localStorage.setItem('otms.token', fakeToken('NURSE'));
    api.listTheatres.mockResolvedValue(THEATRES.filter((t) => t.active));

    renderPage();

    expect(await screen.findByText('OT-1')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /mark/i })).not.toBeInTheDocument();
    await waitFor(() => {
      expect(api.listTheatres).toHaveBeenCalledWith();
    });
  });

  it('shows an error when toggling fails', async () => {
    localStorage.setItem('otms.token', fakeToken('TECHNICIAN'));
    api.listTheatres.mockResolvedValue(THEATRES);
    api.setTheatreAvailability.mockRejectedValue(new Error('Access denied for your role'));

    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Mark unavailable' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Access denied for your role');
  });
});
