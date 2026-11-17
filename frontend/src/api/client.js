const TOKEN_KEY = 'otms.token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function parseToken(token) {
  try {
    const payload = token.split('.')[1];
    const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    return { email: json.sub, name: json.name, role: json.role };
  } catch {
    return null;
  }
}

async function request(path, { method = 'GET', body } = {}) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const error = new Error(data?.message || `Request failed (${response.status})`);
    error.status = response.status;
    error.data = data;
    if (response.status === 401) clearToken();
    throw error;
  }
  return data;
}

function query(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value) search.set(key, value);
  });
  const encoded = search.toString();
  return encoded ? `?${encoded}` : '';
}

export const api = {
  login: (email, password) =>
    request('/api/auth/login', { method: 'POST', body: { email, password } }),
  listTheatres: () => request('/api/theatres'),
  listDoctors: () => request('/api/staff?role=DOCTOR'),
  listSurgeries: (params = {}) => request(`/api/surgeries${query(params)}`),
  listEmergencies: () => request('/api/surgeries/emergencies'),
  createSurgery: (surgery) => request('/api/surgeries', { method: 'POST', body: surgery }),
  updateStatus: (id, status) =>
    request(`/api/surgeries/${id}/status`, { method: 'PATCH', body: { status } }),
};
