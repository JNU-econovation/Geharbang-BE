CREATE TABLE IF NOT EXISTS review_analysis_cache (
    guest_house_post_id BIGINT NOT NULL,
    source_fingerprint VARCHAR(64) NOT NULL,
    categories_json LONGTEXT NOT NULL,
    keywords_json LONGTEXT NOT NULL,
    report LONGTEXT NOT NULL,
    review_count BIGINT NOT NULL,
    analyzed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (guest_house_post_id)
);
