import { describe, expect, it } from 'vitest';
import {
  buildTheatreUtilisation, dailySurgeryCsv, filterSurgeriesByDateRange,
  theatreUtilisationCsv, toCsv,
} from '../util/reports.js';

const surgeries = [
  { startTime: '2030-01-02T09:00', endTime: '2030-01-02T11:00', theatreName: 'OT-1', patientName: 'A, "B"', procedureName: 'Scope', leadSurgeonName: 'Dr A', status: 'SCHEDULED', emergency: false },
  { startTime: '2030-01-03T09:00', endTime: '2030-01-03T10:30', theatreName: 'OT-1', patientName: 'C', procedureName: 'Repair', leadSurgeonName: 'Dr B', status: 'COMPLETED', emergency: true },
  { startTime: '2030-01-03T10:00', endTime: '2030-01-03T11:00', theatreName: 'OT-2', patientName: 'D', procedureName: 'Biopsy', leadSurgeonName: 'Dr C', status: 'SCHEDULED', emergency: false },
];

describe('report utilities', () => {
  it('filters by inclusive date range', () => {
    expect(filterSurgeriesByDateRange(surgeries, '2030-01-03', '2030-01-03')).toHaveLength(2);
    expect(filterSurgeriesByDateRange(surgeries, '', '2030-01-02')).toHaveLength(1);
  });

  it('aggregates surgery counts and theatre hours', () => {
    expect(buildTheatreUtilisation(surgeries)).toEqual([
      { theatreName: 'OT-1', surgeryCount: 2, hours: 3.5 },
      { theatreName: 'OT-2', surgeryCount: 1, hours: 1 },
    ]);
  });

  it('escapes commas and quotes in CSV cells', () => {
    expect(toCsv(['Patient'], [['A, "B"']])).toBe('Patient\r\n"A, ""B"""\r\n');
  });

  it('exports utilisation and daily surgery CSV headers and rows', () => {
    expect(theatreUtilisationCsv(surgeries)).toContain('Theatre,Surgery count,Scheduled hours');
    expect(dailySurgeryCsv(surgeries)).toContain('Date,Patient,Procedure,Theatre,Lead surgeon,Start,End,Status,Emergency');
    expect(dailySurgeryCsv(surgeries)).toContain('"A, ""B"""');
  });
});
