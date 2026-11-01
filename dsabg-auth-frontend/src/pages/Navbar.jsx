import { NavLink } from 'react-router-dom';
import '../../styles/Navbar.css';

/**
 * Top navigation bar — main section links for authenticated users.
 * Uses React Router NavLink so the active route gets the active class.
 * Only includes routes that actually exist in the current project.
 */
const NAV_ITEMS = [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/profile', label: 'Profile' },
];

const Navbar = () => {
    return (
        <nav className="navbar" aria-label="Main navigation">
            <ul className="navbar-list">
                {NAV_ITEMS.map(({ to, label }) => (
                    <li key={to} className="navbar-item">
                        <NavLink
                            to={to}
                            end
                            className={({ isActive }) =>
                                `navbar-link${isActive ? ' navbar-link--active' : ''}`
                            }
                        >
                            {label}
                        </NavLink>
                    </li>
                ))}
            </ul>
        </nav>
    );
};

export default Navbar;