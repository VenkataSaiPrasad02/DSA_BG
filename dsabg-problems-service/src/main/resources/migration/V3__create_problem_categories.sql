


CREATE TABLE categories (
                            category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE problem_categories (
                                    problem_id BIGINT NOT NULL,
                                    category_id BIGINT NOT NULL,

                                    PRIMARY KEY (problem_id, category_id),

                                    CONSTRAINT fk_problem_categories_problem
                                        FOREIGN KEY (problem_id)
                                            REFERENCES problems(problem_id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_problem_categories_category
                                        FOREIGN KEY (category_id)
                                            REFERENCES categories(category_id)
                                            ON DELETE CASCADE
);
