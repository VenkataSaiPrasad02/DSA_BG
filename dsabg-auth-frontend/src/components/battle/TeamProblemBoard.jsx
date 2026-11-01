const TeamProblemList = ({ team, problems, solvedIds, selectedProblemId, onSelect }) => {
    const solvedCount = solvedIds.length;

    return (
        <section
            className={`battle-team-card battle-team-card--${team.toLowerCase()}`}
            aria-labelledby={`battle-team-${team}-heading`}
        >
            <header className="battle-team-heading">
                <div>
                    <span className="battle-team-kicker">ALLOCATED PROBLEMS</span>
                    <h2 id={`battle-team-${team}-heading`}>Team {team}</h2>
                </div>
                <span className="battle-team-progress">{solvedCount}/{problems.length} solved</span>
            </header>
            <div className="battle-team-problems">
                {problems.map((problem) => {
                    const isSolved = solvedIds.includes(problem.id);
                    const isSelected = selectedProblemId === problem.id;

                    return (
                        <button
                            className={[
                                'battle-team-problem',
                                isSolved && 'battle-team-problem--solved',
                                isSelected && 'battle-team-problem--selected'
                            ].filter(Boolean).join(' ')}
                            type="button"
                            key={problem.id}
                            aria-pressed={isSelected}
                            onClick={() => onSelect(problem.id)}
                        >
                            <span className="battle-team-problem-number">{problem.number}</span>
                            <span className="battle-team-problem-title">{problem.title}</span>
                            <span className={`battle-team-problem-status${isSolved ? ' is-solved' : ''}`}>
                                {isSolved ? (
                                    <>
                                        <span aria-hidden="true">✓</span>
                                        <span>Completed</span>
                                    </>
                                ) : 'Pending'}
                            </span>
                        </button>
                    );
                })}
            </div>
        </section>
    );
};

const TeamProblemBoard = ({ problems, progress, selectedProblemId, onSelect }) => (
    <section className="battle-team-board" aria-label="Team problem allocations">
        <TeamProblemList
            team="A"
            problems={problems}
            solvedIds={progress.A}
            selectedProblemId={selectedProblemId}
            onSelect={onSelect}
        />
        <TeamProblemList
            team="B"
            problems={problems}
            solvedIds={progress.B}
            selectedProblemId={selectedProblemId}
            onSelect={onSelect}
        />
    </section>
);

export default TeamProblemBoard;
