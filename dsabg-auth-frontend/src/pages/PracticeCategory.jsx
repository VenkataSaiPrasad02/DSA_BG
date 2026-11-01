import { Link, useParams, useSearchParams } from 'react-router-dom';
import TopicIcon from '../components/practice/TopicIcon';
import usePublishedProblems from '../hooks/usePublishedProblems';
import '../styles/Practice.css';

const PAGE_SIZE = 10;

const PracticeCategory = () => {
    const { category } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();
    const { loading, problems, error, retry } = usePublishedProblems();
    const requestedPage = Number.parseInt(searchParams.get('page') || '0', 10);
    const page = Number.isFinite(requestedPage) && requestedPage >= 0 ? requestedPage : 0;

    const categoryProblems = problems.filter((problem) =>
        (problem.categories || []).includes(category)
    );
    const patternMap = new Map();
    categoryProblems.forEach((problem) => {
        (problem.patterns || []).forEach((pattern) => {
            const existing = patternMap.get(pattern.slug);
            if (existing) {
                existing.problemIds.add(problem.problemId);
            } else {
                patternMap.set(pattern.slug, {
                    ...pattern,
                    problemIds: new Set([problem.problemId])
                });
            }
        });
    });
    const patterns = [...patternMap.values()]
        .map(({ problemIds, ...pattern }) => ({
            ...pattern,
            count: problemIds.size
        }))
        .sort((left, right) => left.name.localeCompare(right.name));
    const totalPages = Math.ceil(categoryProblems.length / PAGE_SIZE);
    const currentPage = Math.min(page, Math.max(totalPages - 1, 0));
    const pageProblems = categoryProblems.slice(
        currentPage * PAGE_SIZE,
        (currentPage + 1) * PAGE_SIZE
    );

    const updatePage = (nextPage) => {
        const nextParams = new URLSearchParams(searchParams);
        nextParams.set('page', String(nextPage));
        setSearchParams(nextParams);
    };

    return (
        <section className="practice-page" aria-labelledby="practice-category-heading">
            <Link className="practice-back-link" to="/practice">← Practice</Link>
            <header className="practice-header practice-category-header">
                <TopicIcon name={category} />
                <div>
                    <p className="practice-eyebrow">Category</p>
                    <h1 id="practice-category-heading">{category}</h1>
                    {!loading && !error && (
                        <p>
                            {categoryProblems.length} published{' '}
                            {categoryProblems.length === 1 ? 'problem' : 'problems'} in this topic.
                        </p>
                    )}
                </div>
            </header>

            {loading ? (
                <div className="practice-state" role="status">Loading topic patterns…</div>
            ) : error ? (
                <div className="practice-state practice-state-error" role="alert">
                    <p>{error}</p>
                    <button type="button" onClick={retry}>Try again</button>
                </div>
            ) : categoryProblems.length === 0 ? (
                <div className="practice-state">
                    <h2>Topic not found</h2>
                    <p>No published problems currently use this category.</p>
                </div>
            ) : (
                <>
                    {patterns.length > 0 ? (
                        <>
                            <div className="practice-section-heading">
                                <div>
                                    <p className="practice-eyebrow">Explore by approach</p>
                                    <h2>Patterns</h2>
                                </div>
                                <span>{patterns.length} patterns</span>
                            </div>
                            <div className="practice-pattern-grid">
                                {patterns.map((pattern) => (
                                    <Link
                                        className="practice-pattern-card"
                                        to={`/practice/categories/${encodeURIComponent(category)}/patterns/${encodeURIComponent(pattern.slug)}`}
                                        key={pattern.slug}
                                    >
                                        <span className="practice-pattern-mark" aria-hidden="true">&lt;/&gt;</span>
                                        <span className="practice-pattern-card-body">
                                            <span className="practice-pattern-name">{pattern.name}</span>
                                            {pattern.description && (
                                                <span className="practice-pattern-description">
                                                    {pattern.description}
                                                </span>
                                            )}
                                            <span className="practice-pattern-count">
                                                {pattern.count} {pattern.count === 1 ? 'problem' : 'problems'}
                                            </span>
                                        </span>
                                        <span className="practice-card-arrow" aria-hidden="true">→</span>
                                    </Link>
                                ))}
                            </div>
                        </>
                    ) : (
                        <div className="practice-inline-empty">
                            No patterns are assigned to published problems in this topic.
                        </div>
                    )}

                    <section className="practice-category-problems" aria-labelledby="category-problems-heading">
                        <div className="practice-section-heading">
                            <div>
                                <p className="practice-eyebrow">Topic library</p>
                                <h2 id="category-problems-heading">Problems</h2>
                            </div>
                            <span>{categoryProblems.length} problems</span>
                        </div>
                        <div className="practice-problem-list">
                            {pageProblems.map((problem) => (
                                <div className="practice-problem-row" key={problem.problemId}>
                                    <Link
                                        className="practice-problem-title"
                                        to={`/problems/${encodeURIComponent(problem.slug)}`}
                                    >
                                        {problem.title}
                                    </Link>
                                    <div className="practice-problem-meta">
                                        <div className="practice-problem-topics" aria-label="Topics">
                                            {(problem.categories || []).map((topic) => (
                                                <span className="practice-small-tag" key={topic}>{topic}</span>
                                            ))}
                                        </div>
                                        <div className="practice-problem-actions">
                                            <span className={`practice-difficulty practice-difficulty--${problem.difficulty?.toLowerCase()}`}>
                                                {problem.difficulty
                                                    ? `${problem.difficulty.charAt(0)}${problem.difficulty.slice(1).toLowerCase()}`
                                                    : 'Unknown'}
                                            </span>
                                            <Link
                                                className="practice-problem-solve"
                                                to={`/problems/${encodeURIComponent(problem.slug)}/solve`}
                                            >
                                                Solve <span aria-hidden="true">→</span>
                                            </Link>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                        {totalPages > 1 && (
                            <nav className="practice-pagination" aria-label="Topic problem pages">
                                <button
                                    type="button"
                                    onClick={() => updatePage(currentPage - 1)}
                                    disabled={currentPage === 0}
                                >
                                    Previous
                                </button>
                                <span>Page {currentPage + 1} of {totalPages}</span>
                                <button
                                    type="button"
                                    onClick={() => updatePage(currentPage + 1)}
                                    disabled={currentPage + 1 >= totalPages}
                                >
                                    Next
                                </button>
                            </nav>
                        )}
                    </section>
                </>
            )}
        </section>
    );
};

export default PracticeCategory;
