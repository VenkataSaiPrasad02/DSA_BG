-- 1. Rename the existing primary key
ALTER TABLE problems
    RENAME COLUMN id TO problem_id;

-- 2. Create a table for problem examples
CREATE TABLE problem_examples (
                                  example_id BIGINT PRIMARY KEY AUTO_INCREMENT,

                                  problem_id BIGINT NOT NULL,

                                  input_data MEDIUMTEXT NOT NULL,
                                  expected_output MEDIUMTEXT NOT NULL,
                                  explanation TEXT,

                                  display_order INT NOT NULL DEFAULT 1,

                                  CONSTRAINT fk_problem_examples_problem
                                      FOREIGN KEY (problem_id)
                                          REFERENCES problems(problem_id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT uk_problem_example_order
                                      UNIQUE (problem_id, display_order)
);