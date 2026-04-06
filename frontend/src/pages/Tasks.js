import { useCallback, useEffect, useState } from 'react';
import { taskApi } from '../api';

const emptyTask = { title: '', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: '' };

export default function Tasks() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyTask);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');

  const fetchTasks = useCallback(async () => {
    try {
      const params = {};
      if (filterStatus !== 'ALL') params.status = filterStatus;
      if (filterPriority !== 'ALL') params.priority = filterPriority;
      const data = await taskApi.list(params);
      setTasks(Array.isArray(data) ? data : []);
    } catch {
      setTasks([]);
    } finally {
      setLoading(false);
    }
  }, [filterStatus, filterPriority]);

  useEffect(() => { fetchTasks(); }, [fetchTasks]);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyTask);
    setError('');
    setShowModal(true);
  };

  const openEdit = (task) => {
    setEditing(task);
    setForm({
      title: task.title || '',
      description: task.description || '',
      priority: task.priority || 'MEDIUM',
      status: task.status || 'TODO',
      dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
    });
    setError('');
    setShowModal(true);
  };

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) { setError('Title is required'); return; }
    setSaving(true);
    setError('');
    try {
      if (editing) {
        await taskApi.update(editing.id, form);
      } else {
        await taskApi.create(form);
      }
      setShowModal(false);
      fetchTasks();
    } catch (err) {
      setError(err.message || 'Failed to save task');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this task?')) return;
    try {
      await taskApi.delete(id);
      fetchTasks();
    } catch { /* ignore */ }
  };

  const priorityBadge = (p) => {
    const map = { HIGH: 'badge-danger', MEDIUM: 'badge-warning', LOW: 'badge-info' };
    return map[p] || 'badge-info';
  };

  const statusBadge = (s) => {
    const map = { COMPLETED: 'badge-success', IN_PROGRESS: 'badge-warning', PENDING: 'badge-info', TODO: 'badge-info' };
    return map[s] || 'badge-info';
  };

  if (loading) return <div className="page-loader">Loading tasks...</div>;

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div>
          <h1>My Tasks</h1>
          <p className="page-subtitle">{tasks.length} task{tasks.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ New Task</button>
      </div>

      <div className="filters-bar">
        <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
          <option value="ALL">All Statuses</option>
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="COMPLETED">Completed</option>
          <option value="PENDING">Pending</option>
        </select>
        <select value={filterPriority} onChange={(e) => setFilterPriority(e.target.value)}>
          <option value="ALL">All Priorities</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
      </div>

      {tasks.length === 0 ? (
        <div className="empty-state-box">
          <p className="empty-state-title">No tasks found</p>
          <p className="text-muted">Create your first task to get started!</p>
          <button className="btn btn-primary" onClick={openCreate}>+ Create Task</button>
        </div>
      ) : (
        <div className="task-cards">
          {tasks.map((task) => (
            <div className="task-card" key={task.id}>
              <div className="task-card-header">
                <h3>{task.title}</h3>
                <div className="task-card-actions">
                  <button className="icon-btn" onClick={() => openEdit(task)} title="Edit">✏️</button>
                  <button className="icon-btn" onClick={() => handleDelete(task.id)} title="Delete">🗑️</button>
                </div>
              </div>
              {task.description && <p className="task-card-desc">{task.description}</p>}
              <div className="task-card-footer">
                <span className={`badge ${priorityBadge(task.priority)}`}>{task.priority}</span>
                <span className={`badge ${statusBadge(task.status)}`}>{task.status?.replace('_', ' ')}</span>
                {task.dueDate && (
                  <span className="text-muted">Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editing ? 'Edit Task' : 'New Task'}</h2>
              <button className="icon-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>
            {error && <p className="alert error">{error}</p>}
            <form className="modal-form" onSubmit={handleSubmit}>
              <label>
                Title *
                <input name="title" value={form.title} onChange={handleChange} placeholder="Task title" />
              </label>
              <label>
                Description
                <textarea name="description" value={form.description} onChange={handleChange} placeholder="Optional description" rows={3} />
              </label>
              <div className="form-row">
                <label>
                  Priority
                  <select name="priority" value={form.priority} onChange={handleChange}>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </label>
                <label>
                  Status
                  <select name="status" value={form.status} onChange={handleChange}>
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="PENDING">Pending</option>
                  </select>
                </label>
              </div>
              <label>
                Due Date
                <input type="date" name="dueDate" value={form.dueDate} onChange={handleChange} />
              </label>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : editing ? 'Update Task' : 'Create Task'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
