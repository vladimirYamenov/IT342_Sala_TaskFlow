import './App.css';
import { useMemo, useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || '';

const initialRegister = {
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
};

const initialLogin = {
  email: '',
  password: ''
};

function App() {
  const [mode, setMode] = useState('login');
  const [registerForm, setRegisterForm] = useState(initialRegister);
  const [loginForm, setLoginForm] = useState(initialLogin);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [currentUser, setCurrentUser] = useState(null);

  const isAuthenticated = useMemo(() => Boolean(currentUser), [currentUser]);

  const switchMode = (nextMode) => {
    setMode(nextMode);
    setError('');
    setSuccess('');
  };

  const handleRegisterChange = (event) => {
    const { name, value } = event.target;
    setRegisterForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleLoginChange = (event) => {
    const { name, value } = event.target;
    setLoginForm((prev) => ({ ...prev, [name]: value }));
  };

  const validateRegister = () => {
    if (!registerForm.fullName || !registerForm.email || !registerForm.password || !registerForm.confirmPassword) {
      return 'All registration fields are required.';
    }

    if (registerForm.password.length < 8) {
      return 'Password must be at least 8 characters.';
    }

    if (registerForm.password !== registerForm.confirmPassword) {
      return 'Passwords do not match.';
    }

    return null;
  };

  const validateLogin = () => {
    if (!loginForm.email || !loginForm.password) {
      return 'Email and password are required.';
    }

    return null;
  };

  const extractErrorMessage = async (response, fallbackMessage) => {
    const contentType = response.headers.get('content-type') || '';

    if (contentType.includes('application/json')) {
      const json = await response.json().catch(() => null);
      if (json) {
        return json.message || json.error || fallbackMessage;
      }
    }

    const text = await response.text();
    if (text && text.trim().length > 0) {
      return text;
    }

    return `${fallbackMessage} (HTTP ${response.status})`;
  };

  const handleRegisterSubmit = async (event) => {
    event.preventDefault();

    const validationError = validateRegister();
    if (validationError) {
      setError(validationError);
      setSuccess('');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registerForm)
      });

      if (!response.ok) {
        const message = await extractErrorMessage(response, 'Registration failed. Please try again.');
        throw new Error(message);
      }

      const text = await response.text();

      setSuccess(text || 'User registered successfully.');
      setRegisterForm(initialRegister);
      setLoginForm((prev) => ({ ...prev, email: registerForm.email }));
      setMode('login');
    } catch (submitError) {
      setError(submitError.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleLoginSubmit = async (event) => {
    event.preventDefault();

    const validationError = validateLogin();
    if (validationError) {
      setError(validationError);
      setSuccess('');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm)
      });

      if (!response.ok) {
        const message = await extractErrorMessage(response, 'Invalid credentials. Please try again.');
        throw new Error(message);
      }

      const user = await response.json().catch(() => null);

      if (!user) {
        throw new Error('Login failed. Invalid server response.');
      }

      setCurrentUser(user);
      setSuccess('Login successful. Welcome back!');
      setLoginForm(initialLogin);
    } catch (submitError) {
      setError(submitError.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setSuccess('Logged out successfully.');
    setError('');
    switchMode('login');
  };

  if (isAuthenticated) {
    return (
      <main className="page">
        <section className="panel panel-dashboard fade-in">
          <p className="eyebrow">TaskFlow</p>
          <h1>Dashboard</h1>
          <p className="subtitle">Authentication complete. You now have access to the system.</p>

          <div className="user-card">
            <span className="label">Name</span>
            <strong>{currentUser.fullName || 'No name provided'}</strong>
            <span className="label">Email</span>
            <strong>{currentUser.email}</strong>
          </div>

          <button className="btn btn-secondary" onClick={handleLogout}>Log Out</button>
        </section>
      </main>
    );
  }

  return (
    <main className="page">
      <section className="panel fade-in">
        <p className="eyebrow">TaskFlow</p>
        <h1>{mode === 'login' ? 'Welcome Back' : 'Create Account'}</h1>
        <p className="subtitle">
          {mode === 'login'
            ? 'Sign in to continue to your workspace.'
            : 'Register a new account to start collaborating.'}
        </p>

        <div className="mode-switch">
          <button
            className={`tab ${mode === 'login' ? 'active' : ''}`}
            onClick={() => switchMode('login')}
            type="button"
          >
            Login
          </button>
          <button
            className={`tab ${mode === 'register' ? 'active' : ''}`}
            onClick={() => switchMode('register')}
            type="button"
          >
            Register
          </button>
        </div>

        {error ? <p className="alert error">{error}</p> : null}
        {success ? <p className="alert success">{success}</p> : null}

        {mode === 'register' ? (
          <form className="auth-form" onSubmit={handleRegisterSubmit}>
            <label>
              Full Name
              <input
                type="text"
                name="fullName"
                value={registerForm.fullName}
                onChange={handleRegisterChange}
                placeholder="Juan Dela Cruz"
              />
            </label>

            <label>
              Email
              <input
                type="email"
                name="email"
                value={registerForm.email}
                onChange={handleRegisterChange}
                placeholder="name@example.com"
              />
            </label>

            <label>
              Password
              <input
                type="password"
                name="password"
                value={registerForm.password}
                onChange={handleRegisterChange}
                placeholder="At least 8 characters"
              />
            </label>

            <label>
              Confirm Password
              <input
                type="password"
                name="confirmPassword"
                value={registerForm.confirmPassword}
                onChange={handleRegisterChange}
                placeholder="Re-enter password"
              />
            </label>

            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Creating account...' : 'Register'}
            </button>
          </form>
        ) : (
          <form className="auth-form" onSubmit={handleLoginSubmit}>
            <label>
              Email
              <input
                type="email"
                name="email"
                value={loginForm.email}
                onChange={handleLoginChange}
                placeholder="name@example.com"
              />
            </label>

            <label>
              Password
              <input
                type="password"
                name="password"
                value={loginForm.password}
                onChange={handleLoginChange}
                placeholder="Enter your password"
              />
            </label>

            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Login'}
            </button>
          </form>
        )}
      </section>
    </main>
  );
}

export default App;
