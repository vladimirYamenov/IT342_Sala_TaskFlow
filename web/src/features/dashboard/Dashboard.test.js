import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Dashboard from './Dashboard';
import { taskApi, groupApi } from '../../shared/api';

jest.mock('../../shared/api', () => ({
  taskApi: { list: jest.fn() },
  groupApi: { list: jest.fn() },
}));

const mockUser = { id: 1, fullName: 'Juan Dela Cruz', email: 'juan@example.com' };

function renderDashboard(user = mockUser) {
  return render(
    <MemoryRouter>
      <Dashboard user={user} />
    </MemoryRouter>
  );
}

describe('TC-W11 | Dashboard — Loading State', () => {
  test('shows loading indicator before data arrives', () => {
    taskApi.list.mockReturnValue(new Promise(() => {}));
    groupApi.list.mockReturnValue(new Promise(() => {}));
    renderDashboard();
    expect(screen.getByText(/loading dashboard/i)).toBeInTheDocument();
  });
});

describe('TC-W12 | Dashboard — Task Statistics', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('displays correct task counts after data loads', async () => {
    taskApi.list.mockResolvedValueOnce([
      { id: 1, title: 'Task A', status: 'COMPLETED', priority: 'HIGH', createdAt: new Date().toISOString() },
      { id: 2, title: 'Task B', status: 'IN_PROGRESS', priority: 'MEDIUM', createdAt: new Date().toISOString() },
      { id: 3, title: 'Task C', status: 'TODO', priority: 'LOW', createdAt: new Date().toISOString() },
    ]);
    groupApi.list.mockResolvedValueOnce([
      { id: 1, name: 'Group Alpha' },
    ]);

    renderDashboard();

    await waitFor(() => {
      expect(screen.queryByText(/loading dashboard/i)).not.toBeInTheDocument();
    });

    // Total tasks stat shows 3
    expect(screen.getByText('3')).toBeInTheDocument();
    // Task labels are visible
    expect(screen.getByText('Total Tasks')).toBeInTheDocument();
    expect(screen.getByText('Completed')).toBeInTheDocument();
    // Recent task names appear in list
    expect(screen.getByText('Task A')).toBeInTheDocument();
  });

  test('displays group names after data loads', async () => {
    taskApi.list.mockResolvedValueOnce([]);
    groupApi.list.mockResolvedValueOnce([
      { id: 1, name: 'Group Alpha' },
      { id: 2, name: 'Group Beta' },
    ]);

    renderDashboard();

    await waitFor(() => {
      expect(screen.queryByText(/loading dashboard/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('Group Alpha')).toBeInTheDocument();
    expect(screen.getByText('Group Beta')).toBeInTheDocument();
  });
});

describe('TC-W13 | Dashboard — Welcome Message', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows the user full name in the welcome subtitle', async () => {
    taskApi.list.mockResolvedValueOnce([]);
    groupApi.list.mockResolvedValueOnce([]);

    renderDashboard({ id: 1, fullName: 'Maria Santos', email: 'maria@example.com' });

    await waitFor(() => {
      expect(screen.queryByText(/loading dashboard/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText(/welcome back, maria santos/i)).toBeInTheDocument();
  });

  test('falls back to "User" when fullName is missing', async () => {
    taskApi.list.mockResolvedValueOnce([]);
    groupApi.list.mockResolvedValueOnce([]);

    renderDashboard({ id: 2, email: 'no-name@example.com' });

    await waitFor(() => {
      expect(screen.queryByText(/loading dashboard/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText(/welcome back, user/i)).toBeInTheDocument();
  });
});
