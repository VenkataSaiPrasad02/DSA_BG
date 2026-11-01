import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import ProblemMarkdown from '../components/problems/ProblemMarkdown';
import { getProblemBySlug, getProblemErrorMessage } from '../services/problemService';
import 'katex/dist/katex.min.css';
import '../styles/Problems.css';

const ProblemDetail = () => {
    const { slug } = useParams();
    const location = useLocation();
    const previousLocation = location.state?.from;
    const practiceHref = previousLocation?.pathname?.startsWith('/practice/')
        ? `${previousLocation.pathname}${previousLocation.search || ''}`
        : '/practice';
    const [retry, setRetry] = useState(0);
    const requestKey = JSON.stringify({ slug, retry });
    const [response, setResponse] = useState({ key: '', problem: null, error: '' });

    useEffect(() => {
        const controller = new AbortController();

        getProblemBySlug(JSON.parse(requestKey).slug, { signal: controller.signal })
            .then((data) => {
                if (!controller.signal.aborted) {
                    setResponse({ key: requestKey, problem: data, error: '' });
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

    if (loading) {
        return <div className="problems-message" role="status">Loading problem…</div>;
    }

    if (error) {
        return (
            <section className="problem-detail-page">
                <Link className="problem-back-link" to={practiceHref}>← Practice topics</Link>
                <div className="problems-message problems-error" role="alert">
                    <p>{error}</p>
                    <button type="button" onClick={() => setRetry((value) => value + 1)}>
                        Try again
                    </button>
                </div>
            </section>
        );
    }

    if (!problem) {
        return <div className="problems-message" role="status">Loading problem…</div>;
    }

    if (problem.status !== 'PUBLISHED') {
        return (
            <section className="problem-detail-page">
                <Link className="problem-back-link" to={practiceHref}>← Practice</Link>
                <div className="problems-message" role="status">
                    This problem is not available.
                </div>
            </section>
        );
    }

    return (
        <article className="problem-detail-page">
            <header className="problem-detail-topbar">
                <Link className="problem-back-link" to={practiceHref}>
                    <span aria-hidden="true">←</span> Practice
                </Link>
                <Link
                    className="problem-solve-action"
                    to={`/problems/${encodeURIComponent(problem.slug)}/solve`}
                    state={{ from: location }}
                >
                    <span aria-hidden="true">&lt;/&gt;</span>
                    Solve
                </Link>
            </header>
            <header className="problem-detail-header">
                    <div className="problem-title-line">
                        <span className="problem-number" aria-label={`Problem ${problem.problemId}`}>
                            #{problem.problemId}
                        </span>
                        <h1>{problem.title}</h1>
                    </div>
                    <div className="problem-detail-meta">
                        <span className={`problem-difficulty problem-difficulty--${problem.difficulty?.toLowerCase()}`}>
                            {problem.difficulty
                                ? `${problem.difficulty.charAt(0)}${problem.difficulty.slice(1).toLowerCase()}`
                                : 'Unknown'}
                        </span>
                        {problem.categories?.length > 0 && (
                            <div className="problem-topics" aria-label="Topics">
                                {problem.categories.map((category) => (
                                    <Link
                                        className="problem-topic-tag"
                                        to={`/practice/categories/${encodeURIComponent(category)}`}
                                        key={category}
                                    >
                                        {category}
                                    </Link>
                                ))}
                            </div>
                        )}
                        {problem.patterns?.length > 0 && (
                            <div className="problem-topics" aria-label="Patterns">
                                {problem.patterns.map((pattern) => (
                                    <Link
                                        className="problem-pattern-tag"
                                        to={`/practice/patterns/${encodeURIComponent(pattern.slug)}`}
                                        key={pattern.slug}
                                    >
                                        {pattern.name}
                                    </Link>
                                ))}
                            </div>
                        )}
                    </div>
            </header>

            <div className="problem-detail-layout">
                <div className="problem-detail-content">
                    <nav className="problem-detail-tabs" aria-label="Problem information">
                        <a className="problem-detail-tab problem-detail-tab--active" href="#description">
                            Description
                        </a>
                    </nav>
                <section className="problem-detail-section problem-statement-section">
                    <h2 className="problem-section-heading" id="description">Description</h2>
                    <ProblemMarkdown content={problem.statementMd} />
                </section>

                {(problem.inputFormat || problem.outputFormat) && (
                    <section className="problem-detail-section problem-io-section">
                        {problem.inputFormat && (
                            <div className="problem-format-block" id="input-format">
                                <h2 className="problem-section-heading">Input Format</h2>
                                <ProblemMarkdown content={problem.inputFormat} />
                            </div>
                        )}
                        {problem.outputFormat && (
                            <div className="problem-format-block" id="output-format">
                                <h2 className="problem-section-heading">Output Format</h2>
                                <ProblemMarkdown content={problem.outputFormat} />
                            </div>
                        )}
                    </section>
                )}

                {problem.examples?.length > 0 && (
                    <section className="problem-detail-section" aria-labelledby="problem-examples-heading">
                        <h2 className="problem-section-heading" id="problem-examples-heading">
                            Examples
                        </h2>
                        <div className="problem-examples">
                            {problem.examples.map((example, index) => (
                                <article className="problem-example" key={example.exampleId}>
                                    <h3>Example {example.displayOrder || index + 1}</h3>
                                    <div className="problem-example-field">
                                        <h4>Input</h4>
                                        <pre><code>{example.inputData}</code></pre>
                                    </div>
                                    <div className="problem-example-field">
                                        <h4>Output</h4>
                                        <pre><code>{example.expectedOutput}</code></pre>
                                    </div>
                                    {example.explanation && (
                                        <div className="problem-example-explanation">
                                            <h4>Explanation</h4>
                                            <ProblemMarkdown content={example.explanation} />
                                        </div>
                                    )}
                                </article>
                            ))}
                        </div>
                    </section>
                )}

                {problem.constraintsText && (
                    <section className="problem-detail-section" aria-labelledby="problem-constraints-heading">
                        <h2 className="problem-section-heading" id="problem-constraints-heading">
                            Constraints
                        </h2>
                        <ProblemMarkdown content={problem.constraintsText} />
                    </section>
                )}
                </div>

                <aside className="problem-detail-outline" aria-label="Problem sections">
                    <p className="problem-outline-title">On this page</p>
                    <a href="#description">Description</a>
                    {problem.inputFormat && <a href="#input-format">Input format</a>}
                    {problem.outputFormat && <a href="#output-format">Output format</a>}
                    {problem.examples?.length > 0 && <a href="#problem-examples-heading">Examples</a>}
                    {problem.constraintsText && <a href="#problem-constraints-heading">Constraints</a>}
                    <div className="problem-outline-divider" />
                    <p>Ready to try it yourself?</p>
                    <Link
                        className="problem-solve-action problem-outline-solve"
                        to={`/problems/${encodeURIComponent(problem.slug)}/solve`}
                        state={{ from: location }}
                    >
                        <span aria-hidden="true">&lt;/&gt;</span>
                        Open workspace
                    </Link>
                </aside>
            </div>
        </article>
    );
};

export default ProblemDetail;
