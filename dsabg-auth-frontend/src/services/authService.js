import API from '../api/axios';

export const signUp = (data) => API.post('/auth/signUp', data);
export const signUpOtpVerification = (data) => API.post('/auth/signUp/verify-otp', data);
export const login = (data) => API.post('/auth/login', data);
export const loginOtpVerification = (data) => API.post('/auth/login/verify-otp', data);
export const forgotPassword = (data) => API.post('/auth/forgot-password', data);
export const resetPassword = (data) => API.post('/auth/reset-password', data);
export const loginWithGoogle = () => {
    const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    window.location.href = base + '/oauth2/authorization/google';
};
