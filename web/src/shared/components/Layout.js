import { NavLink, Outlet, useNavigate } from 'react-router-dom';

const navItems = [
  { to: '/',          icon: '📊', label: 'Dashboard' },
  { to: '/tasks',     icon: '✅', label: 'My Tasks' },
  { to: '/groups',    icon: '👥', label: 'Groups' },
  { to: '/files',     icon: '📁', label: 'Files' },
  { to: '/settings',  icon: '⚙️', label: 'Settings' },
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
          <button className="nav-item btn-logout-nav" onClick={handleLogout} title="Log out">
            <span className="nav-icon">🚪</span>
            <span>Logout</span>
          </button>
        </nav>


      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
