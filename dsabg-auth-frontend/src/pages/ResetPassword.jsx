import { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { forgotPassword, resetPassword } from '../services/authService';
import '../styles/ResetPassword.css';



const RESEND_COOLDOWN_SEC = 60;

const ResetPassword = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const emailParam = new URLSearchParams(location.search).get('email') || '';

    const [email, setEmail] = useState(emailParam);
    const [otp, setOtp] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [newPasswordVisible, setNewPasswordVisible] = useState(false);
    const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);

    const [loading, setLoading] = useState(false);
    const [resending, setResending] = useState(false);
    const [countdown, setCountdown] = useState(0);

    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const otpInputRef = useRef(null);
    const timerRef = useRef(null);

    useEffect(() => {
        otpInputRef.current?.focus();
    }, []);

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

    const validate = () => {
        if (!email.trim()) return 'Please enter your email address.';
        if (otp.trim().length < 4) return 'Please enter the verification code.';
        if (newPassword.length < 6)
            return 'Password must be at least 6 characters.';
        if (newPassword !== confirmPassword)
            return 'Passwords do not match.';
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setLoading(true);
        try {
            await resetPassword({
                email: email.trim().toLowerCase(),
                otp: otp.trim(),
                newPassword,
            });
            setSuccess(true);
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
        setResending(true);

        try {
            await forgotPassword({ email: email.trim().toLowerCase() });
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

    if (success) {
        return (
            <div className="reset-container">
                <div className="reset-card">
                    <div className="reset-icon" aria-hidden="true">✅</div>
                    <h2 className="reset-title">Password Reset!</h2>
                    <p className="reset-subtitle">
                        Your password has been changed successfully. You can now
                        sign in with your new password.
                    </p>
                    <button
                        type="button"
                        className="reset-submit-btn"
                        onClick={() => navigate('/login')}
                    >
                        Go to Sign In
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="reset-container">
            <div className="reset-card">
                <div className="reset-icon" aria-hidden="true">🔑</div>

                <h2 className="reset-title">Reset Your Password</h2>
                <p className="reset-subtitle">
                    Enter the verification code sent to your email and choose a
                    new password.
                </p>

                {email && (
                    <p className="reset-email-hint">
                        Code sent to <strong>{email}</strong>
                    </p>
                )}

                {error && (
                    <div className="reset-alert reset-alert--error" role="alert">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="reset-form" noValidate>
                    {/* Show email field only if not pre-filled from query param */}
                    {!emailParam && (
                        <input
                            type="email"
                            placeholder="Your email address"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="reset-input"
                            autoComplete="email"
                            required
                            disabled={loading}
                        />
                    )}

                    {/* OTP input */}
                    <input
                        ref={otpInputRef}
                        type="text"
                        inputMode="numeric"
                        placeholder="Verification code"
                        value={otp}
                        onChange={(e) =>
                            setOtp(e.target.value.replace(/\D/g, '').slice(0, 8))
                        }
                        className="reset-input reset-input--otp"
                        autoComplete="one-time-code"
                        required
                        disabled={loading}
                        maxLength={8}
                        aria-label="Verification code"
                    />

                    <div className="reset-divider">
                        <span className="reset-divider-text">New Password</span>
                    </div>

                    {/* New password */}
                    <div className="reset-password-wrapper">
                        <input
                            type={newPasswordVisible ? 'text' : 'password'}
                            placeholder="New password (min 6 characters)"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            className="reset-input reset-input--password"
                            autoComplete="new-password"
                            minLength={6}
                            required
                            disabled={loading}
                        />
                        <button
                            type="button"
                            className="reset-password-toggle"
                            onClick={() => setNewPasswordVisible((v) => !v)}
                            aria-label={
                                newPasswordVisible
                                    ? 'Hide password'
                                    : 'Show password'
                            }
                        >
                            {newPasswordVisible ? '🙈' : '👁'}
                        </button>
                    </div>

                    {/* Confirm password */}
                    <div className="reset-password-wrapper">
                        <input
                            type={confirmPasswordVisible ? 'text' : 'password'}
                            placeholder="Confirm new password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            className="reset-input reset-input--password"
                            autoComplete="new-password"
                            required
                            disabled={loading}
                        />
                        <button
                            type="button"
                            className="reset-password-toggle"
                            onClick={() => setConfirmPasswordVisible((v) => !v)}
                            aria-label={
                                confirmPasswordVisible
                                    ? 'Hide password'
                                    : 'Show password'
                            }
                        >
                            {confirmPasswordVisible ? '🙈' : '👁'}
                        </button>
                    </div>

                    {/* Password match indicator */}
                    {confirmPassword && (
                        <p
                            className={`reset-match-hint ${
                                newPassword === confirmPassword
                                    ? 'reset-match-hint--ok'
                                    : 'reset-match-hint--err'
                            }`}
                        >
                            {newPassword === confirmPassword
                                ? '✓ Passwords match'
                                : '✗ Passwords do not match'}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="reset-submit-btn"
                        disabled={loading}
                    >
                        {loading ? 'Resetting…' : 'Reset Password'}
                    </button>
                </form>

                {/* Resend section */}
                <div className="reset-resend-section">
                    <p className="reset-resend-text">
                        Didn&apos;t receive the code?
                    </p>
                    <button
                        type="button"
                        className="reset-resend-btn"
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

                <p className="reset-expiry-note">
                    Verification code expires in 3 minutes.
                </p>

                <button
                    type="button"
                    className="reset-back-link"
                    onClick={() => navigate('/forgot-password')}
                >
                    ← Back
                </button>
            </div>
        </div>
    );
};

export default ResetPassword;
