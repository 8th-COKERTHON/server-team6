ALTER TABLE members
    ADD COLUMN onboarding_status VARCHAR(30) NULL AFTER role;

UPDATE members
SET onboarding_status = CASE
    WHEN onboarding_completed_at IS NOT NULL THEN 'COMPLETED'
    ELSE 'NOT_STARTED'
END;

ALTER TABLE members
    MODIFY COLUMN onboarding_status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    ADD CONSTRAINT ck_members_onboarding_status CHECK (
        onboarding_status IN ('NOT_STARTED', 'EPISODE_REGISTERING', 'PLACEMENT_IN_PROGRESS', 'COMPLETED')
    ),
    ADD CONSTRAINT ck_members_onboarding_completed CHECK (
        (onboarding_status = 'COMPLETED' AND onboarding_completed_at IS NOT NULL)
        OR (onboarding_status <> 'COMPLETED' AND onboarding_completed_at IS NULL)
    );
