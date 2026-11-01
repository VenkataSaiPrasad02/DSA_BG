import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom';
import usePublishedProblems from '../hooks/usePublishedProblems';
import { getPublishedProblems, getProblemErrorMessage } from '../services/problemService';
import '../styles/Practice.css';

const PAGE_SIZE = 10;
const EMPTY_PROBLEMS = [];

const PracticeProblems = ({ allProblems = false }) => {
    const { category, patternSlug } = useParams();
    const location = useLocation();
    const [searchParams, setSearchParams] = useSearchParams();
    const difficulty = searchParams.get('difficulty') || '';
    const categoryFilter = searchParams.get('category') || '';
    const pageValue = Number.parseInt(searchParams.get('page') || '0', 10);
    const page = Number.isFinite(pageValue) && pageValue >= 0 ? pageValue : 0;
    const [searchDraftState, setSearchDraftState] = useState({ source: '', value: '' });
    const [categoryDraftState, setCategoryDraftState] = useState({ source: '', value: '' });
    const search = searchParams.get('search') || '';
    const searchDraft = searchDraftState.source === search ? searchDraftState.value : search;
    const categoryDraft = categoryDraftState.source === categoryFilter
        ? categoryDraftState.value
        : categoryFilter;
    const { loading: collectionLoading, problems: collectionProblems, error: collectionError, retry: retryCollection } =
        usePublishedProblems({ enabled: !allProblems });
    const [retryPage, setRetryPage] = useState(0);
    const pageRequestKey = JSON.stringify({
        allProblems,
        page,
        search,
        difficulty,
        categoryFilter,
        retryPage
    });
    const [pageResponse, setPageResponse] = useState({
        key: '',
        page: null,
        error: ''
    });
    const pageLoading = allProblems && pageResponse.key !== pageRequestKey;
    const pageError = allProblems && !pageLoading ? pageResponse.error : '';
    const pageData = allProblems && !pageLoading ? pageResponse.page : null;
    const loading = allProblems ? pageLoading : collectionLoading;
    const problems = allProblems ? (pageData?.content || EMPTY_PROBLEMS) : collectionProblems;
    const error = allProblems ? pageError : collectionError;
    const retry = allProblems ? () => setRetryPage((value) => value + 1) : retryCollection;

    useEffect(() => {
        if (!allProblems) return undefined;
        const controller = new AbortController();
        getPublishedProblems({
            search,
            difficulty,
            categories: categoryFilter ? [categoryFilter] : [],
            page,
            size: PAGE_SIZE
        }, { signal: controller.signal })
            .then((result) => {
                if (!controller.signal.aborted) {
                    if (!Array.isArray(result?.content)
                        || !Number.isInteger(result.totalElements)
                        || !Number.isInteger(result.totalPages)) {
                        throw new Error('The problem service returned an invalid paginated response.');
                    }
                    setPageResponse({ key: pageRequestKey, page: result, error: '' });
                }
            })
            .catch((requestError) => {
                if (!controller.signal.aborted) {
                    setPageResponse({
                        key: pageRequestKey,
                        page: null,
                        error: getProblemErrorMessage(requestError)
                    });
                }
            });
        return () => controller.abort();
    }, [allProblems, pageRequestKey, search, difficulty, categoryFilter, page]);

    const pattern = useMemo(() => problems
        .flatMap((problem) => problem.patterns || [])
        .find((item) => item.slug === patternSlug), [problems, patternSlug]);
    const patternProblemCount = problems.filter((problem) => (
        (!category || (problem.categories || []).includes(category))
        && (problem.patterns || []).some((item) => item.slug === patternSlug)
    )).length;
    const associatedTopics = useMemo(() => [...new Set(
        problems
            .filter((problem) => (
                (!category || (problem.categories || []).includes(category))
                && (problem.patterns || []).some((item) => item.slug === patternSlug)
            ))
            .flatMap((problem) => problem.categories || [])
    )].sort((a, b) => a.localeCompare(b)), [problems, category, patternSlug]);
    const pageCategories = useMemo(() => [...new Set(
        problems.flatMap((problem) => problem.categories || [])
    )].sort((a, b) => a.localeCompare(b)), [problems]);
    useEffect(() => {
        if (searchDraft === search) return undefined;
        const timer = window.setTimeout(() => {
            const nextParams = new URLSearchParams(searchParams);
            const value = searchDraft.trim();
            if (value) nextParams.set('search', value);
            else nextParams.delete('search');
            nextParams.set('page', '0');
            setSearchParams(nextParams);
        }, 300);
        return () => window.clearTimeout(timer);
    }, [searchDraft, search, searchParams, setSearchParams]);

    useEffect(() => {
        if (!allProblems || categoryDraft === categoryFilter) return undefined;
        const timer = window.setTimeout(() => {
            const nextParams = new URLSearchParams(searchParams);
            const value = categoryDraft.trim();
            if (value) nextParams.set('category', value);
            else nextParams.delete('category');
            nextParams.set('page', '0');
            setSearchParams(nextParams);
        }, 300);
        return () => window.clearTimeout(timer);
    }, [allProblems, categoryDraft, categoryFilter, searchParams, setSearchParams]);

    const filteredProblems = problems.filter((problem) => {
        const matchesCategory = !category || (problem.categories || []).includes(category);
        const matchesPattern = !patternSlug
            || (problem.patterns || []).some((item) => item.slug === patternSlug);
        const matchesDifficulty = allProblems || !difficulty || problem.difficulty === difficulty;
        const matchesSearch = allProblems || !search
            || `${problem.title} ${problem.slug}`.toLowerCase().includes(search.toLowerCase());
        return matchesCategory && matchesPattern && matchesDifficulty && matchesSearch;
    });
    const totalPages = allProblems
        ? (pageData?.totalPages || 0)
        : Math.ceil(filteredProblems.length / PAGE_SIZE);
    const currentPage = allProblems
        ? (pageData?.number ?? page)
        : Math.min(page, Math.max(totalPages - 1, 0));
    const pageProblems = allProblems
        ? filteredProblems
        : filteredProblems.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);
    const title = allProblems ? 'All Problems' : pattern?.name || category || 'Practice problems';

    const updateFilter = (key, value) => {
        const nextParams = new URLSearchParams(searchParams);
        if (value) nextParams.set(key, value);
        else nextParams.delete(key);
        nextParams.set('page', '0');
        setSearchParams(nextParams);
    };

    return (
        <section className="practice-page" aria-labelledby="practice-problems-heading">
            {!allProblems && (
                <Link
                    className="practice-back-link"
                    to={category
                        ? `/practice/categories/${encodeURIComponent(category)}`
                        : '/practice'}
                >
                    ← {category || 'Practice'}
                </Link>
            )}
            <header className="practice-header practice-problems-header">
                <div>
                    <p className="practice-eyebrow">{allProblems ? 'Problem library' : pattern ? 'Pattern' : 'Category'}</p>
                    <h1 id="practice-problems-heading">{title}</h1>
                    {allProblems && <p>Browse all problems currently returned by the published-problems API.</p>}
                    {pattern?.description && <p>{pattern.description}</p>}
                    {pattern && (
                        <p className="practice-pattern-topic-summary">
                            {patternProblemCount} {patternProblemCount === 1 ? 'problem' : 'problems'}
                            {associatedTopics.length > 0 && (
                                <> · {associatedTopics.join(', ')}</>
                            )}
                        </p>
                    )}
                </div>
            </header>

            <div className={`practice-problem-filters${allProblems ? ' practice-all-problem-filters' : ''}`}>
                <label>
                    <span>{allProblems ? 'Search by title' : 'Search problems'}</span>
                    <input
                        type="search"
                        value={searchDraft}
                        onChange={(event) => setSearchDraftState({
                            source: search,
                            value: event.target.value
                        })}
                        placeholder={allProblems ? 'Search problem title' : 'Search title or slug'}
                    />
                </label>
                <label>
                    <span>Difficulty</span>
                    <select
                        value={difficulty}
                        onChange={(event) => updateFilter('difficulty', event.target.value)}
                        disabled={!allProblems}
                    >
                        <option value="">All difficulties</option>
                        <option value="EASY">Easy</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HARD">Hard</option>
                    </select>
                </label>
                {allProblems && (
                    <>
                        <label>
                            <span>Category</span>
                            <input
                                type="search"
                                list="all-problems-category-options"
                                value={categoryDraft}
                                onChange={(event) => setCategoryDraftState({
                                    source: categoryFilter,
                                    value: event.target.value
                                })}
                                placeholder="Any category"
                            />
                            <datalist id="all-problems-category-options">
                                {pageCategories.map((name) => <option value={name} key={name} />)}
                            </datalist>
                        </label>
                        <label>
                            <span>Pattern</span>
                            <select value="" disabled aria-describedby="all-problems-filter-api-note">
                                <option value="">Pattern filtering unavailable</option>
                            </select>
                        </label>
                    </>
                )}
            </div>
            {allProblems && (
                <p className="practice-filter-note" id="all-problems-filter-api-note">
                    Search, difficulty, category, and pagination use backend filters. Pattern filtering is unavailable because the backend list API does not accept a pattern filter.
                </p>
            )}

            {loading ? (
                <div className="practice-state" role="status">Loading problems…</div>
            ) : error ? (
                <div className="practice-state practice-state-error" role="alert">
                    <p>{error}</p>
                    <button type="button" onClick={retry}>Try again</button>
                </div>
            ) : patternSlug && !pattern ? (
                <div className="practice-state">
                    <h2>Pattern not found</h2>
                    <p>No published problems currently use this pattern.</p>
                </div>
            ) : filteredProblems.length === 0 ? (
                <div className="practice-state">
                    <h2>No matching problems</h2>
                    <p>Try a different search or difficulty filter.</p>
                </div>
            ) : (
                <>
                    <div className="practice-list-heading">
                        <h2>Problems</h2>
                        <span>
                            {allProblems
                                ? `${pageData?.totalElements || 0} total`
                                : `${filteredProblems.length} total`}
                        </span>
                    </div>
                    <div className="practice-problem-list">
                        {pageProblems.map((problem) => (
                            <div className="practice-problem-row" key={problem.problemId}>
                                <Link
                                    className="practice-problem-title"
                                    to={`/problems/${encodeURIComponent(problem.slug)}`}
                                    state={{ from: location }}
                                >
                                    {problem.title}
                                </Link>
                                <div className="practice-problem-meta">
                                    <div className="practice-problem-topics" aria-label="Topics">
                                        {(problem.categories || []).map((topic) => (
                                            <span className="practice-small-tag" key={topic}>{topic}</span>
                                        ))}
                                        {(problem.patterns || []).map((item) => (
                                            <span className="practice-small-tag practice-small-tag--pattern" key={item.slug}>
                                                {item.name}
                                            </span>
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
                                            state={{ from: location }}
                                        >
                                            Solve <span aria-hidden="true">→</span>
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                    {totalPages > 1 && (
                        <nav className="practice-pagination" aria-label="Problem pages">
                            <button
                                type="button"
                                onClick={() => updateFilter('page', String(currentPage - 1))}
                                disabled={currentPage === 0}
                            >
                                Previous
                            </button>
                            <span>Page {currentPage + 1} of {totalPages}</span>
                            <button
                                type="button"
                                onClick={() => updateFilter('page', String(currentPage + 1))}
                                disabled={currentPage + 1 >= totalPages}
                            >
                                Next
                            </button>
                        </nav>
                    )}
                </>
            )}
        </section>
    );
};

export default PracticeProblems;
