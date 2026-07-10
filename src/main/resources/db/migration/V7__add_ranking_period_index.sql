ALTER TABLE ranking_score_events
    ADD INDEX idx_ranking_score_events_period_episode
        (score_type, occurred_at, episode_id),
    ALGORITHM=INPLACE,
    LOCK=NONE;
