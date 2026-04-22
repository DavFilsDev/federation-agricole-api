ALTER TABLE collectivity
    ADD COLUMN unique_number VARCHAR(50) UNIQUE,
    ADD COLUMN unique_name VARCHAR(100) UNIQUE;

ALTER TABLE collectivity DROP COLUMN IF EXISTS unique_number;
ALTER TABLE collectivity DROP COLUMN IF EXISTS unique_name;

ALTER TABLE collectivity
    ADD COLUMN name VARCHAR(100) UNIQUE,
    ADD COLUMN number INTEGER UNIQUE;