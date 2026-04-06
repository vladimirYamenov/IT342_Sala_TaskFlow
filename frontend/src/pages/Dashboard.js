import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../api';

/**
 * Facade Pattern (Frontend integration):
 * Before: Dashboard made TWO separate API calls (taskApi.list + groupApi.list)
 * and computed stats (total, completed, pending, inProgress) on the client.
 * After: A single dashboardApi.get() call returns all data pre-aggregated
 * by the DashboardFacade on the backend.
 */
export default function Dashboard({ user }) {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardApi.get()
      .then((data) => setDashboard(data))
      .catch(() => setDashboard(null))
      .finally(() => setLoading(false));
  }, []);

  const total = dashboard?.totalTasks || 0;
  const completed = dashboard?.completedTasks || 0;
  const pending = dashboard?.pendingTasks || 0;
  const inProgress = dashboard?.inProgressTasks || 0;
  const recentTasks = dashboard?.recentTasks || [];
  const groups = dashboard?.groups || [];

  const priorityBadge = (p) => {
    const map = { HIGH: 'badge-danger', MEDIUM: 'badge-warning', LOW: 'badge-info' };
    return map[p] || 'badge-info';
  };

  const statusBadge = (s) => {
    const map = { COMPLETED: 'badge-success', IN_PROGRESS: 'badge-warning', PENDING: 'badge-info', TODO: 'badge-info' };
    return map[s] || 'badge-info';
  };

  if (loading) {
    return <div className="page-loader">Loading dashboard...</div>;
  }

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p className="page-subtitle">Welcome back, {user?.fullName || 'User'}</p>
        </div>
        <Link to="/tasks" className="btn btn-primary">+ New Task</Link>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <span className="stat-icon stat-blue">📋</span>
          <div className="stat-body">
            <span className="stat-value">{total}</span>
            <span className="stat-label">Total Tasks</span>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon stat-green">✅</span>
          <div className="stat-body">
            <span className="stat-value">{completed}</span>
            <span className="stat-label">Completed</span>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon stat-yellow">⏳</span>
          <div className="stat-body">
            <span className="stat-value">{inProgress}</span>
            <span className="stat-label">In Progress</span>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon stat-teal">📌</span>
          <div className="stat-body">
            <span className="stat-value">{pending}</span>
            <span className="stat-label">Pending</span>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <section className="card">
          <div className="card-header">
            <h2>Recent Tasks</h2>
            <Link to="/tasks" className="link-muted">View all →</Link>
          </div>
          {recentTasks.length === 0 ? (
            <p className="empty-state">No tasks yet. Create your first task!</p>
          ) : (
            <div className="task-list">
              {recentTasks.map((task) => (
                <div className="task-row" key={task.id}>
                  <div className="task-row-info">
                    <strong>{task.title}</strong>
                    {task.dueDate && (
                      <span className="text-muted">Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                    )}
                  </div>
                  <div className="task-row-badges">
                    <span className={`badge ${priorityBadge(task.priority)}`}>{task.priority}</span>
                    <span className={`badge ${statusBadge(task.status)}`}>{task.status?.replace('_', ' ')}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="card">
          <div className="card-header">
            <h2>My Groups</h2>
            <Link to="/groups" className="link-muted">View all →</Link>
          </div>
          {groups.length === 0 ? (
            <p className="empty-state">No groups yet. Create or join a group!</p>
          ) : (
            <div className="group-list">
              {groups.map((group) => (
                <div className="group-row" key={group.id}>
                  <div className="group-avatar">{group.name.charAt(0).toUpperCase()}</div>
                  <div className="group-row-info">
                    <strong>{group.name}</strong>
                    <span className="text-muted">{group.members?.length || 0} members</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
