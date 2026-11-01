
-- V4: Remove old JSON column and create problem patterns structure

-- 1. Drop the old categories JSON column if it exists.
SET @drop_categories_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'problems'
          AND column_name = 'categories'
    ),
    'ALTER TABLE problems DROP COLUMN categories',
    'SELECT 1'
);

PREPARE drop_categories_stmt FROM @drop_categories_sql;
EXECUTE drop_categories_stmt;
DEALLOCATE PREPARE drop_categories_stmt;


-- 2. Create patterns.
CREATE TABLE IF NOT EXISTS patterns (
                                        pattern_id BIGINT NOT NULL AUTO_INCREMENT,
                                        name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (pattern_id),
    UNIQUE KEY uk_patterns_slug (slug)
    );


-- 3. Map categories to patterns.
CREATE TABLE IF NOT EXISTS category_patterns (
                                                 category_id BIGINT NOT NULL,
                                                 pattern_id BIGINT NOT NULL,

                                                 PRIMARY KEY (category_id, pattern_id),

    CONSTRAINT fk_category_patterns_category
    FOREIGN KEY (category_id)
    REFERENCES categories(category_id)
    ON DELETE CASCADE,

    CONSTRAINT fk_category_patterns_pattern
    FOREIGN KEY (pattern_id)
    REFERENCES patterns(pattern_id)
    ON DELETE CASCADE
    );


-- 4. Map problems to patterns.
CREATE TABLE IF NOT EXISTS problem_patterns (
                                                problem_id BIGINT NOT NULL,
                                                pattern_id BIGINT NOT NULL,

                                                PRIMARY KEY (problem_id, pattern_id),

    CONSTRAINT fk_problem_patterns_problem
    FOREIGN KEY (problem_id)
    REFERENCES problems(problem_id)
    ON DELETE CASCADE,

    CONSTRAINT fk_problem_patterns_pattern
    FOREIGN KEY (pattern_id)
    REFERENCES patterns(pattern_id)
    ON DELETE CASCADE
    );


-- 5. Create reverse indexes only if they do not exist.
SET @create_category_index_sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'category_patterns'
          AND index_name = 'idx_category_patterns_pattern'
    ),
    'CREATE INDEX idx_category_patterns_pattern ON category_patterns(pattern_id)',
    'SELECT 1'
);

PREPARE category_index_stmt FROM @create_category_index_sql;
EXECUTE category_index_stmt;
DEALLOCATE PREPARE category_index_stmt;


SET @create_problem_index_sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'problem_patterns'
          AND index_name = 'idx_problem_patterns_pattern'
    ),
    'CREATE INDEX idx_problem_patterns_pattern ON problem_patterns(pattern_id)',
    'SELECT 1'
);

PREPARE problem_index_stmt FROM @create_problem_index_sql;
EXECUTE problem_index_stmt;
DEALLOCATE PREPARE problem_index_stmt;
