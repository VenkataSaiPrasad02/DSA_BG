import { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { verifySignupOtp, verifyLoginOtp, loginUser } from '../services/authService';
import '../styles/VerifyOtp.css';

/**
 * OTP Verification page — shared for both signup and login flows.
 *
 * Query params:
 *   context = "signup" | "login"
 *   email   = the email address the OTP was sent to (pre-fills the field)
 *
 * Signup flow:
 *   POST /auth/signup/verify-otp { email, otp }
 *   → success → redirect to /login
 *
 * Login flow:
 *   POST /auth/login/verify-otp { email, otp }
 *   → success → store JWT → redirect to /dashboard
 *
 * Resend:
 *   Signup: re-calls POST /auth/register with the stored payload (not possible
 *           here since we don't keep the password) — instead we navigate back
 *           to /register with a message so the user re-submits.
 *   Login:  re-calls POST /auth/login with the stored login+password from
 *           location.state (if present), otherwise navigates back to /login.
 */
const RESEND_COOLDOWN_SEC = 60;

const VerifyOtp = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { login: storeToken } = useAuth();

    const params = new URLSearchParams(location.search);
    const context = params.get('context'); // "signup" | "login"
    const emailParam = params.get('email') || '';

    // Credentials passed via location.state for resend support on login flow
    const loginCredentials = location.state?.loginCredentials || null;

    const [email, setEmail] = useState(emailParam);
    const [otp, setOtp] = useState('');
    const [loading, setLoading] = useState(false);
    const [resending, setResending] = useState(false);
    const [error, setError] = useState('');
    const [successMsg, setSuccessMsg] = useState('');
    const [countdown, setCountdown] = useState(0);

    const timerRef = useRef(null);
    const otpInputRef = useRef(null);

    // Focus OTP input on mount
    useEffect(() => {
        otpInputRef.current?.focus();
    }, []);

    // Countdown timer for the Resend button
    useEffect(() => {
        if (countdown <= 0) {
            clearInterval(timerRef.current);
            return;
        }
        timerRef.current = setInterval(() => {
            setCountdown((c) => {
                if (c <= 1) {
                    clearInterval(timerRef.current);
                    return 0;
                }
                return c - 1;
            });
        }, 1000);
        return () => clearInterval(timerRef.current);
    }, [countdown]);

    // Redirect if context is missing or invalid
    useEffect(() => {
        if (context !== 'signup' && context !== 'login') {
            navigate('/login', { replace: true });
        }
    }, [context, navigate]);

    const isSignup = context === 'signup';

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccessMsg('');

        if (!email.trim()) {
            setError('Please enter your email address.');
            return;
        }
        if (otp.trim().length < 4) {
            setError('Please enter the verification code.');
            return;
        }

        setLoading(true);
        try {
            if (isSignup) {
                await verifySignupOtp({ email: email.trim().toLowerCase(), otp: otp.trim() });
                setSuccessMsg('Email verified! Redirecting to sign in…');
                setTimeout(() => navigate('/login'), 1500);
            } else {
                const res = await verifyLoginOtp({
                    email: email.trim().toLowerCase(),
                    otp: otp.trim(),
                });
                // AuthResponse: { message, token, tokenType }
                const token = res.data?.token;
                if (token) {
                    storeToken(token);
                    navigate('/dashboard', { replace: true });
                } else {
                    setError('Unexpected response from server. Please try again.');
                }
            }
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Invalid or expired code. Please try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    const handleResend = async () => {
        if (countdown > 0 || resending) return;
        setError('');
        setSuccessMsg('');

        if (isSignup) {
            // We don't hold the registration payload here — send the user back
            // to the Register page so they can re-submit (which re-triggers the OTP).
            navigate('/register', {
                state: { resendMessage: 'Please submit the form again to resend the code.' },
            });
            return;
        }

        // Login resend: we need the original credentials stored in location.state
        if (!loginCredentials) {
            navigate('/login', {
                state: { resendMessage: 'Please sign in again to resend the code.' },
            });
            return;
        }

        setResending(true);
        try {
            await loginUser(loginCredentials);
            setSuccessMsg('A new code has been sent to your email.');
            setOtp('');
            setCountdown(RESEND_COOLDOWN_SEC);
            otpInputRef.current?.focus();
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Could not resend the code. Please try again.'
            );
        } finally {
            setResending(false);
        }
    };

    const pageTitle = isSignup ? 'Verify Your Email' : 'Enter Login Code';
    const pageSubtitle = isSignup
        ? 'Check your inbox for the signup verification code.'
        : 'We emailed you a one-time login code.';

    return (
        <div className="otp-container">
            <div className="otp-card">
                <div className="otp-icon" aria-hidden="true">✉️</div>

                <h2 className="otp-title">{pageTitle}</h2>
                <p className="otp-subtitle">{pageSubtitle}</p>

                {email && (
                    <p className="otp-email-hint">
                        Code sent to <strong>{email}</strong>
                    </p>
                )}

                {error && (
                    <div className="otp-alert otp-alert--error" role="alert">
                        {error}
                    </div>
                )}

                {successMsg && (
                    <div className="otp-alert otp-alert--success" role="status">
                        {successMsg}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="otp-form" noValidate>
                    {!emailParam && (
                        <input
                            type="email"
                            placeholder="Your email address"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="otp-input"
                            autoComplete="email"
                            required
                            disabled={loading}
                        />
                    )}

                    <input
                        ref={otpInputRef}
                        type="text"
                        inputMode="numeric"
                        placeholder="Enter verification code"
                        value={otp}
                        onChange={(e) =>
                            setOtp(e.target.value.replace(/\D/g, '').slice(0, 8))
                        }
                        className="otp-input otp-input--code"
                        autoComplete="one-time-code"
                        required
                        disabled={loading}
                        maxLength={8}
                        aria-label="Verification code"
                    />

                    <button
                        type="submit"
                        className="otp-submit-btn"
                        disabled={loading}
                    >
                        {loading ? 'Verifying…' : 'Verify Code'}
                    </button>
                </form>

                <div className="otp-resend-section">
                    <p className="otp-resend-text">Didn&apos;t receive the code?</p>
                    <button
                        type="button"
                        className="otp-resend-btn"
                        onClick={handleResend}
                        disabled={countdown > 0 || resending || loading}
                    >
                        {resending
                            ? 'Sending…'
                            : countdown > 0
                            ? `Resend in ${countdown}s`
                            : 'Resend Code'}
                    </button>
                </div>

                <p className="otp-expiry-note">
                    {isSignup
                        ? 'Code expires in 2 minutes.'
                        : 'Code expires in 2 minutes.'}
                </p>

                <button
                    type="button"
                    className="otp-back-link"
                    onClick={() => navigate(isSignup ? '/register' : '/login')}
                >
                    ← Back to {isSignup ? 'Registration' : 'Sign In'}
                </button>
            </div>
        </div>
    );
};

export default VerifyOtp;
