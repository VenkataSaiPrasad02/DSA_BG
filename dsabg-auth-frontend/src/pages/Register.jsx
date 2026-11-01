import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { signUp } from '../services/authService';
import '../styles/Register.css';

/**
 * Registration page — step 1 of the signup flow:
 *   POST /auth/register { username, email, phone, password }
 *   → backend creates pending registration and sends signup OTP to email
 *   → navigate to /verify-otp?context=signup&email=<email>
 */
const Register = () => {
    const navigate = useNavigate();
    const { user } = useAuth();

    const [formData, setFormData] = useState({
        username: '',
        email: '',
        phone: '',
        password: '',
    });
    const [passwordVisible, setPasswordVisible] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Already-authenticated users should not be on this page
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
            await signUp(formData);

            // Registration accepted — backend has sent the signup OTP.
            // Pass the email so the OTP page can pre-fill and display it.
            const email = formData.email.trim().toLowerCase();
            navigate(
                `/verify-otp?context=signup&email=${encodeURIComponent(email)}`
            );
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Registration failed. Please try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-container">
            <div className="register-card">
                <h2 className="register-title">Create Account</h2>
                <p className="register-subtitle">Join DSA Battlegrounds today</p>

                {error && (
                    <div className="register-error" role="alert">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="register-form" noValidate>
                    <input
                        type="text"
                        name="username"
                        placeholder="Username"
                        value={formData.username}
                        onChange={handleChange}
                        className="register-input"
                        autoComplete="username"
                        required
                        disabled={loading}
                    />

                    <input
                        type="email"
                        name="email"
                        placeholder="Email Address"
                        value={formData.email}
                        onChange={handleChange}
                        className="register-input"
                        autoComplete="email"
                        required
                        disabled={loading}
                    />

                    <input
                        type="tel"
                        name="phone"
                        placeholder="Phone Number (10 digits)"
                        value={formData.phone}
                        onChange={handleChange}
                        className="register-input"
                        autoComplete="tel"
                        inputMode="tel"
                        pattern="[0-9]{10}"
                        maxLength={10}
                        title="Enter a valid 10-digit phone number"
                        required
                        disabled={loading}
                    />

                    <div className="register-password-wrapper">
                        <input
                            type={passwordVisible ? 'text' : 'password'}
                            name="password"
                            placeholder="Password (min 6 characters)"
                            value={formData.password}
                            onChange={handleChange}
                            className="register-input register-input--password"
                            autoComplete="new-password"
                            minLength={6}
                            required
                            disabled={loading}
                        />
                        <button
                            type="button"
                            className="register-password-toggle"
                            onClick={() => setPasswordVisible((v) => !v)}
                            aria-label={
                                passwordVisible ? 'Hide password' : 'Show password'
                            }
                            tabIndex={0}
                        >
                            {passwordVisible ? '🙈' : '👁'}
                        </button>
                    </div>

                    <button
                        type="submit"
                        className="register-submit-btn"
                        disabled={loading}
                    >
                        {loading ? 'Creating account…' : 'Register'}
                    </button>
                </form>

                <p className="register-footer">
                    Already have an account?{' '}
                    <span
                        onClick={() => navigate('/login')}
                        className="register-link"
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' || e.key === ' ') {
                                navigate('/login');
                            }
                        }}
                    >
                        Sign In
                    </span>
                </p>
            </div>
        </div>
    );
};

export default Register;
