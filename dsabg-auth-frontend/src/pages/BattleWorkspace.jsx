import { useEffect, useMemo, useState } from 'react';
import Editor, { loader } from '@monaco-editor/react';
import * as monaco from 'monaco-editor';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRoomDetails, getRoomErrorMessage, leaveRoom } from '../services/roomService';
import MatchResult from '../components/battle/MatchResult';
import TeamProblemBoard from '../components/battle/TeamProblemBoard';
import {
    BATTLE_PREVIEW_PROBLEMS,
    BATTLE_PREVIEW_PROGRESS,
    MATCH_RESULT_PREVIEW
} from '../components/battle/battlePreviewData';
import '../styles/BattleWorkspace.css';
import '../styles/BattleArena.css';

loader.config({ monaco });

const LANGUAGES = [
    { id: 'javascript', label: 'JavaScript' },
    { id: 'python', label: 'Python' },
    { id: 'java', label: 'Java' },
    { id: 'cpp', label: 'C++' },
];

const BattleWorkspace = () => {
    const { roomCode } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [room, setRoom] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [language, setLanguage] = useState('javascript');
    const [code, setCode] = useState('');
    const [editorReady, setEditorReady] = useState(false);
    const [exitConfirmationOpen, setExitConfirmationOpen] = useState(false);
    const [leavingRoom, setLeavingRoom] = useState(false);
    const [exitError, setExitError] = useState('');
    const [selectedProblemId, setSelectedProblemId] = useState(BATTLE_PREVIEW_PROBLEMS[0].id);
    const [showResultPreview, setShowResultPreview] = useState(false);

    useEffect(() => {
        const controller = new AbortController();
        let active = true;

        const loadBattleRoom = async () => {
            if (!/^\d{1,10}$/.test(roomCode || '')) {
                setError('That room ID is not valid.');
                setLoading(false);
                return;
            }

            try {
                const response = await getRoomDetails(roomCode, {
                    signal: controller.signal,
                });
                if (active) {
                    setRoom(response.data);
                    setError('');
                }
            } catch (requestError) {
                if (active && requestError.code !== 'ERR_CANCELED') {
                    setError(getRoomErrorMessage(requestError));
                }
            } finally {
                if (active) setLoading(false);
            }
        };

        loadBattleRoom();
        return () => {
            active = false;
            controller.abort();
        };
    }, [roomCode]);

    const currentMember = useMemo(
        () => room?.roomMembers?.find(
            (member) => String(member.userId) === String(user?.userId)
        ),
        [room, user?.userId]
    );
    const opposingTeam = currentMember
        ? currentMember.team === 'A' ? 'B' : 'A'
        : null;
    const opponents = opposingTeam
        ? room?.roomMembers?.filter((member) => member.team === opposingTeam) || []
        : [];
    const selectedProblem = BATTLE_PREVIEW_PROBLEMS.find(
        (problem) => problem.id === selectedProblemId
    ) || BATTLE_PREVIEW_PROBLEMS[0];
    const battleFinished = ['COMPLETED', 'FINISHED', 'ENDED', 'CLOSED'].includes(
        room?.roomStatus || room?.status
    );

    const handleExitRoom = async () => {
        if (!room || leavingRoom) return;

        setLeavingRoom(true);
        setExitError('');
        try {
            await leaveRoom(room.roomCode);
            navigate('/custom-rooms', {
                replace: true,
                state: { roomNotice: 'You left the battle room.' },
            });
        } catch (requestError) {
            setExitError(getRoomErrorMessage(requestError));
            setExitConfirmationOpen(false);
        } finally {
            setLeavingRoom(false);
        }
    };

    if (loading) {
        return (
            <main className="battle-workspace battle-workspace-state" role="status">
                <span className="rooms-spinner" aria-hidden="true" />
                <p>Loading battle workspace…</p>
            </main>
        );
    }

    if (room && room.roomStatus !== 'IN_BATTLE' && !battleFinished) {
        return <Navigate to={`/custom-rooms/${roomCode}`} replace />;
    }

    if (error || !room) {
        return (
            <main className="battle-workspace battle-workspace-state">
                <p className="rooms-error" role="alert">
                    {error || 'Battle room details are unavailable.'}
                </p>
                <Link className="battle-workspace-link" to={`/custom-rooms/${roomCode}`}>
                    Return to room
                </Link>
            </main>
        );
    }

    if (battleFinished || showResultPreview) {
        return (
            <MatchResult
                room={room}
                members={room.roomMembers || []}
                scores={MATCH_RESULT_PREVIEW.scores}
                mvpSolved={MATCH_RESULT_PREVIEW.mvpSolved}
                returnLabel={battleFinished ? 'Return to custom rooms' : 'Return to battle preview'}
                onReturn={() => {
                    if (battleFinished) navigate('/custom-rooms', { replace: true });
                    else setShowResultPreview(false);
                }}
            />
        );
    }

    return (
        <main className="battle-workspace">
            <header className="battle-workspace-header">
                <div className="battle-workspace-brand">
                    <Link
                        className="battle-workspace-back"
                        to={`/custom-rooms/${roomCode}`}
                    >
                        ← Room
                    </Link>
                    <div>
                        <span className="battle-workspace-eyebrow">DSABG / LIVE BATTLE</span>
                        <h1>{room.roomName}</h1>
                    </div>
                </div>
                <div className="battle-workspace-session">
                    <span className="battle-live-indicator"><span /> IN BATTLE</span>
                    <span>ROOM {room.roomCode}</span>
                    <button
                        className="battle-result-preview-button"
                        type="button"
                        onClick={() => setShowResultPreview(true)}
                    >
                        Preview result
                    </button>
                    <button
                        className="battle-exit-button"
                        type="button"
                        onClick={() => {
                            setExitError('');
                            setExitConfirmationOpen(true);
                        }}
                    >
                        Exit Room
                    </button>
                </div>
            </header>

            {exitError && <p className="battle-exit-error" role="alert">{exitError}</p>}

            {exitConfirmationOpen && (
                <div className="battle-exit-dialog-backdrop">
                    <section
                        className="battle-exit-dialog"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="battle-exit-title"
                        aria-describedby="battle-exit-description"
                    >
                        <h2 id="battle-exit-title">Exit Room?</h2>
                        <p id="battle-exit-description">
                            Leave this battle room? The room service will process your exit.
                        </p>
                        <div className="battle-exit-dialog-actions">
                            <button
                                type="button"
                                disabled={leavingRoom}
                                onClick={() => setExitConfirmationOpen(false)}
                            >
                                Cancel
                            </button>
                            <button
                                className="battle-exit-confirm"
                                type="button"
                                disabled={leavingRoom}
                                onClick={handleExitRoom}
                            >
                                {leavingRoom ? 'Leaving…' : 'Exit Room'}
                            </button>
                        </div>
                    </section>
                </div>
            )}

            <section className="battle-session-strip" aria-label="Battle status">
                <div className="battle-session-stat">
                    <span>STATUS</span>
                    <strong>Battle active</strong>
                </div>
                <div className="battle-session-stat">
                    <span>TIME</span>
                    <strong>Timer unavailable</strong>
                </div>
                <div className="battle-session-stat battle-opponents">
                    <span>
                        {opposingTeam
                            ? `OPPONENT · TEAM ${opposingTeam}`
                            : 'OPPONENT'}
                    </span>
                    <strong>
                        {!currentMember
                            ? 'Your battle seat is not in the room roster'
                            : opponents.length
                                ? opponents.map((member) => member.displayName).join(', ')
                                : 'No opponent assigned'}
                    </strong>
                </div>
            </section>

            <p className="battle-preview-note">
                Sample arena preview · problem allocation and completion states are local UI data
            </p>
            <TeamProblemBoard
                problems={BATTLE_PREVIEW_PROBLEMS}
                progress={BATTLE_PREVIEW_PROGRESS}
                selectedProblemId={selectedProblemId}
                onSelect={setSelectedProblemId}
            />

            <div className="battle-workspace-grid">
                <section className="battle-problem-panel" aria-labelledby="problem-title">
                    <div className="battle-panel-heading">
                        <div>
                            <span className="battle-workspace-eyebrow">PROBLEM {selectedProblem.number}</span>
                            <h2 id="problem-title">{selectedProblem.title}</h2>
                        </div>
                        <span className="battle-data-badge">SELECTED</span>
                    </div>
                    <div className="battle-problem-sections">
                        <article className="battle-problem-section battle-problem-description">
                            <h3>Problem</h3>
                            <p>{selectedProblem.description}</p>
                        </article>
                        <article className="battle-problem-section">
                            <h3>Constraints</h3>
                            <ul>{selectedProblem.constraints.map((item) => <li key={item}>{item}</li>)}</ul>
                        </article>
                        <article className="battle-problem-section">
                            <h3>Input format</h3>
                            <p>{selectedProblem.inputFormat}</p>
                        </article>
                        <article className="battle-problem-section">
                            <h3>Output format</h3>
                            <p>{selectedProblem.outputFormat}</p>
                        </article>
                        <article className="battle-problem-section battle-problem-examples">
                            <h3>Examples</h3>
                            {selectedProblem.examples.map((example, index) => (
                                <div className="battle-problem-example" key={`${selectedProblem.id}-example-${index}`}>
                                    <p><strong>Input:</strong> <code>{example.input}</code></p>
                                    <p><strong>Output:</strong> <code>{example.output}</code></p>
                                    <p><strong>Explanation:</strong> {example.explanation}</p>
                                </div>
                            ))}
                        </article>
                    </div>
                </section>

                <section className="battle-coding-panel" aria-label="Code editor and output">
                    <div className="battle-editor-toolbar">
                        <label htmlFor="battle-language">Language</label>
                        <select
                            id="battle-language"
                            value={language}
                            onChange={(event) => setLanguage(event.target.value)}
                        >
                            {LANGUAGES.map((item) => (
                                <option key={item.id} value={item.id}>{item.label}</option>
                            ))}
                        </select>
                        <span className="battle-editor-problem-context" title={selectedProblem.title}>
                            Editing: {selectedProblem.title}
                        </span>
                        <span className="battle-draft-label">Unsaved local draft</span>
                    </div>
                    <div className="battle-editor-container">
                        <Editor
                            height="100%"
                            language={language}
                            theme="vs-dark"
                            value={code}
                            onChange={(value) => setCode(value ?? '')}
                            onMount={() => setEditorReady(true)}
                            loading={<div className="battle-editor-loading">Loading editor…</div>}
                            options={{
                                ariaLabel: 'Battle code editor',
                                automaticLayout: true,
                                fontSize: 14,
                                minimap: { enabled: false },
                                scrollBeyondLastLine: false,
                                wordWrap: 'on',
                            }}
                        />
                    </div>
                    <div className="battle-editor-actions">
                        {!editorReady && <span>Preparing editor…</span>}
                        <button type="button" disabled title="Code execution API is not available">
                            Run Code
                        </button>
                        <button
                            className="battle-submit-button"
                            type="button"
                            disabled
                            title="Solution submission API is not available"
                        >
                            Submit Solution
                        </button>
                    </div>
                    <section className="battle-output-panel" aria-live="polite">
                        <div className="battle-output-heading">
                            <h2>Test cases &amp; output</h2>
                            <span>NOT RUN</span>
                        </div>
                        <p>
                            Results will appear here after a run. The current backend
                            does not provide code execution or submission endpoints.
                        </p>
                    </section>
                </section>
            </div>
        </main>
    );
};

export default BattleWorkspace;
