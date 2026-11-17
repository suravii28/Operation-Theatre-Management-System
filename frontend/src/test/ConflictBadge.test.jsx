import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import ConflictBadge from '../components/ConflictBadge.jsx';

describe('ConflictBadge', () => {
  it('renders a conflict label', () => {
    render(<ConflictBadge />);
    expect(screen.getByText('Conflict')).toBeInTheDocument();
  });
});
