import { NavLink, Outlet, useNavigate } from 'react-router-dom';

const navItems = [
  { to: '/',       icon: '📊', label: 'Dashboard' },
  { to: '/tasks',  icon: '✅', label: 'My Tasks' },
  { to: '/groups', icon: '👥', label: 'Groups' },
  { to: '/files',  icon: '📁', label: 'Files' },
];

export default function Layout({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">⚡</span>
          <span className="brand-text">TaskFlow</span>
        </div>

        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="user-avatar">
              {(user?.fullName || 'U').charAt(0).toUpperCase()}
            </div>
            <div className="user-info">
              <span className="user-name">{user?.fullName || 'User'}</span>
              <span className="user-email">{user?.email || ''}</span>
            </div>
          </div>
          <button className="btn-logout" onClick={handleLogout} title="Log out">
            🚪
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
