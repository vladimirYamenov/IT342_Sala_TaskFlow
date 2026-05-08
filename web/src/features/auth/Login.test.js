import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Login from './Login';
import { authApi } from '../../shared/api';

// Mock the API module
jest.mock('../../shared/api', () => ({
  authApi: {
    login: jest.fn(),
  },
}));

// Mock react-router-dom navigation
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => ({ state: null, pathname: '/login' }),
}));

const mockOnAuth = jest.fn();
const mockAddToast = jest.fn();

function renderLogin() {
  return render(
    <MemoryRouter>
      <Login onAuth={mockOnAuth} addToast={mockAddToast} />
    </MemoryRouter>
  );
}

describe('TC-W01 | Login Page Renders Correctly', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders email and password fields and sign-in button', () => {
    renderLogin();
    expect(screen.getByPlaceholderText(/name@example\.com/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter your password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  test('renders "Welcome Back" heading', () => {
    renderLogin();
    expect(screen.getByText('Welcome Back')).toBeInTheDocument();
  });
});

describe('TC-W02 | Login Form Validation — Empty Fields', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when email is empty on submit', async () => {
    renderLogin();
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
    await waitFor(() => {
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W03 | Login Form Validation — Invalid Email', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when email format is invalid', async () => {
    renderLogin();
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'notanemail');
    await userEvent.type(screen.getByPlaceholderText(/enter your password/i), 'somepassword');
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
    await waitFor(() => {
      expect(screen.getByText(/valid email/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W04 | Login Form Validation — Missing Password', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error when password is empty', async () => {
    renderLogin();
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'test@example.com');
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
    await waitFor(() => {
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });
  });
});

describe('TC-W05 | Login — Successful Authentication', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('calls authApi.login and navigates home on valid credentials', async () => {
    authApi.login.mockResolvedValueOnce({ token: 'fake-jwt-token', id: 1, email: 'test@example.com' });

    renderLogin();
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'test@example.com');
    await userEvent.type(screen.getByPlaceholderText(/enter your password/i), 'password123');
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith({ email: 'test@example.com', password: 'password123' });
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  test('shows error message when API returns unauthorized', async () => {
    const err = new Error('Invalid credentials');
    err.status = 401;
    authApi.login.mockRejectedValueOnce(err);

    renderLogin();
    await userEvent.type(screen.getByPlaceholderText(/name@example\.com/i), 'test@example.com');
    await userEvent.type(screen.getByPlaceholderText(/enter your password/i), 'wrongpassword');
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });
});
