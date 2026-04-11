import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { taskApi, groupApi } from '../api';

export default function Dashboard({ user }) {
  const [tasks, setTasks] = useState([]);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      taskApi.list().catch(() => []),
      groupApi.list().catch(() => []),
    ]).then(([t, g]) => {
      setTasks(Array.isArray(t) ? t : []);
      setGroups(Array.isArray(g) ? g : []);
    }).finally(() => setLoading(false));
  }, []);

  const total = tasks.length;
  const completed = tasks.filter((t) => t.status === 'COMPLETED').length;
  const pending = tasks.filter((t) => t.status === 'PENDING' || t.status === 'TODO').length;
  const inProgress = tasks.filter((t) => t.status === 'IN_PROGRESS').length;

  const overdue = tasks.filter((t) => {
    if (!t.dueDate || t.status === 'COMPLETED') return false;
    return new Date(t.dueDate) < new Date();
  }).length;

  const recentTasks = [...tasks]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5);

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

      {overdue > 0 && (
        <div className="alert error" style={{ marginBottom: 20 }}>
          ⚠ You have <strong>{overdue}</strong> overdue task{overdue !== 1 ? 's' : ''}. <Link to="/tasks" style={{ color: 'inherit', fontWeight: 700 }}>View tasks →</Link>
        </div>
      )}

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
                      <span className={`text-muted${new Date(task.dueDate) < new Date() && task.status !== 'COMPLETED' ? ' text-overdue' : ''}`}>
                        Due: {new Date(task.dueDate).toLocaleDateString()}
                        {new Date(task.dueDate) < new Date() && task.status !== 'COMPLETED' && ' (Overdue)'}
                      </span>
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
