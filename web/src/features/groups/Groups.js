import { useCallback, useEffect, useState } from 'react';
import { groupApi, taskApi } from '../../shared/api';

const emptyGroupTask = { title: '', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: '', assignedUserIds: [] };

export default function Groups({ user, addToast }) {
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [groupTasks, setGroupTasks] = useState([]);
  const [memberInput, setMemberInput] = useState('');
  const [showTaskModal, setShowTaskModal] = useState(false);
  const [taskForm, setTaskForm] = useState(emptyGroupTask);
  const [taskSaving, setTaskSaving] = useState(false);
  const [taskError, setTaskError] = useState('');
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState(emptyGroupTask);
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState('');
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);
  const [showRenameModal, setShowRenameModal] = useState(null); // holds group object
  const [renameInput, setRenameInput] = useState('');
  const [renameSaving, setRenameSaving] = useState(false);
  const [confirmDeleteGroupId, setConfirmDeleteGroupId] = useState(null);

  const fetchGroups = useCallback(async () => {
    try {
      const data = await groupApi.list();
      setGroups(Array.isArray(data) ? data : []);
    } catch {
      setGroups([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchGroups(); }, [fetchGroups]);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!newName.trim()) {
      setError('Group name is required.');
      return;
    }
    if (newName.trim().length < 2) {
      setError('Group name must be at least 2 characters.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await groupApi.create({ name: newName });
      addToast(`Group "${newName}" created successfully!`, 'success');
      setNewName('');
      setShowCreate(false);
      fetchGroups();
    } catch (err) {
      setError(err.message || 'Failed to create group');
    } finally {
      setSaving(false);
    }
  };

  const openGroup = async (group) => {
    setSelected(group);
    setError('');
    try {
      const tasks = await groupApi.getTasks(group.id);
      setGroupTasks(Array.isArray(tasks) ? tasks : []);
    } catch {
      setGroupTasks([]);
    }
  };

  const handleAddMember = async (e) => {
    e.preventDefault();
    if (!memberInput.trim() || !selected) {
      setError('Please enter a valid email address.');
      return;
    }
    setError('');
    try {
      await groupApi.addMember(selected.id, memberInput);
      addToast('Member added successfully!', 'success');
      setMemberInput('');
      fetchGroups();
      const updated = await groupApi.get(selected.id);
      setSelected(updated);
    } catch (err) {
      setError(err.message || 'Failed to add member');
      addToast(err.message || 'Failed to add member', 'error');
    }
  };

  const handleRemoveMember = async (userId, memberName) => {
    if (!selected) return;
    try {
      await groupApi.removeMember(selected.id, userId);
      addToast(`${memberName || 'Member'} removed from group.`, 'info');
      fetchGroups();
      const updated = await groupApi.get(selected.id);
      setSelected(updated);
    } catch (err) {
      addToast(err.message || 'Failed to remove member', 'error');
    }
  };

  const openTaskModal = () => {
    setTaskForm(emptyGroupTask);
    setTaskError('');
    setShowTaskModal(true);
  };

  const handleTaskFormChange = (e) => {
    setTaskForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const toggleTaskAssignee = (userId) => {
    setTaskForm((prev) => {
      const ids = prev.assignedUserIds || [];
      return {
        ...prev,
        assignedUserIds: ids.includes(userId)
          ? ids.filter((id) => id !== userId)
          : [...ids, userId],
      };
    });
  };

  const handleCreateGroupTask = async (e) => {
    e.preventDefault();
    if (!taskForm.title.trim()) {
      setTaskError('Task title is required.');
      return;
    }
    setTaskSaving(true);
    setTaskError('');
    try {
      await taskApi.create({
        ...taskForm,
        groupId: selected.id,
        dueDate: taskForm.dueDate || null,
      });
      addToast(`Task "${taskForm.title}" created!`, 'success');
      setShowTaskModal(false);
      const tasks = await groupApi.getTasks(selected.id);
      setGroupTasks(Array.isArray(tasks) ? tasks : []);
    } catch (err) {
      setTaskError(err.message || 'Failed to create task.');
    } finally {
      setTaskSaving(false);
    }
  };

  const openEditModal = (task) => {
    setEditForm({
      title: task.title || '',
      description: task.description || '',
      priority: task.priority || 'MEDIUM',
      status: task.status || 'TODO',
      dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
      assignedUserIds: (task.assignedUsers || []).map((u) => u.id),
    });
    setEditError('');
    setShowEditModal(task.id);
  };

  const handleEditFormChange = (e) => {
    setEditForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const toggleEditAssignee = (userId) => {
    setEditForm((prev) => {
      const ids = prev.assignedUserIds || [];
      return {
        ...prev,
        assignedUserIds: ids.includes(userId) ? ids.filter((id) => id !== userId) : [...ids, userId],
      };
    });
  };

  const handleEditGroupTask = async (e) => {
    e.preventDefault();
    if (!editForm.title.trim()) { setEditError('Task title is required.'); return; }
    setEditSaving(true);
    setEditError('');
    try {
      await taskApi.update(showEditModal, {
        ...editForm,
        groupId: selected.id,
        dueDate: editForm.dueDate || null,
      });
      addToast('Task updated!', 'success');
      setShowEditModal(false);
      const tasks = await groupApi.getTasks(selected.id);
      setGroupTasks(Array.isArray(tasks) ? tasks : []);
    } catch (err) {
      setEditError(err.message || 'Failed to update task.');
    } finally {
      setEditSaving(false);
    }
  };

  const handleDeleteGroupTask = async (taskId) => {
    try {
      await taskApi.delete(taskId);
      addToast('Task deleted.', 'info');
      setConfirmDeleteId(null);
      const tasks = await groupApi.getTasks(selected.id);
      setGroupTasks(Array.isArray(tasks) ? tasks : []);
    } catch (err) {
      addToast(err.message || 'Failed to delete task.', 'error');
    }
  };

  const handleMarkComplete = async (task) => {
    try {
      await taskApi.update(task.id, {
        ...task,
        status: 'COMPLETED',
        groupId: selected.id,
        assignedUserIds: (task.assignedUsers || []).map((u) => u.id),
        dueDate: task.dueDate ? task.dueDate.split('T')[0] : null,
      });
      addToast(`"${task.title}" marked as complete!`, 'success');
      const tasks = await groupApi.getTasks(selected.id);
      setGroupTasks(Array.isArray(tasks) ? tasks : []);
    } catch (err) {
      addToast(err.message || 'Failed to update task.', 'error');
    }
  };

  if (loading) return <div className="page-loader">Loading groups...</div>;

  const handleRenameGroup = async (e) => {
    e.preventDefault();
    if (!renameInput.trim()) return;
    setRenameSaving(true);
    try {
      await groupApi.rename(showRenameModal.id, renameInput.trim());
      addToast(`Group renamed to "${renameInput.trim()}"!`, 'success');
      setShowRenameModal(null);
      // If the renamed group is currently selected, update the header
      if (selected && selected.id === showRenameModal.id) {
        setSelected((prev) => ({ ...prev, name: renameInput.trim() }));
      }
      fetchGroups();
    } catch (err) {
      addToast(err.message || 'Failed to rename group.', 'error');
    } finally {
      setRenameSaving(false);
    }
  };

  const handleDeleteGroup = async (groupId) => {
    try {
      await groupApi.delete(groupId);
      addToast('Group deleted.', 'info');
      setConfirmDeleteGroupId(null);
      if (selected && selected.id === groupId) setSelected(null);
      fetchGroups();
    } catch (err) {
      addToast(err.message || 'Failed to delete group.', 'error');
    }
  };

  if (selected) {
    return (
      <>
      <div className="page-content fade-in">
        <div className="page-header">
          <div>
            <button className="btn-back" onClick={() => { setSelected(null); setError(''); }}>← Back to Groups</button>
            <h1>{selected.name}</h1>
            <p className="page-subtitle">{selected.members?.length || 0} member{(selected.members?.length || 0) !== 1 ? 's' : ''}</p>
          </div>
        </div>

        {error && <p className="alert error">{error}</p>}

        <div className="dashboard-grid">
          <section className="card">
            <div className="card-header">
              <h2>Members</h2>
            </div>
            <div className="member-list">
              {(selected.members || []).map((m) => (
                <div className="member-row" key={m.userId}>
                  <div className="group-avatar">{(m.fullName || m.email || 'U').charAt(0).toUpperCase()}</div>
                  <div className="group-row-info">
                    <strong>{m.fullName || m.email}</strong>
                    <span className={`badge ${m.role === 'ADMIN' ? 'badge-info' : 'badge-success'}`}>{m.role}</span>
                  </div>
                  {m.userId !== user?.userId && (
                    <button
                      className="icon-btn icon-btn-danger"
                      onClick={() => handleRemoveMember(m.userId, m.fullName || m.email)}
                      title="Remove member"
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}
            </div>
            <form className="add-member-form" onSubmit={handleAddMember}>
              <input
                type="email"
                placeholder="Enter member's email address"
                value={memberInput}
                onChange={(e) => setMemberInput(e.target.value)}
              />
              <button className="btn btn-primary" type="submit">Add</button>
            </form>
          </section>

          <section className="card">
            <div className="card-header">
              <h2>Group Tasks</h2>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <span className="text-muted">{groupTasks.length} task{groupTasks.length !== 1 ? 's' : ''}</span>
                <button className="btn btn-primary btn-sm" onClick={openTaskModal}>+ Add Task</button>
              </div>
            </div>
            {groupTasks.length === 0 ? (
              <p className="empty-state">No tasks in this group yet.</p>
            ) : (
              <div className="task-list">
                {groupTasks.map((task) => (
                  <div className="task-row" key={task.id}>
                    <div className="task-row-info">
                      <strong style={{ textDecoration: task.status === 'COMPLETED' ? 'line-through' : 'none', opacity: task.status === 'COMPLETED' ? 0.6 : 1 }}>{task.title}</strong>
                      <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexWrap: 'wrap' }}>
                        <span className={`badge ${{ COMPLETED: 'badge-success', IN_PROGRESS: 'badge-warning', PENDING: 'badge-info', TODO: 'badge-info' }[task.status] || 'badge-info'}`}>
                          {task.status?.replace('_', ' ')}
                        </span>
                        <span className={`badge ${task.priority === 'HIGH' ? 'badge-danger' : task.priority === 'MEDIUM' ? 'badge-warning' : 'badge-info'}`}>
                          {task.priority}
                        </span>
                      </div>
                      {task.assignedUsers && task.assignedUsers.length > 0 && (
                        <div className="task-card-assignees" style={{ marginTop: 2 }}>
                          <span className="assignees-label">👤 </span>
                          {task.assignedUsers.map((u) => (
                            <span key={u.id} className="assignee-tag">{u.fullName || u.email}</span>
                          ))}
                        </div>
                      )}
                    </div>
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexShrink: 0 }}>
                      {task.status !== 'COMPLETED' && (
                        <button
                          className="btn btn-sm"
                          style={{ background: 'var(--success, #22c55e)', color: '#fff', border: 'none' }}
                          onClick={() => handleMarkComplete(task)}
                          title="Mark as complete"
                        >✓</button>
                      )}
                      <button className="btn btn-sm btn-secondary" onClick={() => openEditModal(task)} title="Edit task">✏</button>
                      {confirmDeleteId === task.id ? (
                        <>
                          <button className="btn btn-sm btn-danger" onClick={() => handleDeleteGroupTask(task.id)}>Confirm</button>
                          <button className="btn btn-sm btn-secondary" onClick={() => setConfirmDeleteId(null)}>Cancel</button>
                        </>
                      ) : (
                        <button className="btn btn-sm btn-danger" onClick={() => setConfirmDeleteId(task.id)} title="Delete task">🗑</button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      </div>

      {/* Group Task Creation Modal */}
      {showTaskModal && (
        <div className="modal-overlay" onClick={() => setShowTaskModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Add Task to {selected.name}</h2>
              <button className="icon-btn" onClick={() => setShowTaskModal(false)}>✕</button>
            </div>
            {taskError && <p className="alert error">{taskError}</p>}
            <form className="modal-form" onSubmit={handleCreateGroupTask} noValidate>
              <label>
                Title <span className="required">*</span>
                <input
                  name="title"
                  value={taskForm.title}
                  onChange={handleTaskFormChange}
                  placeholder="Task title"
                  autoFocus
                />
              </label>
              <label>
                Description
                <textarea
                  name="description"
                  value={taskForm.description}
                  onChange={handleTaskFormChange}
                  placeholder="Optional description"
                  rows={2}
                />
              </label>
              <div className="form-row">
                <label>
                  Priority
                  <select name="priority" value={taskForm.priority} onChange={handleTaskFormChange}>
                    <option value="HIGH">🔴 High</option>
                    <option value="MEDIUM">🟡 Medium</option>
                    <option value="LOW">🔵 Low</option>
                  </select>
                </label>
                <label>
                  Status
                  <select name="status" value={taskForm.status} onChange={handleTaskFormChange}>
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="PENDING">Pending</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </label>
              </div>
              <label>
                Due Date
                <input type="date" name="dueDate" value={taskForm.dueDate} onChange={handleTaskFormChange} />
              </label>
              {(selected.members || []).filter(m => m.userId !== user?.userId).length > 0 && (
                <div className="assignees-section">
                  <span className="form-label">Assign Members</span>
                  <div className="assignees-avatar-picker">
                    {(selected.members || []).filter(m => m.userId !== user?.userId).map((m) => {
                      const isSelected = (taskForm.assignedUserIds || []).includes(m.userId);
                      const initials = (m.fullName || m.email || 'U').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
                      return (
                        <button
                          type="button"
                          key={m.userId}
                          className={`assignee-avatar-btn${isSelected ? ' selected' : ''}`}
                          onClick={() => toggleTaskAssignee(m.userId)}
                          title={m.fullName || m.email}
                        >
                          <span className="assignee-avatar-circle">{initials}</span>
                          <span className="assignee-avatar-name">{m.fullName || m.email}</span>
                          {isSelected && <span className="assignee-avatar-check">✓</span>}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowTaskModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={taskSaving}>
                  {taskSaving ? 'Creating...' : 'Create Task'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      {/* Edit Task Modal */}
      {showEditModal && (
        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Edit Task</h2>
              <button className="icon-btn" onClick={() => setShowEditModal(false)}>✕</button>
            </div>
            {editError && <p className="alert error">{editError}</p>}
            <form className="modal-form" onSubmit={handleEditGroupTask} noValidate>
              <label>
                Title <span className="required">*</span>
                <input name="title" value={editForm.title} onChange={handleEditFormChange} placeholder="Task title" autoFocus />
              </label>
              <label>
                Description
                <textarea name="description" value={editForm.description} onChange={handleEditFormChange} placeholder="Optional description" rows={2} />
              </label>
              <div className="form-row">
                <label>
                  Priority
                  <select name="priority" value={editForm.priority} onChange={handleEditFormChange}>
                    <option value="HIGH">🔴 High</option>
                    <option value="MEDIUM">🟡 Medium</option>
                    <option value="LOW">🔵 Low</option>
                  </select>
                </label>
                <label>
                  Status
                  <select name="status" value={editForm.status} onChange={handleEditFormChange}>
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="PENDING">Pending</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </label>
              </div>
              <label>
                Due Date
                <input type="date" name="dueDate" value={editForm.dueDate} onChange={handleEditFormChange} />
              </label>
              {(selected.members || []).filter((m) => m.userId !== user?.userId).length > 0 && (
                <div className="assignees-section">
                  <span className="form-label">Assign Members</span>
                  <div className="assignees-avatar-picker">
                    {(selected.members || []).filter((m) => m.userId !== user?.userId).map((m) => {
                      const isSelected = (editForm.assignedUserIds || []).includes(m.userId);
                      const initials = (m.fullName || m.email || 'U').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase();
                      return (
                        <button
                          type="button"
                          key={m.userId}
                          className={`assignee-avatar-btn${isSelected ? ' selected' : ''}`}
                          onClick={() => toggleEditAssignee(m.userId)}
                          title={m.fullName || m.email}
                        >
                          <span className="assignee-avatar-circle">{initials}</span>
                          <span className="assignee-avatar-name">{m.fullName || m.email}</span>
                          {isSelected && <span className="assignee-avatar-check">✓</span>}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowEditModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={editSaving}>
                  {editSaving ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      </>
    );
  }

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div>
          <h1>Groups</h1>
          <p className="page-subtitle">{groups.length} group{groups.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setShowCreate(true); setError(''); }}>+ New Group</button>
      </div>

      {groups.length === 0 ? (
        <div className="empty-state-box">
          <p className="empty-state-title">No groups yet</p>
          <p className="text-muted">Create a group to start collaborating!</p>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ Create Group</button>
        </div>
      ) : (
        <div className="group-cards">
          {groups.map((group) => (
            <div className="group-card" key={group.id} onClick={() => openGroup(group)} style={{ position: 'relative' }}>
              <div className="group-card-avatar">{group.name.charAt(0).toUpperCase()}</div>
              <h3>{group.name}</h3>
              <span className="text-muted">{group.members?.length || 0} member{(group.members?.length || 0) !== 1 ? 's' : ''}</span>
              <div
                className="group-card-actions"
                onClick={(e) => e.stopPropagation()}
                style={{ position: 'absolute', top: 8, right: 8, display: 'flex', gap: 4 }}
              >
                <button
                  className="icon-btn"
                  title="Rename group"
                  onClick={(e) => { e.stopPropagation(); setRenameInput(group.name); setShowRenameModal(group); }}
                >✏</button>
                {confirmDeleteGroupId === group.id ? (
                  <>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDeleteGroup(group.id)}>Confirm</button>
                    <button className="btn btn-sm btn-secondary" onClick={() => setConfirmDeleteGroupId(null)}>Cancel</button>
                  </>
                ) : (
                  <button
                    className="icon-btn icon-btn-danger"
                    title="Delete group"
                    onClick={(e) => { e.stopPropagation(); setConfirmDeleteGroupId(group.id); }}
                  >🗑</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Rename Group Modal */}
      {showRenameModal && (
        <div className="modal-overlay" onClick={() => setShowRenameModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Rename Group</h2>
              <button className="icon-btn" onClick={() => setShowRenameModal(null)}>✕</button>
            </div>
            <form className="modal-form" onSubmit={handleRenameGroup} noValidate>
              <label>
                New Name <span className="required">*</span>
                <input
                  value={renameInput}
                  onChange={(e) => setRenameInput(e.target.value)}
                  placeholder="Group name"
                  autoFocus
                />
              </label>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowRenameModal(null)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={renameSaving}>
                  {renameSaving ? 'Saving...' : 'Rename'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showCreate && (
        <div className="modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Create Group</h2>
              <button className="icon-btn" onClick={() => setShowCreate(false)}>✕</button>
            </div>
            {error && <p className="alert error">{error}</p>}
            <form className="modal-form" onSubmit={handleCreate} noValidate>
              <label>
                Group Name <span className="required">*</span>
                <input
                  value={newName}
                  onChange={(e) => { setNewName(e.target.value); if (error) setError(''); }}
                  placeholder="e.g. Project Alpha"
                  autoFocus
                />
              </label>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Creating...' : 'Create Group'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
