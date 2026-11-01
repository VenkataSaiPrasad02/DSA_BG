import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login, loginWithGoogle } from '../services/authService';
import '../styles/Login.css';

/**
 * Login page — two-step flow:
 *   Step 1: POST /auth/login  { login, password }  → backend sends OTP email
 *   Step 2: redirect to /verify-otp?context=login&email=<email>
 *           VerifyOtp page posts /auth/login/verify-otp and stores the JWT
 */
const Login = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();

    const [formData, setFormData] = useState({ login: '', password: '' });
    const [passwordVisible, setPasswordVisible] = useState(false);
    const [loading, setLoading] = useState(false);
    
    // Initialize error from query params to avoid setting state in effect
    const [error, setError] = useState(() => {
        const params = new URLSearchParams(location.search);
        const reason = params.get('error');
        if (reason === 'session_expired') {
            return 'Your session has expired. Please sign in again.';
        } else if (reason === 'auth_failed') {
            return 'Social login failed. Please try again.';
        }
        return '';
    });

    // Redirect already-authenticated users straight to the dashboard
    useEffect(() => {
        if (user) navigate('/dashboard', { replace: true });
    }, [user, navigate]);

    const handleChange = (e) => {
        setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // Step 1: validates credentials and triggers an OTP email
            await login(formData);

            // Determine the email to pre-fill on the OTP page.
            // If the user entered an email address use it directly;
            // otherwise the OTP page lets them type it.
            const emailHint = formData.login.includes('@')
                ? formData.login.trim().toLowerCase()
                : '';

            // Pass credentials to VerifyOtp for resend functionality
            navigate(`/verify-otp?context=login&email=${encodeURIComponent(emailHint)}`, {
                state: { loginCredentials: formData }
            });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Invalid credentials. Please try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h2 className="login-title">DSA Battlegrounds</h2>
                <p className="login-subtitle">Sign in to your account</p>

                {error && (
                    <div className="login-error-alert" role="alert">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="login-form" noValidate>
                    <input
                        type="text"
                        name="login"
                        placeholder="Username or Email"
                        value={formData.login}
                        onChange={handleChange}
                        className="login-input"
                        autoComplete="username"
                        required
                        disabled={loading}
                    />

                    <div className="login-password-wrapper">
                        <input
                            type={passwordVisible ? 'text' : 'password'}
                            name="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            className="login-input login-input--password"
                            autoComplete="current-password"
                            required
                            disabled={loading}
                        />
                        <button
                            type="button"
                            className="login-password-toggle"
                            onClick={() => setPasswordVisible((v) => !v)}
                            aria-label={
                                passwordVisible ? 'Hide password' : 'Show password'
                            }
                            tabIndex={0}
                        >
                            {passwordVisible ? '🙈' : '👁'}
                        </button>
                    </div>

                    <div className="login-forgot-wrapper">
                        <span
                            onClick={() => navigate('/forgot-password')}
                            className="login-forgot-link"
                            role="button"
                            tabIndex={0}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' || e.key === ' ')
                                    navigate('/forgot-password');
                            }}
                        >
                            Forgot Password?
                        </span>
                    </div>

                    <button
                        type="submit"
                        className="login-submit-btn"
                        disabled={loading}
                    >
                        {loading ? 'Sending OTP…' : 'Sign In'}
                    </button>
                </form>

                <div className="login-divider-container">
                    <span className="login-divider-line" />
                    <span className="login-divider-text">or continue with</span>
                    <span className="login-divider-line" />
                </div>

                <div className="login-social-container">
                    <button
                        type="button"
                        onClick={loginWithGoogle}
                        className="login-social-btn login-social-google"
                    >
                        <span className="login-social-icon">G</span>
                        Continue with Google
                    </button>
                </div>

                <p className="login-footer-text">
                    Don&apos;t have an account?{' '}
                    <span
                        onClick={() => navigate('/register')}
                        className="login-link-text"
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' || e.key === ' ') {
                                navigate('/register');
                            }
                        }}
                    >
                        Register here
                    </span>
                </p>
            </div>
        </div>
    );
};

export default Login;
