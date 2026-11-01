import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
    acceptFriendRequest,
    getPendingFriendRequests,
    rejectFriendRequest,
} from '../services/friendsService';
import '../styles/Social.css';

const FriendRequests = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [busyId, setBusyId] = useState(null);
    const [error, setError] = useState('');

    useEffect(() => {
        let active = true;

        getPendingFriendRequests()
            .then((data) => {
                if (active) setRequests(data);
            })
            .catch((requestError) => {
                if (active) {
                    setError(
                        requestError.response?.data?.message ||
                        'Could not load friend requests.'
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
            setRequests(await getPendingFriendRequests());
        } catch (requestError) {
            setError(
                requestError.response?.data?.message ||
                'Could not load friend requests.'
            );
        } finally {
            setLoading(false);
        }
    };

    const respond = async (requestId, action) => {
        setBusyId(requestId);
        setError('');
        try {
            if (action === 'accept') await acceptFriendRequest(requestId);
            else await rejectFriendRequest(requestId);
            setRequests((current) => current.filter((request) => request.id !== requestId));
        } catch (requestError) {
            setError(
                requestError.response?.data?.message ||
                'Could not update this request. Please try again.'
            );
        } finally {
            setBusyId(null);
        }
    };

    return (
        <section className="social-page">
            <header className="social-heading">
                <div>
                    <span className="social-eyebrow">YOUR NETWORK</span>
                    <h1>Friend requests</h1>
                    <p>Choose who joins your crew.</p>
                </div>
            </header>

            {error && (
                <p className="social-message social-message--error" role="alert">
                    {error}
                    <button className="social-inline-button" type="button" onClick={handleRetry}>Try again</button>
                </p>
            )}
            {loading ? (
                <p className="social-loading" role="status">Loading requests…</p>
            ) : requests.length ? (
                <div className="social-list">
                    {requests.map((request) => {
                        const player = request.sender || {};
                        const busy = busyId === request.id;
                        return (
                            <article className="social-list-card" key={request.id}>
                                <div className="player-avatar player-avatar--small">
                                    {player.avatarUrl
                                        ? <img src={player.avatarUrl} alt="" />
                                        : <span>{(player.displayName || 'P').charAt(0).toUpperCase()}</span>}
                                </div>
                                <div className="social-list-info">
                                    <Link to={`/profile/${encodeURIComponent(player.userId || request.senderId)}`}>
                                        {player.displayName || 'Player'}
                                    </Link>
                                    <span className="player-id">ID · {player.userId || request.senderId}</span>
                                </div>
                                <div className="social-list-actions">
                                    <button
                                        className="social-primary-button"
                                        type="button"
                                        disabled={busy}
                                        onClick={() => respond(request.id, 'accept')}
                                    >
                                        {busy ? 'Updating…' : 'Accept'}
                                    </button>
                                    <button
                                        className="social-secondary-button"
                                        type="button"
                                        disabled={busy}
                                        onClick={() => respond(request.id, 'reject')}
                                    >
                                        Reject
                                    </button>
                                </div>
                            </article>
                        );
                    })}
                </div>
            ) : (
                <div className="social-empty">
                    <span className="social-empty-mark" aria-hidden="true">◇</span>
                    <h2>You’re all caught up</h2>
                    <p>New friend requests will show up here.</p>
                </div>
            )}
        </section>
    );
};

export default FriendRequests;
