RENAME TABLE memories TO episodes,
             memory_rankings TO episode_rankings;

ALTER TABLE episodes
    RENAME COLUMN memory_date TO episode_date,
    RENAME INDEX uk_memories_id_member TO uk_episodes_id_member,
    RENAME INDEX idx_memories_member_status TO idx_episodes_member_status,
    DROP FOREIGN KEY fk_memories_member,
    DROP CHECK ck_memories_status,
    DROP CHECK ck_memories_matched_at,
    ADD CONSTRAINT fk_episodes_member FOREIGN KEY (member_id) REFERENCES members (id),
    ADD CONSTRAINT ck_episodes_status CHECK (status IN ('AVAILABLE', 'MATCHED', 'ARCHIVED')),
    ADD CONSTRAINT ck_episodes_matched_at
        CHECK ((status = 'MATCHED' AND matched_at IS NOT NULL) OR status <> 'MATCHED');

ALTER TABLE matches
    RENAME COLUMN memory_a_id TO episode_a_id,
    RENAME COLUMN memory_b_id TO episode_b_id,
    RENAME COLUMN winner_memory_id TO winner_episode_id,
    DROP FOREIGN KEY fk_matches_memory_a_owner,
    DROP FOREIGN KEY fk_matches_memory_b_owner,
    DROP FOREIGN KEY fk_matches_winner_owner,
    DROP CHECK ck_matches_distinct_memories,
    DROP CHECK ck_matches_result,
    ADD CONSTRAINT fk_matches_episode_a_owner
        FOREIGN KEY (episode_a_id, member_id) REFERENCES episodes (id, member_id),
    ADD CONSTRAINT fk_matches_episode_b_owner
        FOREIGN KEY (episode_b_id, member_id) REFERENCES episodes (id, member_id),
    ADD CONSTRAINT fk_matches_winner_episode_owner
        FOREIGN KEY (winner_episode_id, member_id) REFERENCES episodes (id, member_id),
    ADD CONSTRAINT ck_matches_distinct_episodes CHECK (episode_a_id <> episode_b_id),
    ADD CONSTRAINT ck_matches_episode_result
        CHECK ((status = 'COMPLETED'
                AND completed_at IS NOT NULL
                AND winner_episode_id IN (episode_a_id, episode_b_id))
            OR (status <> 'COMPLETED'
                AND completed_at IS NULL
                AND winner_episode_id IS NULL));

ALTER TABLE episode_rankings
    RENAME COLUMN memory_id TO episode_id,
    RENAME INDEX idx_memory_rankings_title TO idx_episode_rankings_title,
    DROP FOREIGN KEY fk_memory_rankings_memory,
    DROP FOREIGN KEY fk_memory_rankings_title,
    DROP CHECK ck_memory_rankings_title_score,
    ADD CONSTRAINT fk_episode_rankings_episode FOREIGN KEY (episode_id) REFERENCES episodes (id),
    ADD CONSTRAINT fk_episode_rankings_title FOREIGN KEY (current_title_id) REFERENCES titles (id),
    ADD CONSTRAINT ck_episode_rankings_title_score CHECK (title_score >= 0);

ALTER TABLE ranking_score_events
    RENAME COLUMN memory_id TO episode_id,
    RENAME INDEX idx_ranking_score_events_memory TO idx_ranking_score_events_episode,
    DROP FOREIGN KEY fk_ranking_score_events_memory,
    ADD CONSTRAINT fk_ranking_score_events_episode FOREIGN KEY (episode_id) REFERENCES episodes (id);
