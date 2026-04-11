import { useCallback, useEffect, useState } from 'react';
import { groupApi } from '../api';

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

  if (loading) return <div className="page-loader">Loading groups...</div>;

  if (selected) {
    return (
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
              <span className="text-muted">{groupTasks.length} task{groupTasks.length !== 1 ? 's' : ''}</span>
            </div>
            {groupTasks.length === 0 ? (
              <p className="empty-state">No tasks in this group yet.</p>
            ) : (
              <div className="task-list">
                {groupTasks.map((task) => (
                  <div className="task-row" key={task.id}>
                    <div className="task-row-info">
                      <strong>{task.title}</strong>
                      <span className="text-muted">{task.status?.replace('_', ' ')}</span>
                    </div>
                    <span className={`badge ${task.priority === 'HIGH' ? 'badge-danger' : task.priority === 'MEDIUM' ? 'badge-warning' : 'badge-info'}`}>
                      {task.priority}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      </div>
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
            <div className="group-card" key={group.id} onClick={() => openGroup(group)}>
              <div className="group-card-avatar">{group.name.charAt(0).toUpperCase()}</div>
              <h3>{group.name}</h3>
              <span className="text-muted">{group.members?.length || 0} member{(group.members?.length || 0) !== 1 ? 's' : ''}</span>
            </div>
          ))}
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
