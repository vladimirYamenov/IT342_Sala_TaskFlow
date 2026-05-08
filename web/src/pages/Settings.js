import { useState } from 'react';

export default function Settings({ user, addToast }) {
  const nameParts = (user?.fullName || '').split(' ');
  const [firstName, setFirstName] = useState(nameParts[0] || '');
  const [lastName, setLastName]   = useState(nameParts.slice(1).join(' ') || '');
  const [email, setEmail]         = useState(user?.email || '');
  const [bio, setBio]             = useState('');

  const [notifications, setNotifications] = useState({
    email: true,
    push: true,
    reminders: true,
    groupActivity: false,
  });

  const [language, setLanguage] = useState('English');
  const [timezone, setTimezone] = useState('UTC+8 (PST)');

  const handleSaveProfile = (e) => {
    e.preventDefault();
    addToast('Profile updated successfully', 'success');
  };

  const toggleNotification = (key) => {
    setNotifications((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const initials = ((firstName[0] || '') + (lastName[0] || '')).toUpperCase() || 'U';

  return (
    <div className="page-content settings-page">
      <div className="settings-header">
        <h1 className="page-title">Settings</h1>
        <p className="page-subtitle">Manage your account and preferences</p>
      </div>

      <div className="settings-layout">
        {/* ── Left column ── */}
        <div className="settings-main">

          {/* Profile Information */}
          <section className="settings-card">
            <h2 className="settings-section-title">Profile Information</h2>
            <form onSubmit={handleSaveProfile}>
              <div className="profile-avatar-row">
                <div className="settings-avatar">{initials}</div>
                <button type="button" className="btn-change-photo">
                  📷 Change Photo
                </button>
              </div>

              <div className="settings-form-row">
                <div className="settings-field">
                  <label className="field-label">First Name</label>
                  <input
                    className="field-input"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    placeholder="First name"
                  />
                </div>
                <div className="settings-field">
                  <label className="field-label">Last Name</label>
                  <input
                    className="field-input"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    placeholder="Last name"
                  />
                </div>
              </div>

              <div className="settings-field">
                <label className="field-label">Email</label>
                <input
                  className="field-input"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Email address"
                />
              </div>

              <div className="settings-field">
                <label className="field-label">Bio</label>
                <input
                  className="field-input"
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  placeholder="Tell us about yourself"
                />
              </div>

              <div className="settings-form-actions">
                <button type="submit" className="btn-save">Save Changes</button>
              </div>
            </form>
          </section>

          {/* Notifications */}
          <section className="settings-card">
            <h2 className="settings-section-title">Notifications</h2>

            <div className="notification-row">
              <div>
                <p className="notif-title">Email Notifications</p>
                <p className="notif-desc">Receive email updates about your tasks</p>
              </div>
              <Toggle
                checked={notifications.email}
                onChange={() => toggleNotification('email')}
              />
            </div>

            <div className="notification-row">
              <div>
                <p className="notif-title">Push Notifications</p>
                <p className="notif-desc">Get push notifications in your browser</p>
              </div>
              <Toggle
                checked={notifications.push}
                onChange={() => toggleNotification('push')}
              />
            </div>

            <div className="notification-row">
              <div>
                <p className="notif-title">Task Reminders</p>
                <p className="notif-desc">Remind me about upcoming deadlines</p>
              </div>
              <Toggle
                checked={notifications.reminders}
                onChange={() => toggleNotification('reminders')}
              />
            </div>

            <div className="notification-row">
              <div>
                <p className="notif-title">Group Activity</p>
                <p className="notif-desc">Notify me about group updates</p>
              </div>
              <Toggle
                checked={notifications.groupActivity}
                onChange={() => toggleNotification('groupActivity')}
              />
            </div>
          </section>
        </div>

        {/* ── Right column ── */}
        <div className="settings-side">

          {/* Account */}
          <section className="settings-card">
            <h2 className="settings-section-title">Account</h2>
            <button className="btn-account-action" onClick={() => addToast('Coming soon', 'info')}>
              Change Password
            </button>
            <button className="btn-account-action" onClick={() => addToast('Coming soon', 'info')}>
              Privacy Settings
            </button>
            <button className="btn-account-action danger" onClick={() => addToast('Coming soon', 'info')}>
              Delete Account
            </button>
          </section>

          {/* Preferences */}
          <section className="settings-card">
            <h2 className="settings-section-title">Preferences</h2>

            <div className="settings-field">
              <label className="field-label">Language</label>
              <select
                className="field-input"
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
              >
                <option>English</option>
                <option>Filipino</option>
                <option>Spanish</option>
                <option>French</option>
              </select>
            </div>

            <div className="settings-field">
              <label className="field-label">Timezone</label>
              <select
                className="field-input"
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
              >
                <option>UTC+8 (PST)</option>
                <option>UTC-8 (PST)</option>
                <option>UTC+0 (GMT)</option>
                <option>UTC+9 (JST)</option>
              </select>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}

function Toggle({ checked, onChange }) {
  return (
    <button
      type="button"
      onClick={onChange}
      className={`toggle-switch${checked ? ' on' : ''}`}
      aria-pressed={checked}
    >
      <span className="toggle-knob" />
    </button>
  );
}
