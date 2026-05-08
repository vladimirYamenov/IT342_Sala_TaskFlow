import './App.css';
import { useEffect, useState, useCallback } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { authApi } from './shared/api';
import Layout from './shared/components/Layout';
import Toast from './shared/components/Toast';
import Login from './features/auth/Login';
import Register from './features/auth/Register';
import Dashboard from './features/dashboard/Dashboard';
import Tasks from './features/tasks/Tasks';
import Groups from './features/groups/Groups';
import Files from './features/files/Files';
import Settings from './features/settings/Settings';

function App() {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      authApi.me()
        .then((u) => setUser(u))
        .catch(() => localStorage.removeItem('token'))
        .finally(() => setInitializing(false));
    } else {
      setInitializing(false);
    }
  }, []);

  const addToast = useCallback((message, type = 'success') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const handleAuth = (data) => setUser(data);

  const handleLogout = () => {
    localStorage.removeItem('token');
    setUser(null);
    addToast('Logged out successfully', 'info');
  };

  if (initializing) {
    return (
      <div className="app-loader">
        <span className="brand-icon">⚡</span>
        <p>Loading TaskFlow...</p>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <Toast toasts={toasts} onRemove={removeToast} />
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={user ? <Navigate to="/" /> : <Login onAuth={handleAuth} addToast={addToast} />} />
        <Route path="/register" element={user ? <Navigate to="/" /> : <Register onAuth={handleAuth} addToast={addToast} />} />

        {/* Protected routes */}
        <Route element={user ? <Layout user={user} onLogout={handleLogout} /> : <Navigate to="/login" />}>
          <Route index element={<Dashboard user={user} addToast={addToast} />} />
          <Route path="tasks" element={<Tasks user={user} addToast={addToast} />} />
          <Route path="groups" element={<Groups user={user} addToast={addToast} />} />
          <Route path="files" element={<Files addToast={addToast} />} />
          <Route path="settings" element={<Settings user={user} addToast={addToast} />} />
        </Route>

        <Route path="*" element={<Navigate to={user ? '/' : '/login'} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
