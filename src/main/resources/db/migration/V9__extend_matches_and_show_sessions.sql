ALTER TABLE matching_events
    ADD COLUMN period_key VARCHAR(20) NULL AFTER event_type,
    ADD COLUMN match_type VARCHAR(30) NOT NULL DEFAULT 'RIVAL' AFTER period_key,
    ADD COLUMN score_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00 AFTER score_reward,
    ADD CONSTRAINT uk_matching_events_type_period UNIQUE (event_type, period_key);

ALTER TABLE ring_sessions
    DROP FOREIGN KEY fk_ring_sessions_event,
    DROP CHECK ck_ring_sessions_status,
    MODIFY COLUMN event_id BIGINT NULL,
    ADD COLUMN session_type VARCHAR(30) NOT NULL DEFAULT 'RIVAL' AFTER member_id,
    ADD COLUMN primary_episode_id BIGINT NULL AFTER session_type,
    ADD COLUMN score_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00 AFTER completed_rounds,
    ADD CONSTRAINT fk_ring_sessions_event FOREIGN KEY (event_id) REFERENCES matching_events (id),
    ADD CONSTRAINT fk_ring_sessions_primary_episode FOREIGN KEY (primary_episode_id) REFERENCES episodes (id),
    ADD CONSTRAINT ck_ring_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED'));

ALTER TABLE matches
    ADD COLUMN loser_episode_id BIGINT NULL AFTER winner_episode_id,
    ADD COLUMN match_type VARCHAR(30) NOT NULL DEFAULT 'GENERAL' AFTER loser_episode_id,
    ADD COLUMN match_order INT NULL AFTER match_type,
    ADD COLUMN episode_a_score_before BIGINT NULL AFTER match_order,
    ADD COLUMN episode_b_score_before BIGINT NULL AFTER episode_a_score_before,
    ADD COLUMN episode_a_score_after BIGINT NULL AFTER episode_b_score_before,
    ADD COLUMN episode_b_score_after BIGINT NULL AFTER episode_a_score_after,
    ADD COLUMN winner_delta BIGINT NULL AFTER episode_b_score_after,
    ADD COLUMN loser_delta BIGINT NULL AFTER winner_delta,
    ADD CONSTRAINT fk_matches_loser_episode_owner
        FOREIGN KEY (loser_episode_id, member_id) REFERENCES episodes (id, member_id),
    ADD CONSTRAINT ck_matches_loser_episode CHECK (
        loser_episode_id IS NULL OR loser_episode_id IN (episode_a_id, episode_b_id)
    ),
    ADD CONSTRAINT ck_matches_match_order CHECK (match_order IS NULL OR match_order > 0),
    ADD CONSTRAINT uk_matches_session_match_order UNIQUE (session_id, match_order);

ALTER TABLE episodes
    ADD COLUMN placement_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' AFTER status,
    ADD CONSTRAINT ck_episodes_placement_status CHECK (
        placement_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')
    );

ALTER TABLE episodes ALTER COLUMN placement_status SET DEFAULT 'PENDING';
