ALTER TABLE episode_rankings
    ALTER COLUMN title_score SET DEFAULT 1000,
    DROP CHECK ck_episode_rankings_title_score,
    ADD CONSTRAINT ck_episode_rankings_title_score CHECK (title_score >= 100);
