import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import '../../styles/Aside.css';

/**
 * Sidebar navigation — secondary navigation for the dashboard area.
 * Only lists pages/sections that actually exist in the current project.
 */
const SIDEBAR_ITEMS = [
    { to: '/dashboard', label: 'Overview', icon: '🏠' },
    { to: '/profile', label: 'My Profile', icon: '👤' },
];

const Aside = () => {
    const { user } = useAuth();

    // Show the gamer tag here, never the email (the header already shows it)
    const gamerTag =
        user?.userName && !user.userName.includes('@') ? user.userName : 'Player';

    return (
        <aside className="aside" aria-label="Sidebar navigation">
            {/* User identity block */}
            <div className="aside-user">
                <div className="aside-user-avatar" aria-hidden="true">
                    {gamerTag.charAt(0).toUpperCase()}
                </div>
                <p className="aside-user-email" title={gamerTag}>
                    {gamerTag}
                </p>
            </div>

            <nav aria-label="Sidebar navigation">
                <ul className="aside-list">
                    {SIDEBAR_ITEMS.map(({ to, label, icon }) => (
                        <li key={to} className="aside-item">
                            <NavLink
                                to={to}
                                end
                                className={({ isActive }) =>
                                    `aside-link${isActive ? ' aside-link--active' : ''}`
                                }
                            >
                                <span className="aside-icon" aria-hidden="true">
                                    {icon}
                                </span>
                                <span className="aside-label">{label}</span>
                            </NavLink>
                        </li>
                    ))}
                </ul>
            </nav>
        </aside>
    );
};

export default Aside;