
-- V5: Create problem_test_cases table for acceptance test cases (LeetCode style)

CREATE TABLE problem_test_cases (
    test_case_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    input_data MEDIUMTEXT NOT NULL,
    expected_output MEDIUMTEXT NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 1,

    CONSTRAINT fk_test_cases_problem
        FOREIGN KEY (problem_id)
        REFERENCES problems(problem_id)
        ON DELETE CASCADE,

    CONSTRAINT uk_problem_testcase_order
        UNIQUE (problem_id, display_order)
);
