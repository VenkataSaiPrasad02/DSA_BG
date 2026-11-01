import { useEffect, useState } from 'react';
import {
    getAllPublishedProblems,
    getProblemErrorMessage
} from '../services/problemService';

const EMPTY_PROBLEMS = [];

const usePublishedProblems = ({ enabled = true } = {}) => {
    const [retry, setRetry] = useState(0);
    const requestKey = String(retry);
    const refresh = retry > 0;
    const [response, setResponse] = useState({
        key: '',
        problems: [],
        error: ''
    });

    useEffect(() => {
        if (!enabled) return undefined;
        const controller = new AbortController();

        getAllPublishedProblems(
            { signal: controller.signal },
            { refresh }
        )
            .then(({ problems }) => {
                if (!controller.signal.aborted) {
                    setResponse({ key: requestKey, problems, error: '' });
                }
            })
            .catch((requestError) => {
                if (!controller.signal.aborted) {
                    setResponse({
                        key: requestKey,
                        problems: [],
                        error: getProblemErrorMessage(requestError)
                    });
                }
            });

        return () => controller.abort();
    }, [enabled, requestKey, refresh]);

    return {
        loading: enabled && response.key !== requestKey,
        problems: response.key === requestKey ? response.problems : EMPTY_PROBLEMS,
        error: enabled && response.key === requestKey ? response.error : '',
        retry: () => setRetry((value) => value + 1)
    };
};

export default usePublishedProblems;
