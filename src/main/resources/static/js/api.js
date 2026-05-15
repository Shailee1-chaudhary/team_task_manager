// ===== API Utility =====
const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('token');
}

function getUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

function setAuth(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
        id: data.id,
        name: data.name,
        email: data.email,
        role: data.role
    }));
}

function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
}

function isLoggedIn() {
    return !!getToken();
}

function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    const options = { method, headers };
    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(API_BASE + endpoint, options);

        if (response.status === 401 || response.status === 403) {
            if (endpoint !== '/auth/login' && endpoint !== '/auth/signup') {
                clearAuth();
                window.location.href = '/login.html';
                return null;
            }
        }

        const data = await response.json();

        if (!response.ok) {
            throw { status: response.status, data };
        }

        return data;
    } catch (error) {
        if (error.status) throw error;
        throw { status: 0, data: { message: 'Network error. Please try again.' } };
    }
}

// Auth API
async function signup(name, email, password, role) {
    return apiCall('/auth/signup', 'POST', { name, email, password, role });
}

async function login(email, password) {
    return apiCall('/auth/login', 'POST', { email, password });
}

function logout() {
    clearAuth();
    window.location.href = '/login.html';
}

// Project API
async function getProjects() { return apiCall('/projects'); }
async function getProject(id) { return apiCall('/projects/' + id); }
async function createProject(name, description) { return apiCall('/projects', 'POST', { name, description }); }
async function updateProject(id, name, description) { return apiCall('/projects/' + id, 'PUT', { name, description }); }
async function deleteProject(id) { return apiCall('/projects/' + id, 'DELETE'); }
async function addMember(projectId, userId) { return apiCall('/projects/' + projectId + '/members/' + userId, 'POST'); }
async function removeMember(projectId, userId) { return apiCall('/projects/' + projectId + '/members/' + userId, 'DELETE'); }

// Task API
async function getTasksByProject(projectId) { return apiCall('/tasks/project/' + projectId); }
async function getMyTasks() { return apiCall('/tasks/my-tasks'); }
async function getTask(id) { return apiCall('/tasks/' + id); }
async function createTask(data) { return apiCall('/tasks', 'POST', data); }
async function updateTask(id, data) { return apiCall('/tasks/' + id, 'PUT', data); }
async function updateTaskStatus(id, status) { return apiCall('/tasks/' + id + '/status', 'PATCH', { status }); }
async function deleteTask(id) { return apiCall('/tasks/' + id, 'DELETE'); }

// User API
async function getAllUsers() { return apiCall('/users/all'); }

// Dashboard API
async function getDashboard() { return apiCall('/dashboard'); }

// ===== UI Helpers =====
function showAlert(containerId, message, type = 'error') {
    const el = document.getElementById(containerId);
    if (el) {
        el.className = 'alert alert-' + type;
        el.textContent = message;
        el.style.display = 'block';
        setTimeout(() => { el.style.display = 'none'; }, 5000);
    }
}

function statusBadge(status) {
    const map = { 'TODO': 'todo', 'IN_PROGRESS': 'progress', 'DONE': 'done' };
    const labels = { 'TODO': 'To Do', 'IN_PROGRESS': 'In Progress', 'DONE': 'Done' };
    return `<span class="badge badge-${map[status] || 'todo'}">${labels[status] || status}</span>`;
}

function priorityBadge(priority) {
    const map = { 'LOW': 'low', 'MEDIUM': 'medium', 'HIGH': 'high' };
    return `<span class="badge badge-${map[priority] || 'medium'}">${priority}</span>`;
}

function roleBadge(role) {
    return `<span class="badge badge-${role.toLowerCase()}">${role}</span>`;
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function initSidebar() {
    const user = getUser();
    if (!user) return;

    document.querySelectorAll('.user-name').forEach(el => el.textContent = user.name);
    document.querySelectorAll('.user-email').forEach(el => el.textContent = user.email);
    document.querySelectorAll('.user-role-badge').forEach(el => {
        el.textContent = user.role;
        el.className = 'user-role ' + (user.role === 'ADMIN' ? 'badge badge-admin' : 'badge badge-member');
    });

    // Highlight active nav
    const path = window.location.pathname;
    document.querySelectorAll('.sidebar-nav a').forEach(a => {
        if (a.getAttribute('href') === path) a.classList.add('active');
    });
}
