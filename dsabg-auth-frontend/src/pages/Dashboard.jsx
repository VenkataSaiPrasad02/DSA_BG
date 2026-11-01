import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMyProfile } from '../services/profileService';
import '../styles/Dashboard.css';

const Dashboard = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        let active = true;

        getMyProfile()
            .then((data) => {
                if (active) setProfile(data);
            })
            .catch((requestError) => {
                if (active) {
                    setError(
                        requestError.response?.data?.message ||
                        'Player stats could not be loaded.'
                    );
                }
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, []);

    const displayName =
        profile?.displayName ||
        user?.userName ||
        user?.email?.split('@')[0] ||
        'Player';

    const stats = [
        { label: 'Battles played', value: profile?.battlesPlayed },
        { label: 'Wins', value: profile?.wins },
        { label: 'Problems solved', value: profile?.problemsSolved },
        { label: 'Current level', value: profile?.level },
    ];

    return (
        <div className="dashboard-page">
            <section className="dashboard-hero">
                <div className="dashboard-hero-copy">
                    <span className="dashboard-eyebrow">
                        <span className="dashboard-status-dot" />
                        COMPETE. IMPROVE. CLIMB.
                    </span>
                    <h1>Welcome to the arena, <span>{displayName}</span></h1>
                    <p>
                        Put your problem-solving skills to the test. Find your
                        people, sharpen your edge, and get ready for the next battle.
                    </p>
                    <button
                        className="dashboard-primary-btn"
                        type="button"
                        onClick={() => navigate('/battles')}
                    >
                        Explore the arena <span aria-hidden="true">→</span>
                    </button>
                </div>
                <div className="dashboard-hero-art" aria-hidden="true">
                    <span className="dashboard-orbit dashboard-orbit-one" />
                    <span className="dashboard-orbit dashboard-orbit-two" />
                    <span className="dashboard-hero-symbol">&lt;/&gt;</span>
                    <span className="dashboard-floating-tag">DSA / BATTLE</span>
                </div>
            </section>

            <section className="dashboard-section" aria-labelledby="stats-title">
                <div className="dashboard-section-heading">
                    <div>
                        <span className="dashboard-section-kicker">YOUR PERFORMANCE</span>
                        <h2 id="stats-title">Battle statistics</h2>
                    </div>
                    <Link className="dashboard-text-link" to="/profile">View profile <span aria-hidden="true">↗</span></Link>
                </div>
                {error && <p className="dashboard-inline-error" role="alert">{error}</p>}
                <div className="dashboard-stats-grid" aria-live="polite">
                    {stats.map(({ label, value }) => (
                        <article className="dashboard-stat-card" key={label}>
                            <span>{label}</span>
                            <strong>{loading ? '…' : value ?? '—'}</strong>
                        </article>
                    ))}
                </div>
            </section>

            <section className="dashboard-section">
                <div className="dashboard-section-heading">
                    <div>
                        <span className="dashboard-section-kicker">CHOOSE YOUR MODE</span>
                        <h2>Enter the battleground</h2>
                    </div>
                </div>
                <div className="dashboard-mode-grid">
                    <button className="dashboard-mode-card" type="button" disabled>
                        <span className="dashboard-mode-card-icon" aria-hidden="true">&lt;/&gt;</span>
                        <span className="dashboard-mode-card-copy">
                            <strong>Practice</strong>
                            <small>Sharpen your problem-solving skills.</small>
                        </span>
                        <span className="dashboard-mode-card-status">Coming soon</span>
                    </button>
                    <button className="dashboard-mode-card" type="button" disabled>
                        <span className="dashboard-mode-card-icon" aria-hidden="true">⚔</span>
                        <span className="dashboard-mode-card-copy">
                            <strong>1v1 With Randoms</strong>
                            <small>Find an opponent and compete head-to-head.</small>
                        </span>
                        <span className="dashboard-mode-card-status">Coming soon</span>
                    </button>
                    <Link to="/custom-rooms" className="dashboard-mode-card dashboard-mode-card--active">
                        <span className="dashboard-mode-card-icon" aria-hidden="true">▤</span>
                        <span className="dashboard-mode-card-copy">
                            <strong>Custom Rooms</strong>
                            <small>Browse active battles and create a room.</small>
                        </span>
                        <span className="dashboard-mode-card-status">Open rooms <span aria-hidden="true">↗</span></span>
                    </Link>
                    <button className="dashboard-mode-card" type="button" disabled>
                        <span className="dashboard-mode-card-icon" aria-hidden="true">♛</span>
                        <span className="dashboard-mode-card-copy">
                            <strong>Leaderboard</strong>
                            <small>See where you stand among players.</small>
                        </span>
                        <span className="dashboard-mode-card-status">Coming soon</span>
                    </button>
                </div>
            </section>

            <section className="dashboard-panel dashboard-activity-panel">
                <div>
                    <span className="dashboard-section-kicker">RECENT ACTIVITY</span>
                    <h2>Battle log</h2>
                </div>
                <p className="dashboard-muted-text">
                    Battle history will appear here when battle services are available.
                </p>
            </section>
        </div>
    );
};

export default Dashboard;
