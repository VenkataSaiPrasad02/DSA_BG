import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMyProfile, getProfileById, updateMyProfile } from '../services/profileService';
import '../styles/Profile.css';

const looksLikeEmail = (value) =>
    typeof value === 'string' && value.includes('@');

/**
 * Picks the gamer tag to show as the main identity.
 * 1. profile.displayName (only if it is NOT an email)
 * 2. user.userName from AuthContext (JWT "username" claim, only if NOT an email)
 * 3. "Player" as a last resort
 * The email address is never used as the username.
 */
const resolveGamerTag = (profile, user) => {
    const candidates = [profile?.displayName, user?.userName];
    const match = candidates.find(
        (value) =>
            typeof value === 'string' &&
            value.trim() !== '' &&
            !looksLikeEmail(value)
    );
    return match ? match.trim() : 'Player';
};

// Shows "—" when the backend did not send a value (0 is a real value and is kept).
const formatStat = (value) =>
    value === null || value === undefined ? '—' : value;

function Profile() {
    const { user } = useAuth();
    const { userId: viewedUserId } = useParams();
    const isOwnProfile = !viewedUserId || viewedUserId === user?.userId;

    const [profile, setProfile] = useState(null);
    const [displayName, setDisplayName] = useState('');
    const [avatarUrl, setAvatarUrl] = useState('');
    const [bio, setBio] = useState('');
    const [failedAvatar, setFailedAvatar] = useState('');
    const [editing, setEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');

    // Changing reloadKey re-runs the effect below (used by "Try Again")
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        let ignore = false; // stops updates if the page closes mid-request

        (isOwnProfile ? getMyProfile() : getProfileById(viewedUserId))
            .then((data) => {
                if (ignore) return;
                setProfile(data);
                setDisplayName(data?.displayName ?? '');
                setAvatarUrl(data?.avatarUrl ?? '');
                setBio(data?.bio ?? '');
            })
            .catch((err) => {
                if (ignore) return;
                setError(
                    err.response?.data?.message ||
                        'Could not load your profile. Please try again.'
                );
            })
            .finally(() => {
                if (!ignore) setLoading(false);
            });

        return () => {
            ignore = true;
        };
    }, [isOwnProfile, reloadKey, viewedUserId]);

    const handleRetry = () => {
        setLoading(true);
        setError('');
        setReloadKey((key) => key + 1);
    };

    const handleEdit = () => {
        setError('');
        setMessage('');
        setEditing(true);
    };

    const handleCancel = () => {
        // Throw away unsaved changes and go back to the saved values
        setDisplayName(profile?.displayName ?? '');
        setAvatarUrl(profile?.avatarUrl ?? '');
        setBio(profile?.bio ?? '');
        setError('');
        setEditing(false);
    };

    const handleSave = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError('');
        setMessage('');

        try {
            const updatedProfile = await updateMyProfile({
                displayName: displayName.trim() || null,
                avatarUrl: avatarUrl.trim() || null,
                bio: bio.trim() || null,
            });

            setProfile(updatedProfile);
            setDisplayName(updatedProfile?.displayName ?? '');
            setAvatarUrl(updatedProfile?.avatarUrl ?? '');
            setBio(updatedProfile?.bio ?? '');
            setEditing(false);
            setMessage('Profile updated successfully!');
        } catch (err) {
            setError(
                err.response?.data?.message ||
                    'Could not update your profile. Please try again.'
            );
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="profile-page">
                <p className="profile-status">Loading profile...</p>
            </div>
        );
    }

    if (!profile) {
        return (
            <div className="profile-page">
                <p className="profile-error" role="alert">
                    {error || 'Profile not available.'}
                </p>
                <button type="button" onClick={handleRetry}>
                    Try Again
                </button>
            </div>
        );
    }

    const gamerTag = resolveGamerTag(profile, isOwnProfile ? user : null);
    const playerId = profile.userId || user?.userId;
    const showAvatarImage = Boolean(profile.avatarUrl) && failedAvatar !== profile.avatarUrl;

    const stats = [
        ['Level', profile.level],
        ['Battles Played', profile.battlesPlayed],
        ['Wins', profile.wins],
        ['Losses', profile.losses],
        ['Problems Solved', profile.problemsSolved],
    ];

    return (
        <div className="profile-page">
            <header className="profile-header">
                <div className="profile-avatar">
                    {showAvatarImage ? (
                        <img
                            src={profile.avatarUrl}
                            alt={`${gamerTag}'s avatar`}
                            onError={() => setFailedAvatar(profile.avatarUrl)}
                        />
                    ) : (
                        <span aria-hidden="true">
                            {gamerTag.charAt(0).toUpperCase()}
                        </span>
                    )}
                </div>

                <div className="profile-heading">
                    <p className="profile-eyebrow">PLAYER PROFILE</p>
                    <h1>{gamerTag}</h1>
                    <p className="profile-user-id">
                        Player ID: {playerId || 'Not available'}
                    </p>
                </div>

                {!editing && isOwnProfile && (
                    <button
                        type="button"
                        className="profile-edit-button"
                        onClick={handleEdit}
                    >
                        Edit Profile
                    </button>
                )}
            </header>

            {error && (
                <p className="profile-error" role="alert">
                    {error}
                </p>
            )}
            {message && (
                <p className="profile-success" role="status">
                    {message}
                </p>
            )}

            {editing && (
                <form className="profile-edit-form" onSubmit={handleSave}>
                    <h2>Edit Profile</h2>

                    <label htmlFor="displayName">Display name</label>
                    <input
                        id="displayName"
                        type="text"
                        maxLength={100}
                        value={displayName}
                        onChange={(event) => setDisplayName(event.target.value)}
                    />

                    <label htmlFor="avatarUrl">Avatar URL</label>
                    <input
                        id="avatarUrl"
                        type="url"
                        maxLength={1000}
                        placeholder="https://example.com/my-avatar.png"
                        value={avatarUrl}
                        onChange={(event) => setAvatarUrl(event.target.value)}
                    />

                    <label htmlFor="bio">Bio</label>
                    <textarea
                        id="bio"
                        rows={4}
                        maxLength={500}
                        placeholder="Tell other players about yourself"
                        value={bio}
                        onChange={(event) => setBio(event.target.value)}
                    />
                    <p className="profile-character-count">{bio.length}/500</p>

                    <div className="profile-form-actions">
                        <button type="submit" disabled={saving}>
                            {saving ? 'Saving...' : 'Save Changes'}
                        </button>
                        <button
                            type="button"
                            className="profile-cancel-button"
                            onClick={handleCancel}
                            disabled={saving}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            )}

            <section className="profile-bio">
                <h2>Bio</h2>
                {profile.bio ? (
                    <p>{profile.bio}</p>
                ) : (
                    <p className="profile-bio-empty">No bio added yet.</p>
                )}
            </section>

            <section className="profile-stats">
                <h2>Player Statistics</h2>
                <div className="profile-stats-grid">
                    {stats.map(([label, value]) => (
                        <div className="profile-stat-card" key={label}>
                            <p>{label}</p>
                            <strong>{formatStat(value)}</strong>
                        </div>
                    ))}
                </div>
            </section>

        </div>
    );
}

export default Profile;