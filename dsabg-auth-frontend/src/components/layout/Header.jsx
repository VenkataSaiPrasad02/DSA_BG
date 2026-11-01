import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import '../../styles/Header.css';

/**
 * Application header — shown inside the Dashboard layout.
 * Displays the app name and the logged-in user's email with a logout button.
 */
const Header = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login', { replace: true });
    };

    return (
        <header className="header">
            <div className="header-brand">
                <span className="header-brand-icon" aria-hidden="true">⚔</span>
                <span className="header-brand-name">DSABG<span> / ARENA</span></span>
            </div>

            <div className="header-user">
                {(user?.userName || user?.email) && (
                    <span className="header-user-email" title={user.userName || user.email}>
                        {user.userName || user.email}
                    </span>
                )}
                <button
                    type="button"
                    className="header-logout-btn"
                    onClick={handleLogout}
                    aria-label="Log out"
                >
                    Logout
                </button>
            </div>
        </header>
    );
};

export default Header;
