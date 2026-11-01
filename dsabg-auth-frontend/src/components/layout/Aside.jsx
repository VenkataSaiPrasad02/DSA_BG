import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import '../../styles/Aside.css';

/**
 * Sidebar navigation — secondary navigation for the dashboard area.
 * Only lists pages/sections that actually exist in the current project.
 */
const SIDEBAR_ITEMS = [
    { to: '/dashboard', label: 'Overview', icon: '⌂' },
    { to: '/battles', label: 'Battle lobby', icon: '</>' },
    { to: '/custom-rooms', label: 'Custom rooms', icon: '▤' },
    { to: '/problems', label: 'All Problems', icon: '≡' },
    { to: '/friends', label: 'Find players', icon: '+' },
    { to: '/friends/requests', label: 'Requests', icon: '◇' },
    { to: '/friends/list', label: 'Friends', icon: '◎' },
    { to: '/profile', label: 'My profile', icon: '◉' },
];

const Aside = () => {
    const { user } = useAuth();

    return (
        <aside className="aside" aria-label="Sidebar navigation">
            {/* User identity block */}
            <div className="aside-user">
                <div className="aside-user-avatar" aria-hidden="true">
                    {(user?.userName || user?.email || 'P').charAt(0).toUpperCase()}
                </div>
                {(user?.userName || user?.email) && (
                    <p className="aside-user-email" title={user.userName || user.email}>
                        {user.userName || user.email}
                    </p>
                )}
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
