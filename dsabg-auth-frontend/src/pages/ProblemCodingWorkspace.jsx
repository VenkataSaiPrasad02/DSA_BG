import { useEffect, useState } from 'react';
import Editor, { loader } from '@monaco-editor/react';
import * as monaco from 'monaco-editor';
import { Link, useLocation, useParams } from 'react-router-dom';
import ProblemMarkdown from '../components/problems/ProblemMarkdown';
import { getProblemBySlug, getProblemErrorMessage } from '../services/problemService';
import '../styles/ProblemCodingWorkspace.css';

loader.config({ monaco });

const LANGUAGES = [{ id: 'java', label: 'Java' }];
const MIN_TEST_PANEL_HEIGHT = 120;
const MAX_TEST_PANEL_HEIGHT = 420;

const getDraft = (slug, language) =>
    localStorage.getItem(`dsabg:problem-draft:${slug}:${language}`) || '';

const ProblemCodingWorkspace = () => {
    const { slug } = useParams();
    const location = useLocation();
    const [retry, setRetry] = useState(0);
    const requestKey = JSON.stringify({ slug, retry });
    const [response, setResponse] = useState({ key: '', problem: null, error: '' });
    const [language, setLanguage] = useState('java');
    const [codeState, setCodeState] = useState(() => ({
        slug,
        language: 'java',
        value: getDraft(slug, 'java')
    }));
    const code = codeState.slug === slug && codeState.language === language
        ? codeState.value
        : getDraft(slug, language);
    const [activeTestTab, setActiveTestTab] = useState('results');
    const [customInput, setCustomInput] = useState('');
    const [testPanelHeight, setTestPanelHeight] = useState(220);
    const [testPanelCollapsed, setTestPanelCollapsed] = useState(false);

    useEffect(() => {
        const controller = new AbortController();
        getProblemBySlug(JSON.parse(requestKey).slug, { signal: controller.signal })
            .then((problem) => {
                if (!controller.signal.aborted) {
                    setResponse({ key: requestKey, problem, error: '' });
                }
            })
            .catch((requestError) => {
                if (!controller.signal.aborted) {
                    setResponse({
                        key: requestKey,
                        problem: null,
                        error: getProblemErrorMessage(requestError)
                    });
                }
            });
        return () => controller.abort();
    }, [requestKey]);

    const loading = response.key !== requestKey;
    const problem = loading ? null : response.problem;
    const error = loading ? '' : response.error;
    const problemUnavailable = !loading && !error && problem?.status !== 'PUBLISHED';
    const examples = problem?.examples || [];
    const previousPath = location.state?.from?.pathname;
    const backToProblems = previousPath?.startsWith('/practice/')
        ? `${previousPath}${location.state.from.search || ''}`
        : problem?.patterns?.[0]
            ? problem.categories?.[0]
                ? `/practice/categories/${encodeURIComponent(problem.categories[0])}/patterns/${encodeURIComponent(problem.patterns[0].slug)}`
                : `/practice/patterns/${encodeURIComponent(problem.patterns[0].slug)}`
            : problem?.categories?.[0]
                ? `/practice/categories/${encodeURIComponent(problem.categories[0])}`
                : '/practice';

    const resizeTestPanel = (event) => {
        if (event.target.closest('button')) return;
        event.preventDefault();
        const handle = event.currentTarget;
        const panel = handle.closest('.problem-workspace-editor-panel');
        if (!panel) return;
        handle.setPointerCapture(event.pointerId);
        const updateHeight = (pointerEvent) => {
            const bounds = panel.getBoundingClientRect();
            const nextHeight = Math.min(
                MAX_TEST_PANEL_HEIGHT,
                Math.max(MIN_TEST_PANEL_HEIGHT, bounds.bottom - pointerEvent.clientY)
            );
            setTestPanelHeight(nextHeight);
            setTestPanelCollapsed(false);
        };
        updateHeight(event);
        const handleMove = (pointerEvent) => updateHeight(pointerEvent);
        const handleUp = () => {
            handle.removeEventListener('pointermove', handleMove);
            handle.removeEventListener('pointerup', handleUp);
            handle.removeEventListener('pointercancel', handleUp);
        };
        handle.addEventListener('pointermove', handleMove);
        handle.addEventListener('pointerup', handleUp);
        handle.addEventListener('pointercancel', handleUp);
    };

    useEffect(() => {
        if (problem
            && codeState.slug === problem.slug
            && codeState.language === language) {
            localStorage.setItem(
                `dsabg:problem-draft:${problem.slug}:${language}`,
                codeState.value
            );
        }
    }, [problem, language, codeState]);

    const changeLanguage = (nextLanguage) => {
        if (!problem) return;
        localStorage.setItem(`dsabg:problem-draft:${problem.slug}:${language}`, code);
        setCodeState({
            slug: problem.slug,
            language: nextLanguage,
            value: getDraft(problem.slug, nextLanguage)
        });
        setLanguage(nextLanguage);
    };

    if (loading) {
        return <main className="problem-workspace-state" role="status">Loading coding workspace…</main>;
    }

    if (error || !problem || problemUnavailable) {
        return (
            <main className="problem-workspace-state">
                <p role="alert">
                    {error || (problemUnavailable ? 'This problem is not available.' : 'Problem details are unavailable.')}
                </p>
                <Link to={`/problems/${encodeURIComponent(slug)}`}>Back to problem</Link>
                {error && (
                    <button type="button" onClick={() => setRetry((value) => value + 1)}>
                        Try again
                    </button>
                )}
            </main>
        );
    }

    return (
        <main className="problem-workspace">
            <header className="problem-workspace-header">
                <Link className="problem-workspace-brand" to="/practice" aria-label="DSABG Practice">
                    <span className="problem-workspace-brand-mark">D</span>
                    <span>DSABG <small>Practice</small></span>
                </Link>
                <Link className="problem-workspace-back" to={backToProblems}>
                    <span aria-hidden="true">←</span> Back to problems
                </Link>
                <span className="problem-workspace-context">Coding workspace</span>
            </header>

            <div className="problem-workspace-grid">
                <section className="problem-workspace-description" aria-label="Problem information">
                    <div className="problem-workspace-scroll">
                        <div className="problem-description-heading">
                            <div>
                                <p className="problem-workspace-eyebrow">Problem</p>
                                <h1>{problem.title}</h1>
                            </div>
                            <span className={`practice-difficulty practice-difficulty--${problem.difficulty?.toLowerCase()}`}>
                                {problem.difficulty
                                    ? `${problem.difficulty.charAt(0)}${problem.difficulty.slice(1).toLowerCase()}`
                                    : 'Unknown'}
                            </span>
                        </div>
                        <div className="problem-workspace-tags">
                            {(problem.categories || []).map((category) => (
                                <span className="problem-workspace-tag" key={category}>{category}</span>
                            ))}
                            {(problem.patterns || []).map((pattern) => (
                                <span className="problem-workspace-tag" key={pattern.slug}>
                                    {pattern.name}
                                </span>
                            ))}
                        </div>
                        <section className="problem-workspace-section">
                            <h2>Description</h2>
                            <ProblemMarkdown content={problem.statementMd} />
                        </section>
                        {problem.inputFormat && (
                            <section className="problem-workspace-section">
                                <h2>Input format</h2>
                                <ProblemMarkdown content={problem.inputFormat} />
                            </section>
                        )}
                        {problem.outputFormat && (
                            <section className="problem-workspace-section">
                                <h2>Output format</h2>
                                <ProblemMarkdown content={problem.outputFormat} />
                            </section>
                        )}
                        {examples.length > 0 && (
                            <section className="problem-workspace-section">
                                <h2>Examples</h2>
                                {examples.map((example, index) => (
                                    <article className="workspace-example" key={example.exampleId}>
                                        <h3>Example {example.displayOrder || index + 1}</h3>
                                        <h4>Input</h4>
                                        <pre>{example.inputData}</pre>
                                        <h4>Output</h4>
                                        <pre>{example.expectedOutput}</pre>
                                        {example.explanation && (
                                            <>
                                                <h4>Explanation</h4>
                                                <ProblemMarkdown content={example.explanation} />
                                            </>
                                        )}
                                    </article>
                                ))}
                            </section>
                        )}
                        {problem.constraintsText && (
                            <section className="problem-workspace-section">
                                <h2>Constraints</h2>
                                <ProblemMarkdown content={problem.constraintsText} />
                            </section>
                        )}
                    </div>
                </section>

                <section
                    className={`problem-workspace-editor-panel${testPanelCollapsed ? ' test-panel-collapsed' : ''}`}
                    aria-label="Code editor and test output"
                    style={{
                        '--test-panel-height': testPanelCollapsed ? '0px' : `${testPanelHeight}px`
                    }}
                >
                    <div className="problem-editor-toolbar">
                        <div className="problem-editor-language-control">
                            <label htmlFor="problem-editor-language">Language</label>
                            <select
                                id="problem-editor-language"
                                value={language}
                                onChange={(event) => changeLanguage(event.target.value)}
                            >
                                {LANGUAGES.map((item) => (
                                    <option key={item.id} value={item.id}>{item.label}</option>
                                ))}
                            </select>
                        </div>
                        <div className="problem-editor-toolbar-actions">
                            <span className="problem-draft-status">
                                {code ? 'Code saved locally' : 'Starter code unavailable'}
                            </span>
                            <button
                                className="problem-reset-button"
                                type="button"
                                onClick={() => setCodeState({ slug, language, value: '' })}
                                title="Clear the editor"
                            >
                                <svg viewBox="0 0 20 20" aria-hidden="true">
                                    <path d="M4.5 6.5h11M8 6.5V4.8h4v1.7m2.5 0-.7 9H6.2l-.7-9m3.3 2.2v4.5m2.4-4.5v4.5" />
                                </svg>
                                Reset
                            </button>
                            <button
                                className="problem-run-button"
                                type="button"
                                disabled
                                title="No code execution endpoint is available"
                            >
                                <svg viewBox="0 0 20 20" aria-hidden="true">
                                    <path d="m7 4.8 8 5.2-8 5.2z" />
                                </svg>
                                Run
                            </button>
                            <button
                                className="problem-submit-button"
                                type="button"
                                disabled
                                title="No solution submission endpoint is available"
                            >
                                <svg viewBox="0 0 20 20" aria-hidden="true">
                                    <path d="M10 14.8V5.2m-4 4 4-4 4 4M4.5 15.5h11" />
                                </svg>
                                Submit
                            </button>
                        </div>
                    </div>

                    <div className="problem-editor">
                        <Editor
                            height="100%"
                            language={language}
                            theme="vs-dark"
                            value={code}
                            onChange={(value) => setCodeState({
                                slug,
                                language,
                                value: value ?? ''
                            })}
                            loading={<div className="problem-editor-loading">Opening editor…</div>}
                            options={{
                                ariaLabel: `${problem.title} code editor`,
                                automaticLayout: true,
                                fontSize: 14,
                                fontFamily: "'Cascadia Code', 'Fira Code', Consolas, monospace",
                                lineNumbers: 'on',
                                tabSize: 4,
                                autoIndent: 'full',
                                formatOnPaste: true,
                                formatOnType: true,
                                minimap: { enabled: false },
                                scrollBeyondLastLine: false,
                                wordWrap: 'on',
                                padding: { top: 16 }
                            }}
                        />
                    </div>

                    <div
                        className="problem-test-panel-resizer"
                        role="separator"
                        aria-label="Resize test case panel"
                        aria-orientation="horizontal"
                        aria-valuemin={MIN_TEST_PANEL_HEIGHT}
                        aria-valuemax={MAX_TEST_PANEL_HEIGHT}
                        aria-valuenow={testPanelCollapsed ? 0 : testPanelHeight}
                        tabIndex={0}
                        onPointerDown={resizeTestPanel}
                        onKeyDown={(event) => {
                            if (event.key === 'ArrowUp') {
                                setTestPanelHeight((height) => Math.min(MAX_TEST_PANEL_HEIGHT, height + 20));
                                setTestPanelCollapsed(false);
                            } else if (event.key === 'ArrowDown') {
                                setTestPanelHeight((height) => Math.max(MIN_TEST_PANEL_HEIGHT, height - 20));
                                setTestPanelCollapsed(false);
                            }
                        }}
                    >
                        <button
                            className="problem-test-panel-toggle"
                            type="button"
                            onClick={() => setTestPanelCollapsed((collapsed) => !collapsed)}
                            aria-expanded={!testPanelCollapsed}
                        >
                            {testPanelCollapsed ? 'Show test cases' : 'Hide test cases'}
                        </button>
                    </div>
                    <section className="problem-workspace-output" aria-label="Test cases and execution results" aria-live="polite">
                        <div className="problem-output-header">
                            <div className="problem-test-tabs" role="tablist" aria-label="Test results and input">
                                <button
                                    type="button"
                                    role="tab"
                                    aria-selected={activeTestTab === 'results'}
                                    className={activeTestTab === 'results' ? 'is-active' : ''}
                                    onClick={() => setActiveTestTab('results')}
                                >
                                    Test Results
                                </button>
                                <button
                                    type="button"
                                    role="tab"
                                    aria-selected={activeTestTab === 'custom'}
                                    className={activeTestTab === 'custom' ? 'is-active' : ''}
                                    onClick={() => setActiveTestTab('custom')}
                                >
                                    Custom Input
                                </button>
                            </div>
                            {activeTestTab === 'results' && (
                                <span className="problem-results-unavailable">Not run</span>
                            )}
                        </div>
                        {activeTestTab === 'custom' ? (
                            <>
                                <label className="problem-custom-input-label" htmlFor="problem-custom-input">
                                    Custom input
                                </label>
                                <textarea
                                    id="problem-custom-input"
                                    value={customInput}
                                    onChange={(event) => setCustomInput(event.target.value)}
                                    placeholder="Enter custom input"
                                />
                                <p className="problem-execution-notice">
                                    Java is selected. The problem API does not provide starter code or an execution endpoint; this input remains local.
                                </p>
                            </>
                        ) : (
                            <>
                                <div className="problem-test-result-grid">
                                    <div className="problem-output-example">
                                        <h3>Actual output</h3>
                                        <pre>No execution result is available.</pre>
                                    </div>
                                    <div className="problem-output-example">
                                        <h3>Expected output</h3>
                                        <pre>Public examples are shown in the problem description.</pre>
                                    </div>
                                    <div className="problem-output-metrics">
                                        <span>Status <strong>Not run</strong></span>
                                        <span>Execution time <strong>—</strong></span>
                                        <span>Memory <strong>—</strong></span>
                                    </div>
                                </div>
                                <p className="problem-execution-notice">
                                    The problem API does not provide starter code or run/submit endpoints. No execution result is being simulated.
                                </p>
                            </>
                        )}
                    </section>
                </section>
            </div>
        </main>
    );
};

export default ProblemCodingWorkspace;
