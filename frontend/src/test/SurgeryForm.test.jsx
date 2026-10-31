import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import SurgeryForm from '../components/SurgeryForm.jsx';

const THEATRES = [{ id: 1, name: 'OT-1', speciality: 'General Surgery' }];
const DOCTORS = [{ id: 10, name: 'Dr. Ram Sharma' }];

function renderForm(onSubmit = vi.fn()) {
  return {
    onSubmit,
    ...render(
      <SurgeryForm theatres={THEATRES} doctors={DOCTORS} onSubmit={onSubmit} submitting={false} />,
    ),
  };
}

describe('SurgeryForm', () => {
  it('submits the scheduling payload with combined date and time', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();

    await user.type(screen.getByLabelText(/patient name/i), 'Ravi Kumar');
    await user.type(screen.getByLabelText(/procedure/i), 'Appendectomy');
    await user.selectOptions(screen.getByLabelText(/lead surgeon/i), '10');
    await user.selectOptions(screen.getByLabelText(/theatre/i), '1');
    await user.clear(screen.getByLabelText(/date/i));
    await user.type(screen.getByLabelText(/date/i), '2030-01-10');
    await user.clear(screen.getByLabelText(/start time/i));
    await user.type(screen.getByLabelText(/start time/i), '09:00');
    await user.clear(screen.getByLabelText(/end time/i));
    await user.type(screen.getByLabelText(/end time/i), '11:00');
    await user.click(screen.getByRole('button', { name: /schedule surgery/i }));

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          patientName: 'Ravi Kumar',
          procedureName: 'Appendectomy',
          leadSurgeonId: 10,
          theatreId: 1,
          startTime: '2030-01-10T09:00',
          endTime: '2030-01-10T11:00',
          emergency: false,
        }),
      );
    });
  });

  it('blocks submission when end time is before start time', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();

    await user.type(screen.getByLabelText(/patient name/i), 'Ravi Kumar');
    await user.type(screen.getByLabelText(/procedure/i), 'Appendectomy');
    await user.selectOptions(screen.getByLabelText(/lead surgeon/i), '10');
    await user.selectOptions(screen.getByLabelText(/theatre/i), '1');
    await user.clear(screen.getByLabelText(/start time/i));
    await user.type(screen.getByLabelText(/start time/i), '11:00');
    await user.clear(screen.getByLabelText(/end time/i));
    await user.type(screen.getByLabelText(/end time/i), '09:00');
    await user.click(screen.getByRole('button', { name: /schedule surgery/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('End time must be after start time');
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
