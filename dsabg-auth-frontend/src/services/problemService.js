import API from '../api/axios';

const PUBLISHED_PROBLEMS_CACHE_MS = 30_000;
let publishedProblemsCache = null;
let publishedProblemsCachedAt = 0;

export const getPublishedProblems = ({ search, difficulty, categories, page, size }, config = {}) => {
    const params = new URLSearchParams();

    if (search) params.set('search', search);
    if (difficulty) params.set('difficulty', difficulty);
    (categories || []).forEach((category) => params.append('categories', category));
    params.set('page', String(page));
    params.set('size', String(size));

    return API.get('/problem', { ...config, params }).then(({ data }) => data);
};

export const getAllPublishedProblems = async (config = {}, { refresh = false } = {}) => {
    if (!refresh
        && publishedProblemsCache
        && Date.now() - publishedProblemsCachedAt < PUBLISHED_PROBLEMS_CACHE_MS) {
        return publishedProblemsCache;
    }

    const problemsById = new Map();
    const problemSlugs = new Set();
    const size = 50;
    let page = 0;
    let totalElements;

    while (true) {
        const result = await getPublishedProblems({
            categories: [],
            page,
            size
        }, config);

        if (!Array.isArray(result?.content)
            || !Number.isInteger(result.totalPages)
            || !Number.isInteger(result.totalElements)) {
            throw new Error('The problem service returned an invalid paginated response.');
        }

        result.content.forEach((problem) => {
            const id = problem.problemId == null ? null : String(problem.problemId);
            const slug = typeof problem.slug === 'string' ? problem.slug : null;
            if ((id && problemsById.has(id)) || (slug && problemSlugs.has(slug))) return;
            if (id) problemsById.set(id, problem);
            else if (slug) problemsById.set(slug, problem);
            else throw new Error('The problem service returned a problem without an ID or slug.');
            if (slug) problemSlugs.add(slug);
        });
        totalElements = result.totalElements;

        if (result.last || result.totalPages === 0 || page + 1 >= result.totalPages) {
            break;
        }

        page += 1;
    }

    const problems = [...problemsById.values()];
    if (problems.length !== totalElements) {
        throw new Error('The problem service returned an incomplete set of published problems.');
    }

    publishedProblemsCache = { problems, totalElements };
    publishedProblemsCachedAt = Date.now();
    return publishedProblemsCache;
};

export const getProblemBySlug = (slug, config = {}) =>
    API.get(`/problem/${encodeURIComponent(slug)}`, config).then(({ data }) => data);

export const getProblemErrorMessage = (error) => {
    const status = error.response?.status;
    const response = error.response?.data;
    const message = typeof response === 'string'
        ? response
        : response?.message || response?.detail || response?.error;

    if (status === 404) {
        return 'This problem could not be found. It may have been removed or its slug may be incorrect.';
    }

    if (status === 400) {
        return message || 'The problem request was invalid. Check the selected filters and try again.';
    }

    if (status === 401) {
        return 'Your session has expired. Sign in again to continue.';
    }

    if (message) return message;

    if (error.request) {
        return 'The problem service could not be reached. Check the API Gateway connection and try again.';
    }

    return error.message || 'The problem request failed. Please try again.';
};
