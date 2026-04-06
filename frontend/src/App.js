import './App.css';
import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { authApi } from './api';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Tasks from './pages/Tasks';
import Groups from './pages/Groups';
import Files from './pages/Files';

function App() {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

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

  const handleAuth = (data) => setUser(data);

  const handleLogout = () => {
    localStorage.removeItem('token');
    setUser(null);
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
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={user ? <Navigate to="/" /> : <Login onAuth={handleAuth} />} />
        <Route path="/register" element={user ? <Navigate to="/" /> : <Register onAuth={handleAuth} />} />

        {/* Protected routes */}
        <Route element={user ? <Layout user={user} onLogout={handleLogout} /> : <Navigate to="/login" />}>
          <Route index element={<Dashboard user={user} />} />
          <Route path="tasks" element={<Tasks />} />
          <Route path="groups" element={<Groups user={user} />} />
          <Route path="files" element={<Files />} />
        </Route>

        <Route path="*" element={<Navigate to={user ? '/' : '/login'} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
