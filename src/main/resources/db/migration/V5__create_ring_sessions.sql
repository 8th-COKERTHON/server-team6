DROP TABLE balance_question_responses;
DROP TABLE balance_question_options;
DROP TABLE balance_questions;

ALTER TABLE matching_events
    ADD COLUMN round_count INT NOT NULL DEFAULT 5,
    ADD CONSTRAINT ck_matching_events_round_count CHECK (round_count BETWEEN 1 AND 20);

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
    CONSTRAINT ck_ring_sessions_rounds CHECK (
        total_rounds > 0 AND completed_rounds BETWEEN 0 AND total_rounds),
    CONSTRAINT ck_ring_sessions_completed CHECK (
        (status = 'COMPLETED' AND completed_rounds = total_rounds AND completed_at IS NOT NULL)
        OR (status = 'IN_PROGRESS' AND completed_rounds < total_rounds AND completed_at IS NULL)),
    INDEX idx_ring_sessions_member_status (member_id, status, started_at DESC, id DESC)
);

ALTER TABLE matches
    ADD COLUMN session_id BIGINT NULL,
    ADD COLUMN round_no INT NULL,
    ADD CONSTRAINT fk_matches_session FOREIGN KEY (session_id) REFERENCES ring_sessions (id),
    ADD CONSTRAINT uk_matches_session_round UNIQUE (session_id, round_no),
    ADD CONSTRAINT ck_matches_round_no CHECK (round_no IS NULL OR round_no > 0),
    ADD INDEX idx_matches_session_status (session_id, status, round_no);
