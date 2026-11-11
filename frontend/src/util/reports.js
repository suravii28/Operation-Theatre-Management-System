export function filterSurgeriesByDateRange(surgeries, fromDate, toDate) {
  return surgeries.filter((surgery) => {
    const date = surgery.startTime?.slice(0, 10);
    return (!fromDate || date >= fromDate) && (!toDate || date <= toDate);
  });
}

export function buildTheatreUtilisation(surgeries) {
  const totals = new Map();
  for (const surgery of surgeries) {
    const current = totals.get(surgery.theatreName) ?? { theatreName: surgery.theatreName, surgeryCount: 0, hours: 0 };
    current.surgeryCount += 1;
    current.hours += Math.max(0, (new Date(surgery.endTime) - new Date(surgery.startTime)) / 3600000);
    totals.set(surgery.theatreName, current);
  }
  return [...totals.values()].sort((a, b) => a.theatreName.localeCompare(b.theatreName));
}

function csvCell(value) {
  const text = value == null ? '' : String(value);
  return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}

export function toCsv(headers, rows) {
  return [headers, ...rows].map((row) => row.map(csvCell).join(',')).join('\r\n') + '\r\n';
}

export function theatreUtilisationCsv(surgeries) {
  const rows = buildTheatreUtilisation(surgeries).map((item) => [
    item.theatreName, item.surgeryCount, item.hours.toFixed(2),
  ]);
  return toCsv(['Theatre', 'Surgery count', 'Scheduled hours'], rows);
}

export function dailySurgeryCsv(surgeries) {
  const rows = [...surgeries]
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
    .map((s) => [
      s.startTime?.slice(0, 10), s.patientName, s.procedureName, s.theatreName,
      s.leadSurgeonName, s.startTime, s.endTime, s.status, s.emergency ? 'Yes' : 'No',
    ]);
  return toCsv(['Date', 'Patient', 'Procedure', 'Theatre', 'Lead surgeon', 'Start', 'End', 'Status', 'Emergency'], rows);
}

export function downloadCsv(filename, content) {
  const blob = new Blob(['\uFEFF', content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}
