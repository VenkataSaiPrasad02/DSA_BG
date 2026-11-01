
-- V6: Create problem_language_configs table and add missing columns to problem_test_cases

-- 1. Create problem_language_configs table for per-language starter code configuration
CREATE TABLE problem_language_configs (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    class_name VARCHAR(200),
    method_name VARCHAR(200),
    return_type VARCHAR(200),
    parameter_types JSON,
    parameter_names JSON,
    starter_code MEDIUMTEXT,

    CONSTRAINT fk_language_configs_problem
        FOREIGN KEY (problem_id)
            REFERENCES problems(problem_id)
            ON DELETE CASCADE,

    CONSTRAINT uk_problem_language
        UNIQUE (problem_id, language)
);


-- 2. Add missing columns to problem_test_cases if they do not exist
SET @add_is_hidden = IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'problem_test_cases' AND column_name = 'is_hidden'
    ),
    'ALTER TABLE problem_test_cases ADD COLUMN is_hidden BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE stmt FROM @add_is_hidden;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_is_active = IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'problem_test_cases' AND column_name = 'is_active'
    ),
    'ALTER TABLE problem_test_cases ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE stmt FROM @add_is_active;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_created_at = IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'problem_test_cases' AND column_name = 'created_at'
    ),
    'ALTER TABLE problem_test_cases ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT 1'
);
PREPARE stmt FROM @add_created_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_updated_at = IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'problem_test_cases' AND column_name = 'updated_at'
    ),
    'ALTER TABLE problem_test_cases ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT 1'
);
PREPARE stmt FROM @add_updated_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
