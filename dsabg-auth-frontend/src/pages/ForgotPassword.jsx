import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { forgotPassword } from '../services/authService';
import '../styles/ForgotPassword.css';

/**
 * Forgot Password page — step 1 of the password-reset flow.
 *
 * POST /auth/forgot-password { email }
 * → backend generates a 3-minute OTP and sends it to the email address.
 * → navigate to /reset-password with the email pre-filled.
 *
 * Generic success message is shown regardless of whether the email
 * exists, to avoid account enumeration.
 */
const ForgotPassword = () => {
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [submitted, setSubmitted] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await forgotPassword({ email: email.trim().toLowerCase() });
            setSubmitted(true);
        } catch (err) {
            // Diagnostic: log the exact error shape so root cause is visible
            console.error('[ForgotPassword] request failed', {
                status:       err.response?.status,
                responseData: err.response?.data,
                message:      err.message,
                requestURL:   err.config?.url,
                baseURL:      err.config?.baseURL,
            });
            setError(
                err.response?.data?.message ||
                'Something went wrong. Please try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    const handleContinue = () => {
        navigate(
            `/reset-password?email=${encodeURIComponent(email.trim().toLowerCase())}`
        );
    };

    return (
        <div className="forgot-container">
            <div className="forgot-card">
                <div className="forgot-icon" aria-hidden="true">🔐</div>

                <h2 className="forgot-title">Forgot Password?</h2>
                <p className="forgot-subtitle">
                    Enter your registered email address and we&apos;ll send you a
                    verification code to reset your password.
                </p>

                {error && (
                    <div className="forgot-alert forgot-alert--error" role="alert">
                        {error}
                    </div>
                )}

                {submitted ? (
                    <div className="forgot-success">
                        <div className="forgot-alert forgot-alert--success" role="status">
                            If an account exists for <strong>{email}</strong>, a
                            verification code has been sent. Check your inbox.
                        </div>
                        <p className="forgot-otp-note">
                            The code expires in <strong>3 minutes</strong>.
                        </p>
                        <button
                            type="button"
                            className="forgot-submit-btn"
                            onClick={handleContinue}
                        >
                            Enter Verification Code
                        </button>
                        <button
                            type="button"
                            className="forgot-resend-btn"
                            onClick={() => {
                                setSubmitted(false);
                                setError('');
                            }}
                        >
                            Use a different email
                        </button>
                    </div>
                ) : (
                    <form
                        onSubmit={handleSubmit}
                        className="forgot-form"
                        noValidate
                    >
                        <input
                            type="email"
                            placeholder="Email Address"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="forgot-input"
                            autoComplete="email"
                            required
                            disabled={loading}
                        />

                        <button
                            type="submit"
                            className="forgot-submit-btn"
                            disabled={loading || !email.trim()}
                        >
                            {loading ? 'Sending…' : 'Send Verification Code'}
                        </button>
                    </form>
                )}

                <button
                    type="button"
                    className="forgot-back-link"
                    onClick={() => navigate('/login')}
                >
                    ← Back to Sign In
                </button>
            </div>
        </div>
    );
};

export default ForgotPassword;
