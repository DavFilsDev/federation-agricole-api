ALTER TABLE collectivity
    ADD COLUMN unique_number VARCHAR(50) UNIQUE,
    ADD COLUMN unique_name VARCHAR(100) UNIQUE;