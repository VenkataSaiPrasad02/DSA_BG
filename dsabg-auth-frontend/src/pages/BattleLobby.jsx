import { Link } from 'react-router-dom';
import '../styles/Social.css';

const BattleLobby = () => (
    <section className="social-page battle-lobby">
        <header className="social-heading">
            <div>
                <span className="social-eyebrow">DSABG ARENA</span>
                <h1>Battle lobby</h1>
                <p>Set up your next head-to-head coding challenge.</p>
            </div>
        </header>
        <div className="lobby-panel">
            <div className="lobby-emblem" aria-hidden="true">&lt;/&gt;</div>
            <span className="social-eyebrow">CUSTOM ROOMS AVAILABLE</span>
            <h2>Build your squad</h2>
            <p>
                Custom rooms are available. Create a room, invite your crew, and
                start a battle from the host controls in the room lobby.
            </p>
            <div className="lobby-actions">
                <Link className="social-primary-button" to="/custom-rooms">Open custom rooms</Link>
            </div>
            <Link className="social-text-link" to="/friends">Find players for your crew <span aria-hidden="true">→</span></Link>
        </div>
    </section>
);

export default BattleLobby;
