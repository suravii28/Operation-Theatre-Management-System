import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import EmergenciesPage from '../pages/EmergenciesPage.jsx';

vi.mock('../api/client.js', () => ({
  api: {
    listEmergencies: vi.fn(),
    listTheatres: vi.fn(),
    surgeryImpact: vi.fn(),
    rescheduleSurgery: vi.fn(),
    updateStatus: vi.fn(),
  },
}));

import { api } from '../api/client.js';

const EMERGENCY = {
  id: 7,
  patientName: 'Lakshmi Rao',
  procedureName: 'Emergency laparotomy',
  theatreId: 1,
  theatreName: 'OT-1',
  leadSurgeonId: 4,
  leadSurgeonName: 'Dr. Priya Nair',
  startTime: '2030-01-10T14:00',
  endTime: '2030-01-10T16:00',
  status: 'SCHEDULED',
  emergency: true,
  notes: null,
};

const DISPLACED = {
  ...EMERGENCY,
  id: 3,
  patientName: 'Imran Khan',
  procedureName: 'Hip replacement',
  emergency: false,
  theatreName: 'OT-3',
};

const THEATRES = [
  { id: 1, name: 'OT-1', speciality: 'General Surgery', active: true },
  { id: 2, name: 'OT-2', speciality: 'Cardiac', active: true },
];

describe('EmergenciesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.listTheatres.mockResolvedValue(THEATRES);
  });

  it('shows the empty state when there are no emergencies', async () => {
    api.listEmergencies.mockResolvedValue([]);

    render(<EmergenciesPage />);

    expect(await screen.findByText('No active emergency cases.')).toBeInTheDocument();
  });

  it('lists affected surgeries for an emergency (TC-EMG-03)', async () => {
    api.listEmergencies.mockResolvedValue([EMERGENCY]);
    api.surgeryImpact.mockResolvedValue([DISPLACED]);

    const user = userEvent.setup();
    render(<EmergenciesPage />);

    await user.click(await screen.findByRole('button', { name: 'Impact' }));

    expect(await screen.findByText('Imran Khan')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Reschedule' })).toBeInTheDocument();
  });

  it('shows when an emergency affects nothing', async () => {
    api.listEmergencies.mockResolvedValue([EMERGENCY]);
    api.surgeryImpact.mockResolvedValue([]);

    const user = userEvent.setup();
    render(<EmergenciesPage />);

    await user.click(await screen.findByRole('button', { name: 'Impact' }));

    expect(await screen.findByText('No other surgeries are affected.')).toBeInTheDocument();
  });

  it('reschedules a displaced surgery through the API (TC-EMG-04)', async () => {
    api.listEmergencies.mockResolvedValue([EMERGENCY]);
    api.surgeryImpact.mockResolvedValue([DISPLACED]);
    api.rescheduleSurgery.mockResolvedValue({ ...DISPLACED, theatreId: 2, theatreName: 'OT-2' });

    const user = userEvent.setup();
    render(<EmergenciesPage />);

    await user.click(await screen.findByRole('button', { name: 'Impact' }));
    await user.click(await screen.findByRole('button', { name: 'Reschedule' }));
    await user.selectOptions(screen.getByLabelText(/new theatre/i), '2');
    await user.clear(screen.getByLabelText(/date/i));
    await user.type(screen.getByLabelText(/date/i), '2030-01-12');
    await user.click(screen.getByRole('button', { name: 'Reschedule' }));

    await waitFor(() => {
      expect(api.rescheduleSurgery).toHaveBeenCalledWith(3, {
        theatreId: 2,
        startTime: '2030-01-12T18:00',
        endTime: '2030-01-12T20:00',
      });
    });
  });

  it('shows an error when rescheduling hits a conflict', async () => {
    api.listEmergencies.mockResolvedValue([EMERGENCY]);
    api.surgeryImpact.mockResolvedValue([DISPLACED]);
    api.rescheduleSurgery.mockRejectedValue(new Error('Scheduling conflict detected: [OT-2 occupied]'));

    const user = userEvent.setup();
    render(<EmergenciesPage />);

    await user.click(await screen.findByRole('button', { name: 'Impact' }));
    await user.click(await screen.findByRole('button', { name: 'Reschedule' }));
    await user.click(screen.getByRole('button', { name: 'Reschedule' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Scheduling conflict detected');
  });
});
