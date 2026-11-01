export const BATTLE_PREVIEW_PROBLEMS = [
    {
        id: 'preview-1',
        number: '01',
        title: 'Two Sum',
        description: 'Find the indices of two numbers whose values add up to the target.',
        examples: [
            {
                input: 'nums = [2, 7, 11, 15], target = 9',
                output: '[0, 1]',
                explanation: 'nums[0] + nums[1] equals 9.'
            }
        ],
        constraints: ['2 ≤ nums.length ≤ 10⁴', 'Each input has exactly one solution.'],
        inputFormat: 'An integer array nums and an integer target.',
        outputFormat: 'Return the indices of the two values that sum to target.'
    },
    {
        id: 'preview-2',
        number: '02',
        title: 'Valid Parentheses',
        description: 'Determine whether every opening bracket is closed in the correct order.',
        examples: [
            {
                input: 's = "([])"',
                output: 'true',
                explanation: 'Each opening bracket has a matching closing bracket.'
            }
        ],
        constraints: ['1 ≤ s.length ≤ 10⁴', 's contains only bracket characters.'],
        inputFormat: 'A string s containing bracket characters.',
        outputFormat: 'Return true if the brackets are valid; otherwise return false.'
    },
    {
        id: 'preview-3',
        number: '03',
        title: 'Maximum Subarray',
        description: 'Find the contiguous subarray with the largest sum.',
        examples: [
            {
                input: 'nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]',
                output: '6',
                explanation: 'The subarray [4, -1, 2, 1] has the largest sum.'
            }
        ],
        constraints: ['1 ≤ nums.length ≤ 10⁵', '-10⁴ ≤ nums[i] ≤ 10⁴'],
        inputFormat: 'An integer array nums.',
        outputFormat: 'Return the largest sum of any non-empty contiguous subarray.'
    }
];

export const BATTLE_PREVIEW_PROGRESS = {
    A: ['preview-1'],
    B: ['preview-1']
};

export const MATCH_RESULT_PREVIEW = {
    scores: { A: 2, B: 1 },
    mvpSolved: { A: 2, B: 1 }
};
