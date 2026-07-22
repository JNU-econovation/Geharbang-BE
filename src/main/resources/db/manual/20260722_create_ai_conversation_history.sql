CREATE TABLE IF NOT EXISTS ai_conversation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100) NOT NULL,
    title VARCHAR(80) NOT NULL,
    last_message VARCHAR(300) NOT NULL,
    context_json LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_conversation_session_id (session_id),
    KEY idx_ai_conversation_user_updated (user_id, updated_at),
    CONSTRAINT fk_ai_conversation_user
        FOREIGN KEY (user_id) REFERENCES uuser (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ai_conversation_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    message_role VARCHAR(16) NOT NULL,
    content LONGTEXT NOT NULL,
    image_url VARCHAR(500) NULL,
    domain VARCHAR(50) NULL,
    confidence DOUBLE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_message_conversation_id (conversation_id, id),
    CONSTRAINT fk_ai_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES ai_conversation (id)
        ON DELETE CASCADE
);
