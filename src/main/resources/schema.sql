CREATE TABLE IF NOT EXISTS bubble_alloc (
    biz_tag VARCHAR(64) PRIMARY KEY,
    max_id BIGINT NOT NULL,
    step INT NOT NULL,
    version INT NOT NULL
);
