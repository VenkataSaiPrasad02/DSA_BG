import { useState } from 'react';
import { Link } from 'react-router-dom';
import { sendFriendRequest, searchPlayerById } from '../services/friendsService';
import '../styles/Social.css';

const Friends = () => {
    const [userId, setUserId] = useState('');
    const [player, setPlayer] = useState(null);
    const [loading, setLoading] = useState(false);
    const [sending, setSending] = useState(false);
    const [sent, setSent] = useState(false);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');

    const handleSearch = async (event) => {
        event.preventDefault();
        const exactId = userId.trim();
        if (!exactId) return;

        setLoading(true);
        setError('');
        setMessage('');
        setSent(false);
        setPlayer(null);
        try {
            const result = await searchPlayerById(exactId);
            setPlayer(result);
        } catch (requestError) {
            setError(
                requestError.response?.data?.message ||
                'Player not found. Check the User ID and try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    const handleAddFriend = async () => {
        if (!player?.profile?.userId) return;
        setSending(true);
        setError('');
        setMessage('');
        try {
            const result = await sendFriendRequest(player.profile.userId);
            setMessage(result?.message || 'Friend request sent.');
            setSent(true);
        } catch (requestError) {
            setError(
                requestError.response?.data?.message ||
                'Could not send the friend request. Please try again.'
            );
        } finally {
            setSending(false);
        }
    };

    const profile = player?.profile;
    const connected = player?.relationship === 'CONNECTED';

    return (
        <section className="social-page">
            <header className="social-heading">
                <div>
                    <span className="social-eyebrow">PLAYER NETWORK</span>
                    <h1>Find your next rival</h1>
                    <p>Search by an exact User ID to find and connect with a player.</p>
                </div>
            </header>

            <form className="social-search" onSubmit={handleSearch}>
                <label htmlFor="player-id">Player User ID</label>
                <div className="social-search-row">
                    <input
                        id="player-id"
                        value={userId}
                        onChange={(event) => setUserId(event.target.value)}
                        placeholder="Enter exact User ID"
                        autoComplete="off"
                        required
                    />
                    <button className="social-primary-button" type="submit" disabled={loading}>
                        {loading ? 'Searching…' : 'Search players'}
                    </button>
                </div>
            </form>

            {error && <p className="social-message social-message--error" role="alert">{error}</p>}
            {message && <p className="social-message social-message--success" role="status">{message}</p>}

            {profile && (
                <article className="player-card">
                    <div className="player-avatar">
                        {profile.avatarUrl
                            ? <img src={profile.avatarUrl} alt="" />
                            : <span>{(profile.displayName || 'P').charAt(0).toUpperCase()}</span>}
                    </div>
                    <div className="player-card-info">
                        <span className="social-eyebrow">PLAYER FOUND</span>
                        <h2>{profile.displayName || 'Player'}</h2>
                        <p className="player-id">ID · {profile.userId}</p>
                        <span className="relationship-pill">
                            {connected ? 'Connected' : 'Not connected'}
                        </span>
                    </div>
                    <div className="player-card-actions">
                        <Link className="social-secondary-button" to={`/profile/${encodeURIComponent(profile.userId)}`}>
                            View profile
                        </Link>
                        <button
                            className="social-primary-button"
                            type="button"
                            onClick={handleAddFriend}
                            disabled={connected || sending || sent}
                        >
                            {connected ? 'Already connected' : sent ? 'Request sent' : sending ? 'Sending…' : 'Add friend'}
                        </button>
                    </div>
                </article>
            )}

            {!profile && !loading && !error && (
                <div className="social-empty">
                    <span className="social-empty-mark" aria-hidden="true">+</span>
                    <h2>Build your crew</h2>
                    <p>Find players by ID and connect before your next challenge.</p>
                </div>
            )}
        </section>
    );
};

export default Friends;
