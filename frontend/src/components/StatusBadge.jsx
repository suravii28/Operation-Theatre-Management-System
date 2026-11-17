const STATUS_CLASS = {
  SCHEDULED: 'badge-scheduled',
  IN_PROGRESS: 'badge-progress',
  COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled',
};

export default function StatusBadge({ status }) {
  const label = status?.replace(/_/g, ' ') ?? '';
  return <span className={`badge ${STATUS_CLASS[status] ?? 'badge-scheduled'}`}>{label}</span>;
}
