import { useCallback, useEffect, useState } from 'react';
import { taskApi } from '../api';

const emptyTask = { title: '', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: '' };

export default function Tasks({ addToast }) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyTask);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [deleteConfirm, setDeleteConfirm] = useState(null);

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

  // Filter tasks by search query
  const filteredTasks = tasks.filter((task) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      task.title?.toLowerCase().includes(q) ||
      task.description?.toLowerCase().includes(q)
    );
  });

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
    if (error) setError('');
  };

  const validate = () => {
    if (!form.title.trim()) return 'Task title is required.';
    if (form.title.trim().length < 3) return 'Title must be at least 3 characters.';
    if (form.title.trim().length > 100) return 'Title must be less than 100 characters.';
    if (form.description && form.description.length > 500) return 'Description must be less than 500 characters.';
    if (!['HIGH', 'MEDIUM', 'LOW'].includes(form.priority)) return 'Please select a valid priority.';
    if (!['TODO', 'IN_PROGRESS', 'COMPLETED', 'PENDING'].includes(form.status)) return 'Please select a valid status.';
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    setSaving(true);
    setError('');
    try {
      if (editing) {
        await taskApi.update(editing.id, form);
        addToast(`Task "${form.title}" updated successfully!`, 'success');
      } else {
        await taskApi.create(form);
        addToast(`Task "${form.title}" created successfully!`, 'success');
      }
      setShowModal(false);
      fetchTasks();
    } catch (err) {
      setError(err.message || 'Failed to save task. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (task) => {
    try {
      await taskApi.delete(task.id);
      addToast(`Task "${task.title}" deleted successfully.`, 'info');
      setDeleteConfirm(null);
      fetchTasks();
    } catch (err) {
      addToast(err.message || 'Failed to delete task.', 'error');
      setDeleteConfirm(null);
    }
  };

  const handleQuickStatus = async (task, newStatus) => {
    try {
      await taskApi.update(task.id, { ...task, status: newStatus, dueDate: task.dueDate ? task.dueDate.split('T')[0] : '' });
      const label = newStatus.replace('_', ' ').toLowerCase();
      addToast(`Task marked as ${label}.`, 'success');
      fetchTasks();
    } catch (err) {
      addToast(err.message || 'Failed to update status.', 'error');
    }
  };

  const priorityBadge = (p) => {
    const map = { HIGH: 'badge-danger', MEDIUM: 'badge-warning', LOW: 'badge-info' };
    return map[p] || 'badge-info';
  };

  const statusBadge = (s) => {
    const map = { COMPLETED: 'badge-success', IN_PROGRESS: 'badge-warning', PENDING: 'badge-info', TODO: 'badge-info' };
    return map[s] || 'badge-info';
  };

  const isOverdue = (task) => {
    if (!task.dueDate || task.status === 'COMPLETED') return false;
    return new Date(task.dueDate) < new Date();
  };

  const clearFilters = () => {
    setFilterStatus('ALL');
    setFilterPriority('ALL');
    setSearchQuery('');
  };

  const hasActiveFilters = filterStatus !== 'ALL' || filterPriority !== 'ALL' || searchQuery.trim() !== '';

  if (loading) return <div className="page-loader">Loading tasks...</div>;

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div>
          <h1>My Tasks</h1>
          <p className="page-subtitle">
            {filteredTasks.length} task{filteredTasks.length !== 1 ? 's' : ''}
            {hasActiveFilters && ` (filtered from ${tasks.length})`}
          </p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ New Task</button>
      </div>

      {/* Search and Filters */}
      <div className="filters-bar">
        <div className="search-box">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Search tasks..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
          {searchQuery && (
            <button className="search-clear" onClick={() => setSearchQuery('')}>✕</button>
          )}
        </div>
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
        {hasActiveFilters && (
          <button className="btn btn-secondary btn-sm" onClick={clearFilters}>Clear Filters</button>
        )}
      </div>

      {/* Task List */}
      {filteredTasks.length === 0 ? (
        <div className="empty-state-box">
          {hasActiveFilters ? (
            <>
              <p className="empty-state-title">No tasks match your filters</p>
              <p className="text-muted">Try adjusting your search or filter criteria.</p>
              <button className="btn btn-secondary" onClick={clearFilters}>Clear Filters</button>
            </>
          ) : (
            <>
              <p className="empty-state-title">No tasks yet</p>
              <p className="text-muted">Create your first task to get started!</p>
              <button className="btn btn-primary" onClick={openCreate}>+ Create Task</button>
            </>
          )}
        </div>
      ) : (
        <div className="task-cards">
          {filteredTasks.map((task) => (
            <div className={`task-card${isOverdue(task) ? ' task-card-overdue' : ''}${task.status === 'COMPLETED' ? ' task-card-done' : ''}`} key={task.id}>
              <div className="task-card-header">
                <h3>{task.title}</h3>
                <div className="task-card-actions">
                  {task.status !== 'COMPLETED' && (
                    <button
                      className="icon-btn"
                      onClick={() => handleQuickStatus(task, 'COMPLETED')}
                      title="Mark as completed"
                    >
                      ✓
                    </button>
                  )}
                  <button className="icon-btn" onClick={() => openEdit(task)} title="Edit">✏️</button>
                  <button className="icon-btn icon-btn-danger" onClick={() => setDeleteConfirm(task)} title="Delete">🗑️</button>
                </div>
              </div>
              {task.description && <p className="task-card-desc">{task.description}</p>}
              <div className="task-card-footer">
                <span className={`badge ${priorityBadge(task.priority)}`}>{task.priority}</span>
                <span className={`badge ${statusBadge(task.status)}`}>{task.status?.replace('_', ' ')}</span>
                {task.dueDate && (
                  <span className={`text-muted${isOverdue(task) ? ' text-overdue' : ''}`}>
                    {isOverdue(task) ? '⚠ Overdue: ' : 'Due: '}
                    {new Date(task.dueDate).toLocaleDateString()}
                  </span>
                )}
              </div>
              {task.status !== 'COMPLETED' && (
                <div className="task-card-quick-actions">
                  {task.status !== 'IN_PROGRESS' && (
                    <button className="btn-quick" onClick={() => handleQuickStatus(task, 'IN_PROGRESS')}>
                      Start Working
                    </button>
                  )}
                  {task.status !== 'TODO' && task.status !== 'COMPLETED' && (
                    <button className="btn-quick" onClick={() => handleQuickStatus(task, 'COMPLETED')}>
                      Mark Done
                    </button>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editing ? 'Edit Task' : 'Create New Task'}</h2>
              <button className="icon-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>
            {error && <p className="alert error">{error}</p>}
            <form className="modal-form" onSubmit={handleSubmit} noValidate>
              <label>
                Title <span className="required">*</span>
                <input
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  placeholder="Enter task title (min. 3 characters)"
                  maxLength={100}
                  autoFocus
                />
                <span className="char-count">{form.title.length}/100</span>
              </label>
              <label>
                Description
                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  placeholder="Describe what needs to be done (optional)"
                  rows={3}
                  maxLength={500}
                />
                <span className="char-count">{form.description.length}/500</span>
              </label>
              <div className="form-row">
                <label>
                  Priority <span className="required">*</span>
                  <select name="priority" value={form.priority} onChange={handleChange}>
                    <option value="HIGH">🔴 High</option>
                    <option value="MEDIUM">🟡 Medium</option>
                    <option value="LOW">🔵 Low</option>
                  </select>
                </label>
                <label>
                  Status <span className="required">*</span>
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

      {/* Delete Confirmation Modal */}
      {deleteConfirm && (
        <div className="modal-overlay" onClick={() => setDeleteConfirm(null)}>
          <div className="modal modal-sm" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Delete Task</h2>
              <button className="icon-btn" onClick={() => setDeleteConfirm(null)}>✕</button>
            </div>
            <p className="modal-body-text">
              Are you sure you want to delete <strong>"{deleteConfirm.title}"</strong>? This action cannot be undone.
            </p>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setDeleteConfirm(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={() => handleDelete(deleteConfirm)}>Delete Task</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
