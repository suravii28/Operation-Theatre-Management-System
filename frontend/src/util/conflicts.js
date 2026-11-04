const ACTIVE_STATUSES = ['SCHEDULED', 'IN_PROGRESS'];

function overlaps(a, b) {
  return a.startTime < b.endTime && b.startTime < a.endTime;
}

/**
 * Returns the ids of surgeries involved in a scheduling conflict: overlapping
 * time ranges sharing the same theatre or the same lead surgeon. Cancelled and
 * completed surgeries never conflict.
 */
export function findConflictingIds(surgeries) {
  const active = surgeries.filter((s) => ACTIVE_STATUSES.includes(s.status));
  const ids = new Set();

  for (let i = 0; i < active.length; i += 1) {
    for (let j = i + 1; j < active.length; j += 1) {
      const a = active[i];
      const b = active[j];
      const sameResource = a.theatreId === b.theatreId || a.leadSurgeonId === b.leadSurgeonId;
      if (sameResource && overlaps(a, b)) {
        ids.add(a.id);
        ids.add(b.id);
      }
    }
  }

  return ids;
}
