
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Layout from '../components/Layout.jsx';

const mockNavigate = vi.fn();
const mockLogout = vi.fn();

vi.mock('../auth/AuthContext.jsx', () => ({
  useAuth: () => ({
    user: { name: 'Kiran Coordinator', role: 'COORDINATOR' },
    logout: mockLogout,
  }),
}));

vi.mock('../api/client.js', () => ({
  api: {
    listNotifications: vi.fn(),
  },
}));

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

import { api } from '../api/client.js';

describe('Layout notification bell', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays the unread notification count', async () => {
    api.listNotifications.mockResolvedValue([
      { id: 1, read: false },
      { id: 2, read: false },
      { id: 3, read: true },
    ]);

    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    expect(await screen.findByText('2')).toBeInTheDocument();
    expect(screen.getByRole('button', {
      name: 'Notifications, 2 unread',
    })).toBeInTheDocument();
  });

  it('hides the unread badge when there are no unread notifications', async () => {
    api.listNotifications.mockResolvedValue([
      { id: 1, read: true },
    ]);

    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', {
        name: 'Notifications, 0 unread',
      })).toBeInTheDocument();
    });

    expect(screen.queryByText('1')).not.toBeInTheDocument();
  });

  it('navigates to notifications when the bell is clicked', async () => {
    api.listNotifications.mockResolvedValue([]);

    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    await user.click(screen.getByRole('button', {
      name: 'Notifications, 0 unread',
    }));

    expect(mockNavigate).toHaveBeenCalledWith('/notifications');
  });
});
