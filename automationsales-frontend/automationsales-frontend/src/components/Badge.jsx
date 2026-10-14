// A coloured status pill, e.g. <Badge value="ACTIVE" />
import { niceLabel } from '../utils';

// Which colour each status gets (colours are defined in styles.css: .badge-green etc.)
const COLOURS = {
  NEW: 'blue', CONTACTED: 'purple', QUALIFIED: 'teal', UNQUALIFIED: 'grey', CONVERTED: 'green', LOST: 'red',
  OPEN: 'blue', IN_PROGRESS: 'amber', DONE: 'green', CANCELLED: 'grey',
  LOW: 'grey', MEDIUM: 'blue', HIGH: 'amber', URGENT: 'red',
  ACTIVE: 'green', INACTIVE: 'grey', EXPIRING_SOON: 'amber', EXPIRED: 'red', RENEWED: 'teal',
  PENDING: 'amber', SENT: 'green', FAILED: 'red',
  ADMIN: 'purple', SALESPERSON: 'teal',
  SCHEDULED: 'blue', COMPLETED: 'green', NO_ANSWER: 'amber',
};

export default function Badge({ value }) {
  if (!value) return <span className="muted">—</span>;
  const colour = COLOURS[value] || 'grey';
  return <span className={'badge badge-' + colour}>{niceLabel(value)}</span>;
}
