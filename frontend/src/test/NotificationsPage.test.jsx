import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import NotificationsPage from '../pages/NotificationsPage.jsx';

vi.mock('../api/client.js', () => ({
  api: {
    listNotifications: vi.fn(),
    markNotificationRead: vi.fn(),
  },
}));

import { api } from '../api/client.js';

const NOTIFICATIONS = [
  {
    id: 1,
    type: 'EMERGENCY_SCHEDULED',
    message: "Emergency surgery scheduled: 'Laparotomy' for Lakshmi in OT-1",
    surgeryId: 7,
    createdAt: '2030-01-10T09:05',
    read: false,
  },
  {
    id: 2,
    type: 'SURGERY_RESCHEDULED',
    message: "Surgery 'Hip replacement' for Imran rescheduled to OT-3",
    surgeryId: 3,
    createdAt: '2030-01-10T09:10',
    read: true,
  },
];

describe('NotificationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows the empty state when there are no notifications', async () => {
    api.listNotifications.mockResolvedValue([]);

    render(<NotificationsPage />);

    expect(await screen.findByText(/no notifications yet/i)).toBeInTheDocument();
  });

  it('lists schedule-change and emergency notifications (TC-NOT-01, TC-NOT-02)', async () => {
    api.listNotifications.mockResolvedValue(NOTIFICATIONS);

    render(<NotificationsPage />);

    expect(await screen.findByText(/emergency surgery scheduled/i)).toBeInTheDocument();
    expect(screen.getByText(/rescheduled to OT-3/i)).toBeInTheDocument();
    expect(screen.getByText('1 unread')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: 'Mark read' })).toHaveLength(1);
  });

  it('marks a notification as read through the API', async () => {
    api.listNotifications.mockResolvedValue(NOTIFICATIONS);
    api.markNotificationRead.mockResolvedValue({ ...NOTIFICATIONS[0], read: true });

    const user = userEvent.setup();
    render(<NotificationsPage />);

    await user.click(await screen.findByRole('button', { name: 'Mark read' }));

    await waitFor(() => {
      expect(api.markNotificationRead).toHaveBeenCalledWith(1);
    });
  });

  it('shows an error when the notifications request fails', async () => {
    api.listNotifications.mockRejectedValue(new Error('Request failed (403)'));

    render(<NotificationsPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed (403)');
  });
});
