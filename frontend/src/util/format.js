export function formatTime(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function formatDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString([], {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  });
}

export function formatDateTime(iso) {
  return `${formatDate(iso)} · ${formatTime(iso)}`;
}

export function todayInputValue() {
  const now = new Date();
  const offset = now.getTimezoneOffset();
  return new Date(now.getTime() - offset * 60000).toISOString().slice(0, 10);
}

export function toApiDateTime(dateValue, timeValue) {
  return `${dateValue}T${timeValue}`;
}
