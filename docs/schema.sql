-- Team 6 MVP target schema for MySQL 8.4 and ERD import.
-- A match compares two episodes owned by the same authenticated member.
-- Photo storage, community features, member-to-member matching, and connection ranking are excluded.
-- Current schema after Flyway V6. V2 retains the historical pre-rename identifiers.

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    onboarding_completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_members_email UNIQUE (email),
    INDEX idx_members_name_id (name, id)
);

CREATE TABLE episodes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    episode_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    matched_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_episodes_id_member UNIQUE (id, member_id),
    CONSTRAINT fk_episodes_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT ck_episodes_status CHECK (status IN ('AVAILABLE', 'MATCHED', 'ARCHIVED')),
    CONSTRAINT ck_episodes_matched_at
        CHECK ((status = 'MATCHED' AND matched_at IS NOT NULL) OR status <> 'MATCHED'),
    INDEX idx_episodes_member_status (member_id, status, episode_date DESC, id DESC)
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

CREATE TABLE matching_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    score_reward BIGINT NOT NULL DEFAULT 0,
    round_count INT NOT NULL DEFAULT 5,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_matching_events_type CHECK (event_type IN ('WEEKLY', 'MONTHLY', 'SPECIAL')),
    CONSTRAINT ck_matching_events_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'OPEN', 'CLOSED', 'CANCELLED')),
    CONSTRAINT ck_matching_events_period CHECK (ends_at > starts_at),
    CONSTRAINT ck_matching_events_score CHECK (score_reward >= 0),
    CONSTRAINT ck_matching_events_round_count CHECK (round_count BETWEEN 1 AND 20),
    INDEX idx_matching_events_upcoming (status, starts_at, id)
);

CREATE TABLE ring_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_rounds INT NOT NULL,
    completed_rounds INT NOT NULL DEFAULT 0,
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ring_sessions_event_member UNIQUE (event_id, member_id),
    CONSTRAINT fk_ring_sessions_event FOREIGN KEY (event_id) REFERENCES matching_events (id),
    CONSTRAINT fk_ring_sessions_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT ck_ring_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_ring_sessions_rounds CHECK (total_rounds > 0 AND completed_rounds BETWEEN 0 AND total_rounds),
    CONSTRAINT ck_ring_sessions_completed CHECK (
        (status = 'COMPLETED' AND completed_rounds = total_rounds AND completed_at IS NOT NULL)
        OR (status = 'IN_PROGRESS' AND completed_rounds < total_rounds AND completed_at IS NULL)),
    INDEX idx_ring_sessions_member_status (member_id, status, started_at DESC, id DESC)
);

CREATE TABLE matches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NULL,
    member_id BIGINT NOT NULL,
    episode_a_id BIGINT NOT NULL,
    episode_b_id BIGINT NOT NULL,
    winner_episode_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    session_id BIGINT NULL,
    round_no INT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_matches_event FOREIGN KEY (event_id) REFERENCES matching_events (id),
    CONSTRAINT fk_matches_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_matches_session FOREIGN KEY (session_id) REFERENCES ring_sessions (id),
    CONSTRAINT fk_matches_episode_a_owner
        FOREIGN KEY (episode_a_id, member_id) REFERENCES episodes (id, member_id),
    CONSTRAINT fk_matches_episode_b_owner
        FOREIGN KEY (episode_b_id, member_id) REFERENCES episodes (id, member_id),
    CONSTRAINT fk_matches_winner_episode_owner
        FOREIGN KEY (winner_episode_id, member_id) REFERENCES episodes (id, member_id),
    CONSTRAINT ck_matches_distinct_episodes CHECK (episode_a_id <> episode_b_id),
    CONSTRAINT uk_matches_session_round UNIQUE (session_id, round_no),
    CONSTRAINT ck_matches_round_no CHECK (round_no IS NULL OR round_no > 0),
    CONSTRAINT ck_matches_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_matches_result
        CHECK ((status = 'COMPLETED'
                AND completed_at IS NOT NULL
                AND winner_episode_id IN (episode_a_id, episode_b_id))
            OR (status <> 'COMPLETED'
                AND completed_at IS NULL
                AND winner_episode_id IS NULL)),
    INDEX idx_matches_member_status (member_id, status, started_at DESC, id DESC),
    INDEX idx_matches_member_completed (member_id, completed_at DESC, id DESC),
    INDEX idx_matches_session_status (session_id, status, round_no)
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

CREATE TABLE episode_rankings (
    episode_id BIGINT NOT NULL,
    title_score BIGINT NOT NULL DEFAULT 0,
    current_title_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (episode_id),
    CONSTRAINT fk_episode_rankings_episode FOREIGN KEY (episode_id) REFERENCES episodes (id),
    CONSTRAINT fk_episode_rankings_title FOREIGN KEY (current_title_id) REFERENCES titles (id),
    CONSTRAINT ck_episode_rankings_title_score CHECK (title_score >= 0),
    INDEX idx_episode_rankings_title (title_score DESC, episode_id ASC)
);

CREATE TABLE ranking_score_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_key VARCHAR(100) NOT NULL,
    episode_id BIGINT NOT NULL,
    score_type VARCHAR(20) NOT NULL,
    delta BIGINT NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ranking_score_events_key UNIQUE (event_key),
    CONSTRAINT fk_ranking_score_events_episode FOREIGN KEY (episode_id) REFERENCES episodes (id),
    CONSTRAINT ck_ranking_score_events_type CHECK (score_type = 'TITLE'),
    CONSTRAINT ck_ranking_score_events_delta CHECK (delta <> 0),
    INDEX idx_ranking_score_events_episode (episode_id, occurred_at, id),
    INDEX idx_ranking_score_events_source (source_type, source_id)
);
