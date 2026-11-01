import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getFriends, removeFriend } from '../services/friendsService';
import '../styles/Social.css';

const FriendsList = () => {
    const [friends, setFriends] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');
    const [removingFriendId, setRemovingFriendId] = useState(null);

    useEffect(() => {
        let active = true;

        getFriends()
            .then((data) => {
                if (active) setFriends(data);
            })
            .catch((requestError) => {
                if (active) {
                    setError(
                        requestError.response?.data?.message ||
                        'Could not load your friends.'
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

    const handleRetry = async () => {
        setLoading(true);
        setError('');
        try {
            setFriends(await getFriends());
        } catch (requestError) {
            setError(
                requestError.response?.data?.message ||
                'Could not load your friends.'
            );
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveFriend = async (friend) => {
        setRemovingFriendId(friend.userId);
        setActionError('');
        try {
            await removeFriend(friend.userId);
            setFriends((currentFriends) =>
                currentFriends.filter((item) => item.userId !== friend.userId)
            );
        } catch (requestError) {
            setActionError(
                requestError.response?.data?.message ||
                'Could not remove this friend. Please try again.'
            );
        } finally {
            setRemovingFriendId(null);
        }
    };

    return (
        <section className="social-page">
            <header className="social-heading">
                <div>
                    <span className="social-eyebrow">YOUR NETWORK</span>
                    <h1>Friends</h1>
                    <p>Your connected players, ready for the next challenge.</p>
                </div>
                <Link className="social-primary-button" to="/friends">Find players</Link>
            </header>

            {error && (
                <div className="social-message social-message--error" role="alert">
                    {error}
                    <button className="social-inline-button" type="button" onClick={handleRetry}>Try again</button>
                </div>
            )}
            {actionError && <p className="social-message social-message--error" role="alert">{actionError}</p>}
            {loading ? (
                <p className="social-loading" role="status">Loading friends…</p>
            ) : friends.length ? (
                <div className="friends-grid">
                    {friends.map((friend) => (
                        <article className="friend-tile" key={friend.userId}>
                            <Link
                                className="friend-tile-profile"
                                to={`/profile/${encodeURIComponent(friend.userId)}`}
                            >
                                <div className="player-avatar">
                                    {friend.avatarUrl
                                        ? <img src={friend.avatarUrl} alt="" />
                                        : <span>{(friend.displayName || 'P').charAt(0).toUpperCase()}</span>}
                                </div>
                                <strong>{friend.displayName || 'Player'}</strong>
                                <span className="player-id">ID · {friend.userId}</span>
                                <span className="friend-tile-link">View profile <span aria-hidden="true">↗</span></span>
                            </Link>
                            <button
                                className="social-secondary-button friend-remove-button"
                                type="button"
                                disabled={removingFriendId !== null}
                                onClick={() => handleRemoveFriend(friend)}
                            >
                                {removingFriendId === friend.userId ? 'Removing…' : 'Remove friend'}
                            </button>
                        </article>
                    ))}
                </div>
            ) : !error && (
                <div className="social-empty">
                    <span className="social-empty-mark" aria-hidden="true">◇</span>
                    <h2>Your crew starts here</h2>
                    <p>Search for a player by User ID to send your first request.</p>
                    <Link className="social-primary-button" to="/friends">Find players</Link>
                </div>
            )}
        </section>
    );
};

export default FriendsList;
