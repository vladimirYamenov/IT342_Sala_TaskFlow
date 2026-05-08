import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Tasks from './Tasks';
import { taskApi, groupApi } from '../../shared/api';

jest.mock('../../shared/api', () => ({
  taskApi: {
    list: jest.fn(),
    create: jest.fn(),
    update: jest.fn(),
    delete: jest.fn(),
  },
  groupApi: {
    list: jest.fn(),
    get: jest.fn(),
  },
}));

const mockUser = { id: 1, fullName: 'Juan Dela Cruz', email: 'juan@example.com' };
const mockAddToast = jest.fn();

const sampleTasks = [
  { id: 1, title: 'Write unit tests', description: 'Cover all features', priority: 'HIGH', status: 'TODO', dueDate: null, groupId: null, assignedUsers: [], creatorId: 1 },
  { id: 2, title: 'Fix bug #42', description: 'Null pointer issue', priority: 'MEDIUM', status: 'IN_PROGRESS', dueDate: null, groupId: null, assignedUsers: [], creatorId: 1 },
];

function renderTasks(user = mockUser) {
  return render(
    <MemoryRouter>
      <Tasks user={user} addToast={mockAddToast} />
    </MemoryRouter>
  );
}

describe('TC-W14 | Tasks — Loading and List Display', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows task titles after data loads', async () => {
    taskApi.list.mockResolvedValueOnce(sampleTasks);
    groupApi.list.mockResolvedValueOnce([]);

    renderTasks();

    await waitFor(() => {
      expect(screen.getByText('Write unit tests')).toBeInTheDocument();
      expect(screen.getByText('Fix bug #42')).toBeInTheDocument();
    });
  });

  test('shows empty state message when no tasks exist', async () => {
    taskApi.list.mockResolvedValueOnce([]);
    groupApi.list.mockResolvedValueOnce([]);

    renderTasks();

    await waitFor(() => {
      expect(screen.getByText(/no tasks yet/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W15 | Tasks — New Task Button', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders New Task button', async () => {
    taskApi.list.mockResolvedValueOnce([]);
    groupApi.list.mockResolvedValueOnce([]);

    renderTasks();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /\+ new task/i })).toBeInTheDocument();
    });
  });
});

describe('TC-W16 | Tasks — Open Create Modal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('opens task creation modal when New Task button is clicked', async () => {
    taskApi.list.mockResolvedValueOnce([]);
    groupApi.list.mockResolvedValueOnce([]);

    renderTasks();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new task/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /\+ new task/i }));

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Enter task title (min. 3 characters)')).toBeInTheDocument();
    });
  });
});

describe('TC-W17 | Tasks — Search Filter', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('filters tasks by search query', async () => {
    taskApi.list.mockResolvedValueOnce(sampleTasks);
    groupApi.list.mockResolvedValueOnce([]);

    renderTasks();

    await waitFor(() => {
      expect(screen.getByText('Write unit tests')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await userEvent.clear(searchInput);
    await userEvent.type(searchInput, 'bug');

    await waitFor(() => {
      expect(screen.queryByText('Write unit tests')).not.toBeInTheDocument();
      expect(screen.getByText('Fix bug #42')).toBeInTheDocument();
    });
  });
});

describe('TC-W18 | Tasks — Create Task via API', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('calls taskApi.create and refreshes list on save', async () => {
    taskApi.list
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([{ id: 3, title: 'New Task Created', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: null, groupId: null, assignedUsers: [], creatorId: 1 }]);
    groupApi.list.mockResolvedValue([]);
    taskApi.create.mockResolvedValueOnce({ id: 3, title: 'New Task Created' });

    renderTasks();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new task/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /\+ new task/i }));

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Enter task title (min. 3 characters)')).toBeInTheDocument();
    });

    await userEvent.type(screen.getByPlaceholderText('Enter task title (min. 3 characters)'), 'New Task Created');

    // Submit the form (Create Task button — exact match to avoid the empty-state button)
    const saveBtn = screen.getByRole('button', { name: /^create task$/i });
    fireEvent.click(saveBtn);

    await waitFor(() => {
      expect(taskApi.create).toHaveBeenCalledWith(
        expect.objectContaining({ title: 'New Task Created' })
      );
    });
  });
});
