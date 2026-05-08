import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Groups from './Groups';
import { groupApi } from '../../shared/api';

jest.mock('../../shared/api', () => ({
  groupApi: {
    list: jest.fn(),
    get: jest.fn(),
    create: jest.fn(),
    addMember: jest.fn(),
    removeMember: jest.fn(),
    getTasks: jest.fn(),
    rename: jest.fn(),
    delete: jest.fn(),
  },
  taskApi: {
    create: jest.fn(),
    update: jest.fn(),
    delete: jest.fn(),
  },
}));

const mockUser = { id: 1, fullName: 'Juan Dela Cruz', email: 'juan@example.com' };
const mockAddToast = jest.fn();

const sampleGroups = [
  { id: 1, name: 'Alpha Team', owner: { id: 1 }, members: [{ id: 1, fullName: 'Juan Dela Cruz', email: 'juan@example.com' }] },
  { id: 2, name: 'Beta Squad', owner: { id: 2 }, members: [{ id: 2, fullName: 'Maria Santos', email: 'maria@example.com' }] },
];

function renderGroups(user = mockUser) {
  return render(
    <MemoryRouter>
      <Groups user={user} addToast={mockAddToast} />
    </MemoryRouter>
  );
}

describe('TC-W19 | Groups — Loading and List Display', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows group names after data loads', async () => {
    groupApi.list.mockResolvedValueOnce(sampleGroups);

    renderGroups();

    await waitFor(() => {
      expect(screen.getByText('Alpha Team')).toBeInTheDocument();
      expect(screen.getByText('Beta Squad')).toBeInTheDocument();
    });
  });

  test('shows empty state when no groups exist', async () => {
    groupApi.list.mockResolvedValueOnce([]);

    renderGroups();

    await waitFor(() => {
      expect(screen.getByText(/no groups yet/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W20 | Groups — New Group Button', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders New Group button', async () => {
    groupApi.list.mockResolvedValueOnce([]);

    renderGroups();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new group/i })).toBeInTheDocument();
    });
  });
});

describe('TC-W21 | Groups — Create Group Validation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when group name is empty', async () => {
    groupApi.list.mockResolvedValueOnce([]);

    renderGroups();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new group/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /new group/i }));

    await waitFor(() => {
      expect(screen.getByPlaceholderText('e.g. Project Alpha')).toBeInTheDocument();
    });

    // Submit with empty name
    const createBtn = screen.getByRole('button', { name: /^create group$/i });
    fireEvent.click(createBtn);

    await waitFor(() => {
      expect(screen.getByText(/group name is required/i)).toBeInTheDocument();
    });
  });

  test('shows error when group name is too short', async () => {
    groupApi.list.mockResolvedValueOnce([]);

    renderGroups();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new group/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /new group/i }));

    await waitFor(() => {
      expect(screen.getByPlaceholderText('e.g. Project Alpha')).toBeInTheDocument();
    });

    await userEvent.type(screen.getByPlaceholderText('e.g. Project Alpha'), 'A');
    const createBtn = screen.getByRole('button', { name: /^create group$/i });
    fireEvent.click(createBtn);

    await waitFor(() => {
      expect(screen.getByText(/at least 2 characters/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W22 | Groups — Create Group via API', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('calls groupApi.create and refreshes list on success', async () => {
    groupApi.list
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([{ id: 3, name: 'New Team', owner: { id: 1 }, members: [] }]);
    groupApi.create.mockResolvedValueOnce({ id: 3, name: 'New Team' });

    renderGroups();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new group/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /new group/i }));

    await waitFor(() => {
      expect(screen.getByPlaceholderText('e.g. Project Alpha')).toBeInTheDocument();
    });

    await userEvent.type(screen.getByPlaceholderText('e.g. Project Alpha'), 'New Team');
    const createBtn = screen.getByRole('button', { name: /^create group$/i });
    fireEvent.click(createBtn);

    await waitFor(() => {
      expect(groupApi.create).toHaveBeenCalledWith({ name: 'New Team' });
    });
  });
});
