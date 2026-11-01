
import axios from 'axios';

const API = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add JWT to outgoing requests
API.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// Handle expired or invalid sessions
API.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            const hadToken = Boolean(localStorage.getItem('token'));

            if (hadToken) {
                localStorage.removeItem('token');
                window.location.href = '/login?error=session_expired';
            }
        }

        return Promise.reject(error);
    }
);

export default API;
