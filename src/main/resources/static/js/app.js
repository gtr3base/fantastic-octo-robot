const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('accessToken');
}

function setToken(token) {
    localStorage.setItem('accessToken', token);
}

function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}

function setRefreshToken(token) {
    localStorage.setItem('refreshToken', token);
}

window.logout = function() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login';
}

async function refreshToken() {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
        logout();
        return null;
    }

    try {
        const response = await fetch(`${API_BASE}/user/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: refreshToken })
        });

        if (response.ok) {
            const data = await response.json();
            if (data.accessToken) {
                setToken(data.accessToken);
            } else if (data.loginToken) {
                setToken(data.loginToken);
            }
            if (data.refreshToken) {
                setRefreshToken(data.refreshToken);
            }
            return getToken();
        }
    } catch (err) {
        console.error('Token refresh failed:', err);
    }

    logout();
    return null;
}

async function apiFetch(endpoint, options = {}) {
    let token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };

    let response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        const newToken = await refreshToken();
        if (newToken) {
            headers['Authorization'] = `Bearer ${newToken}`;
            response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
        } else {
            logout();
            return;
        }
    }

    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'An error occurred');
    }

    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
        return response.json();
    } else {
        return response.text();
    }
}

export { getToken, setToken, getRefreshToken, setRefreshToken, logout, apiFetch, refreshToken };