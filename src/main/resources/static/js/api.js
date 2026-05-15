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
    // Check server instance in background (non-blocking)
    checkServerInstance();
    return true;
}

// Check if server has restarted by comparing instance IDs
async function checkServerInstance() {
    try {
        const response = await fetch('/api/health');
        const data = await response.json();
        if (data && data.success && data.data && data.data.serverInstanceId) {
            const currentId = data.data.serverInstanceId;
            const storedId = localStorage.getItem('serverInstanceId');
            if (storedId && storedId !== currentId) {
                // Server restarted - force logout
                clearAuth();
                window.location.href = '/login.html';
                return;
            }
            // Store the current server instance ID
            localStorage.setItem('serverInstanceId', currentId);
        }
    } catch (e) {
        // Network error - ignore, don't force logout
        console.warn('Could not check server instance:', e);
    }
}

// Store server instance ID on login
function storeServerInstanceId(instanceId) {
    if (instanceId) {
        localStorage.setItem('serverInstanceId', instanceId);
    }
}

// Refresh (store without comparing) the server instance ID — use after login/signup
// so we don't accidentally clear the token we just received.
async function refreshServerInstanceId() {
    try {
        const response = await fetch('/api/health');
        const data = await response.json();
        if (data && data.success && data.data && data.data.serverInstanceId) {
            localStorage.setItem('serverInstanceId', data.data.serverInstanceId);
        }
    } catch (e) {
        console.warn('Could not refresh server instance ID:', e);
    }
}

// Async version: validates the token is still valid by calling the server
// Use this on protected pages to handle stale tokens after H2 restart
async function validateAuthAsync() {
    if (!isLoggedIn()) {
        window.location.href = '/login.html';
        return false;
    }
    try {
        const result = await apiCall('/dashboard');
        if (result && result.success) {
            return true;
        }
    } catch (e) {
        // Token is stale or user no longer exists (H2 wiped)
        clearAuth();
        window.location.href = '/login.html';
        return false;
    }
    clearAuth();
    window.location.href = '/login.html';
    return false;
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

        // Only auto-logout on 401 (Unauthorized = invalid/expired token)
        // 403 (Forbidden) means authenticated but not authorized for the resource — stay logged in
        if (response.status === 401) {
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
    localStorage.removeItem('serverInstanceId');
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

// Progress Notes API
async function getTaskProgress(taskId) { return apiCall('/tasks/' + taskId + '/progress'); }
async function addTaskProgress(taskId, content) { return apiCall('/tasks/' + taskId + '/progress', 'POST', { content }); }
async function deleteTaskProgress(taskId, progressId) { return apiCall('/tasks/' + taskId + '/progress/' + progressId, 'DELETE'); }

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
    const map = { 'TODO': 'todo', 'IN_PROGRESS': 'progress', 'BLOCKED': 'blocked', 'CODE_REVIEW': 'code-review', 'QA_TESTING': 'qa-testing', 'QA_TESTING_FAILED': 'qa-failed', 'DONE': 'done' };
    const labels = { 'TODO': 'To Do', 'IN_PROGRESS': 'In Progress', 'BLOCKED': 'Blocked', 'CODE_REVIEW': 'Code Review', 'QA_TESTING': 'QA Testing', 'QA_TESTING_FAILED': 'QA Failed', 'DONE': 'Done' };
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

function formatDateTime(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function storyPointsBadge(points) {
    if (points === null || points === undefined) return '';
    return `<span class="badge badge-sp" title="Story Points">${points} SP</span>`;
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
