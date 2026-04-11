const API_BASE = process.env.REACT_APP_API_BASE || '';

function getToken() {
  return localStorage.getItem('token');
}

function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(url, options = {}) {
  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...options.headers,
    },
  });

  if (!res.ok) {
    const body = await res.json().catch(() => null);
    const message = body?.message || body?.error || `Request failed (${res.status})`;
    const error = new Error(message);
    error.status = res.status;
    throw error;
  }

  if (res.status === 204) return null;
  return res.json();
}

// Auth
export const authApi = {
  register: (data) => request('/api/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/api/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  me: () => request('/api/auth/me'),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
  googleAuth: (idToken) => request('/api/auth/oauth/google', { method: 'POST', body: JSON.stringify({ idToken }) }),
};

// Tasks
export const taskApi = {
  list: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return request(`/api/tasks${query ? `?${query}` : ''}`);
  },
  get: (id) => request(`/api/tasks/${id}`),
  create: (data) => request('/api/tasks', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/api/tasks/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/api/tasks/${id}`, { method: 'DELETE' }),
};

// Groups
export const groupApi = {
  list: () => request('/api/groups'),
  get: (id) => request(`/api/groups/${id}`),
  create: (data) => request('/api/groups', { method: 'POST', body: JSON.stringify(data) }),
  addMember: (groupId, email) => request(`/api/groups/${groupId}/members`, { method: 'POST', body: JSON.stringify({ email }) }),
  removeMember: (groupId, userId) => request(`/api/groups/${groupId}/members/${userId}`, { method: 'DELETE' }),
  getTasks: (groupId, params = {}) => {
    const query = new URLSearchParams(params).toString();
    return request(`/api/groups/${groupId}/tasks${query ? `?${query}` : ''}`);
  },
};

// Files
export const fileApi = {
  list: () => request('/api/files'),
  getByTask: (taskId) => request(`/api/files/task/${taskId}`),
  upload: (file, taskId) => {
    const formData = new FormData();
    formData.append('file', file);
    if (taskId) formData.append('taskId', taskId);
    return fetch(`${API_BASE}/api/files`, {
      method: 'POST',
      headers: authHeaders(),
      body: formData,
    }).then(res => {
      if (!res.ok) throw new Error('Upload failed');
      return res.json();
    });
  },
  delete: (id) => request(`/api/files/${id}`, { method: 'DELETE' }),
  downloadUrl: (id) => `${API_BASE}/api/files/${id}/download`,
};
