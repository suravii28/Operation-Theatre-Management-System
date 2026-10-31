import { describe, expect, it } from 'vitest';
import { findConflictingIds } from '../util/conflicts.js';

function surgery(overrides = {}) {
  return {
    id: 1,
    theatreId: 1,
    leadSurgeonId: 10,
    status: 'SCHEDULED',
    startTime: '2030-01-10T09:00',
    endTime: '2030-01-10T11:00',
    ...overrides,
  };
}

describe('findConflictingIds', () => {
  it('flags overlapping surgeries in the same theatre', () => {
    const a = surgery({ id: 1 });
    const b = surgery({ id: 2, startTime: '2030-01-10T10:00', endTime: '2030-01-10T12:00' });

    expect(findConflictingIds([a, b])).toEqual(new Set([1, 2]));
  });

  it('flags the same lead surgeon booked in different theatres', () => {
    const a = surgery({ id: 1 });
    const b = surgery({
      id: 2,
      theatreId: 2,
      startTime: '2030-01-10T10:00',
      endTime: '2030-01-10T12:00',
    });

    expect([...findConflictingIds([a, b])].sort()).toEqual([1, 2]);
  });

  it('ignores cancelled and completed surgeries', () => {
    const a = surgery({ id: 1 });
    const cancelled = surgery({
      id: 2,
      status: 'CANCELLED',
      startTime: '2030-01-10T10:00',
      endTime: '2030-01-10T12:00',
    });
    const completed = surgery({
      id: 3,
      status: 'COMPLETED',
      startTime: '2030-01-10T10:00',
      endTime: '2030-01-10T12:00',
    });

    expect(findConflictingIds([a, cancelled, completed]).size).toBe(0);
  });

  it('ignores non-overlapping surgeries with different resources', () => {
    const a = surgery({ id: 1 });
    const b = surgery({
      id: 2,
      theatreId: 2,
      leadSurgeonId: 11,
      startTime: '2030-01-10T11:00',
      endTime: '2030-01-10T13:00',
    });

    expect(findConflictingIds([a, b]).size).toBe(0);
  });

  it('treats back-to-back bookings as non-conflicting', () => {
    const a = surgery({ id: 1, endTime: '2030-01-10T11:00' });
    const b = surgery({ id: 2, startTime: '2030-01-10T11:00', endTime: '2030-01-10T13:00' });

    expect(findConflictingIds([a, b]).size).toBe(0);
  });
});
