import './App.css';
import { useEffect, useState, useCallback } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { authApi } from './api';
import Layout from './components/Layout';
import Toast from './components/Toast';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Tasks from './pages/Tasks';
import Groups from './pages/Groups';
import Files from './pages/Files';

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
          <Route path="tasks" element={<Tasks addToast={addToast} />} />
          <Route path="groups" element={<Groups user={user} addToast={addToast} />} />
          <Route path="files" element={<Files addToast={addToast} />} />
        </Route>

        <Route path="*" element={<Navigate to={user ? '/' : '/login'} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
