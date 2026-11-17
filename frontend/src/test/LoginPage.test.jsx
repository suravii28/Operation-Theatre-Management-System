import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext.jsx';
import LoginPage from '../pages/LoginPage.jsx';

function fakeToken(payload) {
  const body = btoa(JSON.stringify(payload)).replace(/=/g, '');
  return `header.${body}.signature`;
}

function mockFetch({ ok, status, body }) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue({
      ok,
      status,
      text: async () => JSON.stringify(body),
    }),
  );
}

function renderLogin() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores the token and shows the user name after a successful login', async () => {
    mockFetch({
      ok: true,
      status: 200,
      body: { token: fakeToken({ sub: 'admin@otms.local', name: 'Asha Admin', role: 'ADMIN' }) },
    });
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByLabelText(/email/i), 'admin@otms.local');
    await user.type(screen.getByLabelText(/password/i), 'Admin123!');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(localStorage.getItem('otms.token')).toContain('header.');
    });
  });

  it('shows an error message when credentials are rejected', async () => {
    mockFetch({ ok: false, status: 401, body: { message: 'Invalid email or password' } });
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByLabelText(/email/i), 'admin@otms.local');
    await user.type(screen.getByLabelText(/password/i), 'wrong');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password');
    expect(localStorage.getItem('otms.token')).toBeNull();
  });
});
