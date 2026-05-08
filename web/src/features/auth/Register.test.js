import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Register from './Register';
import { authApi } from '../../shared/api';

// Mock the API module
jest.mock('../../shared/api', () => ({
  authApi: {
    register: jest.fn(),
  },
}));

// Mock react-router-dom navigation
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const mockOnAuth = jest.fn();
const mockAddToast = jest.fn();

function renderRegister() {
  return render(
    <MemoryRouter>
      <Register onAuth={mockOnAuth} addToast={mockAddToast} />
    </MemoryRouter>
  );
}

describe('TC-W06 | Register Page Renders Correctly', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders all required form fields', () => {
    renderRegister();
    expect(screen.getByPlaceholderText(/juan dela cruz/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/name@example\.com/i)).toBeInTheDocument();
    // Two password fields
    const passwordFields = screen.getAllByPlaceholderText(/password/i);
    expect(passwordFields.length).toBeGreaterThanOrEqual(1);
  });

  test('renders "Create Account" heading', () => {
    renderRegister();
    expect(screen.getByRole('heading', { name: 'Create Account' })).toBeInTheDocument();
  });

  test('renders sign-up submit button', () => {
    renderRegister();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });
});

describe('TC-W07 | Register Validation — Missing Full Name', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when full name is empty', async () => {
    renderRegister();
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));
    await waitFor(() => {
      expect(screen.getByText(/full name is required/i)).toBeInTheDocument();
    });
  });

  test('shows error when full name is too short', async () => {
    renderRegister();
    await userEvent.type(screen.getByPlaceholderText(/juan dela cruz/i), 'A');
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));
    await waitFor(() => {
      expect(screen.getByText(/at least 2 characters/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W08 | Register Validation — Password Mismatch', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when passwords do not match', async () => {
    renderRegister();
    await userEvent.type(screen.getByPlaceholderText(/juan dela cruz/i), 'Juan Dela Cruz');
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'juan@example.com');

    await userEvent.type(screen.getByPlaceholderText('At least 8 characters'), 'password123');
    await userEvent.type(screen.getByPlaceholderText('Re-enter password'), 'different456');

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));
    await waitFor(() => {
      expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W09 | Register Validation — Short Password', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when password is less than 8 characters', async () => {
    renderRegister();
    await userEvent.type(screen.getByPlaceholderText(/juan dela cruz/i), 'Juan Dela Cruz');
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'juan@example.com');

    await userEvent.type(screen.getByPlaceholderText('At least 8 characters'), 'short1');
    await userEvent.type(screen.getByPlaceholderText('Re-enter password'), 'short1');

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));
    await waitFor(() => {
      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W10 | Register — Successful Submission', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('calls authApi.register and navigates to login on success', async () => {
    authApi.register.mockResolvedValueOnce({});

    renderRegister();
    await userEvent.type(screen.getByPlaceholderText(/juan dela cruz/i), 'Juan Dela Cruz');
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'juan@example.com');

    await userEvent.type(screen.getByPlaceholderText('At least 8 characters'), 'password123');
    await userEvent.type(screen.getByPlaceholderText('Re-enter password'), 'password123');

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith({
        fullName: 'Juan Dela Cruz',
        email: 'juan@example.com',
        password: 'password123',
        confirmPassword: 'password123',
      });
      expect(mockNavigate).toHaveBeenCalledWith('/login', expect.objectContaining({ state: expect.any(Object) }));
    });
  });

  test('shows API error message on registration failure', async () => {
    authApi.register.mockRejectedValueOnce(new Error('Email already in use.'));

    renderRegister();
    await userEvent.type(screen.getByPlaceholderText(/juan dela cruz/i), 'Juan Dela Cruz');
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'existing@example.com');

    await userEvent.type(screen.getByPlaceholderText('At least 8 characters'), 'password123');
    await userEvent.type(screen.getByPlaceholderText('Re-enter password'), 'password123');

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(screen.getByText(/email already in use/i)).toBeInTheDocument();
    });
  });
});
