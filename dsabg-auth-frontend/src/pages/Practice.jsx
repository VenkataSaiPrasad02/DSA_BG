import { Fragment, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import usePublishedProblems from '../hooks/usePublishedProblems';
import '../styles/Practice.css';

const PATTERN_NAMES = [
    'Counting',
    'Integer Overflow',
    'Bit Manipulation',
    'Binary Exponentiation',
    'Sieve of Eratosthenes',
    'Simulation',
    'String',
    'String Simulation',
    'Hashing',
    'Hash Map',
    'Prefix Sum',
    'Two Pointers',
    'Fast and Slow Pointers',
    'In-place Array',
    'Sliding Window',
    'Sliding Window + Hashing',
    'Binary Search',
    'Binary Search on Answer',
    'Sorting + Stack',
    'Stack',
    'Greedy',
    'Greedy + Priority Queue',
    'Boyer-Moore Voting',
    'String Matching',
    'Expand Around Center',
    'Matrix Traversal',
    'Matrix Simulation',
    'In-place Matrix',
    'Linked List',
    'Tree BFS',
    'Tree DFS',
    'DFS',
    'Backtracking',
    "Kadane's Algorithm",
    '1D Dynamic Programming'
];

const PATTERN_ORDER = new Map(
    PATTERN_NAMES.map((pattern, index) => [pattern, index])
);
const PATTERN_GROUP_ENDINGS = new Set([
    'String Simulation',
    'Sliding Window + Hashing',
    'Boyer-Moore Voting',
    'Linked List'
]);

const Practice = () => {
    const { loading, problems, error, retry } = usePublishedProblems();
    const [searchParams, setSearchParams] = useSearchParams();
    const search = searchParams.get('search') || '';
    const topicFilter = searchParams.get('topic') || '';

    const { patterns, topics } = useMemo(() => {
        const patternMap = new Map();
        const topicNames = new Set();

        problems.forEach((problem) => {
            (problem.categories || []).forEach((category) => topicNames.add(category));
            (problem.patterns || []).forEach((pattern) => {
                const entry = patternMap.get(pattern.slug) || {
                    ...pattern,
                    problemIds: new Set(),
                    categories: new Set()
                };
                entry.problemIds.add(problem.problemId);
                (problem.categories || []).forEach((category) => entry.categories.add(category));
                patternMap.set(pattern.slug, entry);
            });
        });

        return {
            patterns: [...patternMap.values()]
                .map(({ problemIds, categories: patternCategories, ...pattern }) => ({
                    ...pattern,
                    count: problemIds.size,
                    categories: [...patternCategories].sort((a, b) => a.localeCompare(b))
                }))
                .sort((a, b) => (PATTERN_ORDER.get(a.name) ?? Number.MAX_SAFE_INTEGER)
                    - (PATTERN_ORDER.get(b.name) ?? Number.MAX_SAFE_INTEGER)
                    || a.name.localeCompare(b.name)),
            topics: [...topicNames].sort((a, b) => a.localeCompare(b))
        };
    }, [problems]);

    const visiblePatterns = patterns.filter((pattern) => {
        const matchesSearch = `${pattern.name} ${pattern.description || ''}`
            .toLocaleLowerCase()
            .includes(search.trim().toLocaleLowerCase());
        const matchesTopic = !topicFilter || pattern.categories.includes(topicFilter);
        return matchesSearch && matchesTopic;
    });

    const updateFilter = (key, value) => {
        const nextParams = new URLSearchParams(searchParams);
        if (value) nextParams.set(key, value);
        else nextParams.delete(key);
        setSearchParams(nextParams);
    };

    return (
        <section className="practice-page" aria-labelledby="practice-heading">
            <header className="practice-header practice-landing-header">
                <div>
                    <p className="practice-eyebrow">DSABG learning</p>
                    <h1 id="practice-heading">Practice</h1>
                    <p>Explore problem-solving patterns and improve your DSA skills.</p>
                </div>
            </header>

            <div className="practice-pattern-filters">
                <label className="practice-pattern-search">
                    <span className="visually-hidden">Search patterns</span>
                    <svg className="practice-search-icon" viewBox="0 0 20 20" aria-hidden="true">
                        <circle cx="8.5" cy="8.5" r="5.5" />
                        <path d="m13 13 4 4" />
                    </svg>
                    <input
                        type="search"
                        value={search}
                        onChange={(event) => updateFilter('search', event.target.value)}
                        placeholder="Search patterns"
                    />
                </label>
                <label className="practice-topic-filter">
                    <span className="visually-hidden">Filter patterns by topic</span>
                    <select
                        value={topicFilter}
                        onChange={(event) => updateFilter('topic', event.target.value)}
                    >
                        <option value="">All topics</option>
                        {topics.map((topic) => <option value={topic} key={topic}>{topic}</option>)}
                    </select>
                </label>
            </div>
            {topicFilter && (
                <Link
                    className="practice-selected-topic-link"
                    to={`/practice/categories/${encodeURIComponent(topicFilter)}`}
                >
                    Browse {topicFilter} problems <span aria-hidden="true">→</span>
                </Link>
            )}

            {loading ? (
                <div className="practice-state" role="status">Loading practice patterns…</div>
            ) : error ? (
                <div className="practice-state practice-state-error" role="alert">
                    <p>{error}</p>
                    <button type="button" onClick={retry}>Try again</button>
                </div>
            ) : patterns.length === 0 ? (
                <div className="practice-state">
                    <h2>No practice patterns yet</h2>
                    <p>Patterns will appear when published problems are available.</p>
                </div>
            ) : visiblePatterns.length === 0 ? (
                <div className="practice-state">
                    <h2>No matching patterns</h2>
                    <p>Try a different search or topic filter.</p>
                </div>
            ) : (
                <section className="practice-section" aria-labelledby="practice-patterns-heading">
                    <div className="practice-section-heading">
                        <h2 id="practice-patterns-heading">Patterns</h2>
                        <span>{patterns.length} patterns</span>
                    </div>
                    <div className="practice-pattern-grid practice-landing-pattern-grid">
                        {visiblePatterns.map((pattern, index) => (
                            <Fragment key={pattern.slug}>
                                <Link
                                    className="practice-pattern-card practice-landing-pattern-card"
                                    to={`/practice/patterns/${encodeURIComponent(pattern.slug)}`}
                                >
                                    <span className="practice-pattern-name">{pattern.name}</span>
                                    <span className="practice-pattern-count">
                                        {pattern.count} {pattern.count === 1 ? 'problem' : 'problems'}
                                    </span>
                                    <span className="practice-card-arrow" aria-hidden="true">→</span>
                                </Link>
                                {PATTERN_GROUP_ENDINGS.has(pattern.name)
                                    && visiblePatterns.slice(index + 1).some((nextPattern) =>
                                        PATTERN_ORDER.get(nextPattern.name) > PATTERN_ORDER.get(pattern.name)
                                    )
                                    && <span className="practice-pattern-group-spacer" aria-hidden="true" />}
                            </Fragment>
                        ))}
                    </div>
                </section>
            )}
        </section>
    );
};

export default Practice;
