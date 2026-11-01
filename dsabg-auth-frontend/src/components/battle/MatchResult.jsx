const getInitials = (name) =>
    name.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();

const MatchResult = ({ room, members, scores, mvpSolved, returnLabel, onReturn }) => {
    const winner = scores.A >= scores.B ? 'A' : 'B';
    const loser = winner === 'A' ? 'B' : 'A';
    const teamName = (team) => `Team ${team}`;
    const mvpName = (team) =>
        members.find((member) => member.team === team)?.displayName || `Team ${team} MVP`;

    return (
        <main className="battle-result-page">
            <header className="battle-result-topbar">
                <div>
                    <span className="battle-workspace-eyebrow">DSABG / MATCH COMPLETE</span>
                    <h1>{room.roomName}</h1>
                </div>
                <span className="battle-result-room">ROOM {room.roomCode}</span>
            </header>

            <section className={`battle-result-hero battle-result-hero--${winner.toLowerCase()}`}>
                <span className="battle-result-confetti" aria-hidden="true">✦</span>
                <span className="battle-result-eyebrow">FINAL WHISTLE</span>
                <h2>VICTORY</h2>
                <p><strong>{teamName(winner)}</strong> takes the match</p>
                <div className="battle-result-score">
                    <span>{scores[winner]}</span>
                    <span className="battle-result-score-divider">—</span>
                    <span>{scores[loser]}</span>
                </div>
                <div className="battle-result-score-labels">
                    <span>{teamName(winner)} · Problems solved</span>
                    <span>{teamName(loser)} · Problems solved</span>
                </div>
            </section>

            <section className="battle-result-teams" aria-label="Team results">
                {[winner, loser].map((team) => {
                    const isWinner = team === winner;
                    const name = mvpName(team);

                    return (
                        <article
                            className={`battle-mvp-card battle-mvp-card--${team.toLowerCase()}${isWinner ? ' battle-mvp-card--winner' : ''}`}
                            key={team}
                        >
                            <div className="battle-mvp-team-row">
                                <span>{teamName(team)}</span>
                                {isWinner
                                    ? <span className="battle-team-victory-badge">VICTORY</span>
                                    : <span className="battle-team-defeat-badge">DEFEAT</span>}
                            </div>
                            <div className="battle-mvp-content">
                                <span className="battle-mvp-avatar" aria-hidden="true">{getInitials(name)}</span>
                                <div className="battle-mvp-player">
                                    <span className="battle-mvp-label">♛ TEAM MVP</span>
                                    <h3>{name}</h3>
                                    <p>{mvpSolved[team]} problems solved</p>
                                </div>
                                <span className="battle-mvp-crown" aria-hidden="true">♛</span>
                            </div>
                        </article>
                    );
                })}
            </section>

            <section className="battle-result-summary" aria-labelledby="battle-result-summary-heading">
                <h2 id="battle-result-summary-heading">Match summary</h2>
                <dl>
                    <div><dt>Team A final score</dt><dd>{scores.A}</dd></div>
                    <div><dt>Team B final score</dt><dd>{scores.B}</dd></div>
                    <div><dt>Winning team</dt><dd>{teamName(winner)}</dd></div>
                    <div><dt>Team A MVP</dt><dd>{mvpName('A')}</dd></div>
                    <div><dt>Team B MVP</dt><dd>{mvpName('B')}</dd></div>
                </dl>
            </section>
            <p className="battle-result-preview-note">Sample match result preview · scores shown for UI demonstration</p>
            <button className="battle-result-return" type="button" onClick={onReturn}>
                {returnLabel}
            </button>
        </main>
    );
};

export default MatchResult;
