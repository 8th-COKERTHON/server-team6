-- Team 6 MVP target schema for MySQL 8.4 and ERD import.
-- A match compares two memories owned by the same authenticated member.
-- Photo storage, community features, member-to-member matching, and connection ranking are excluded.
-- This documentation differs from the uncommitted V2 migration draft.

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_members_email UNIQUE (email),
    INDEX idx_members_name_id (name, id)
);

CREATE TABLE memories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    memory_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    matched_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_memories_id_member UNIQUE (id, member_id),
    CONSTRAINT fk_memories_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT ck_memories_status CHECK (status IN ('AVAILABLE', 'MATCHED', 'ARCHIVED')),
    CONSTRAINT ck_memories_matched_at
        CHECK ((status = 'MATCHED' AND matched_at IS NOT NULL) OR status <> 'MATCHED'),
    INDEX idx_memories_member_status (member_id, status, memory_date DESC, id DESC)
);

CREATE TABLE ai_recommendation_caches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    recommendation_date DATE NOT NULL,
    content TEXT NOT NULL,
    generated_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_recommendations_member_date UNIQUE (member_id, recommendation_date),
    CONSTRAINT fk_ai_recommendations_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT ck_ai_recommendations_expiry CHECK (expires_at > generated_at),
    INDEX idx_ai_recommendations_member_expiry (member_id, expires_at, generated_at)
);

CREATE TABLE balance_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_text VARCHAR(500) NOT NULL,
    opens_at DATETIME(6) NOT NULL,
    closes_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_balance_questions_period CHECK (closes_at > opens_at),
    CONSTRAINT ck_balance_questions_status
        CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'CANCELLED')),
    INDEX idx_balance_questions_status_period (status, opens_at, closes_at)
);

CREATE TABLE balance_question_options (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    option_no SMALLINT NOT NULL,
    option_text VARCHAR(300) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_balance_options_question_no UNIQUE (question_id, option_no),
    CONSTRAINT uk_balance_options_id_question UNIQUE (id, question_id),
    CONSTRAINT fk_balance_options_question FOREIGN KEY (question_id) REFERENCES balance_questions (id),
    CONSTRAINT ck_balance_options_no CHECK (option_no > 0)
);

CREATE TABLE balance_question_responses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_balance_responses_question_member UNIQUE (question_id, member_id),
    CONSTRAINT fk_balance_responses_option_question
        FOREIGN KEY (option_id, question_id) REFERENCES balance_question_options (id, question_id),
    CONSTRAINT fk_balance_responses_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX idx_balance_responses_option (option_id)
);

CREATE TABLE matching_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    score_reward BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_matching_events_type CHECK (event_type IN ('WEEKLY', 'MONTHLY', 'SPECIAL')),
    CONSTRAINT ck_matching_events_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'CANCELLED')),
    CONSTRAINT ck_matching_events_period CHECK (ends_at > starts_at),
    CONSTRAINT ck_matching_events_score CHECK (score_reward >= 0),
    INDEX idx_matching_events_upcoming (status, starts_at, id)
);

CREATE TABLE matches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NULL,
    member_id BIGINT NOT NULL,
    memory_a_id BIGINT NOT NULL,
    memory_b_id BIGINT NOT NULL,
    winner_memory_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_matches_event FOREIGN KEY (event_id) REFERENCES matching_events (id),
    CONSTRAINT fk_matches_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_matches_memory_a_owner
        FOREIGN KEY (memory_a_id, member_id) REFERENCES memories (id, member_id),
    CONSTRAINT fk_matches_memory_b_owner
        FOREIGN KEY (memory_b_id, member_id) REFERENCES memories (id, member_id),
    CONSTRAINT fk_matches_winner_owner
        FOREIGN KEY (winner_memory_id, member_id) REFERENCES memories (id, member_id),
    CONSTRAINT ck_matches_distinct_memories CHECK (memory_a_id <> memory_b_id),
    CONSTRAINT ck_matches_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_matches_result
        CHECK ((status = 'COMPLETED'
                AND completed_at IS NOT NULL
                AND winner_memory_id IN (memory_a_id, memory_b_id))
            OR (status <> 'COMPLETED'
                AND completed_at IS NULL
                AND winner_memory_id IS NULL)),
    INDEX idx_matches_member_status (member_id, status, started_at DESC, id DESC),
    INDEX idx_matches_member_completed (member_id, completed_at DESC, id DESC)
);

CREATE TABLE titles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    min_score BIGINT NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_titles_code UNIQUE (code),
    CONSTRAINT uk_titles_display_order UNIQUE (display_order),
    CONSTRAINT ck_titles_min_score CHECK (min_score >= 0)
);

CREATE TABLE memory_rankings (
    memory_id BIGINT NOT NULL,
    title_score BIGINT NOT NULL DEFAULT 0,
    current_title_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (memory_id),
    CONSTRAINT fk_memory_rankings_memory FOREIGN KEY (memory_id) REFERENCES memories (id),
    CONSTRAINT fk_memory_rankings_title FOREIGN KEY (current_title_id) REFERENCES titles (id),
    CONSTRAINT ck_memory_rankings_title_score CHECK (title_score >= 0),
    INDEX idx_memory_rankings_title (title_score DESC, memory_id ASC)
);

CREATE TABLE ranking_score_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_key VARCHAR(100) NOT NULL,
    memory_id BIGINT NOT NULL,
    score_type VARCHAR(20) NOT NULL,
    delta BIGINT NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ranking_score_events_key UNIQUE (event_key),
    CONSTRAINT fk_ranking_score_events_memory FOREIGN KEY (memory_id) REFERENCES memories (id),
    CONSTRAINT ck_ranking_score_events_type CHECK (score_type = 'TITLE'),
    CONSTRAINT ck_ranking_score_events_delta CHECK (delta <> 0),
    INDEX idx_ranking_score_events_memory (memory_id, occurred_at, id),
    INDEX idx_ranking_score_events_source (source_type, source_id)
);
