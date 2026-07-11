ALTER TABLE matching_events DROP CHECK ck_matching_events_status;

ALTER TABLE matching_events
    ADD CONSTRAINT ck_matching_events_status
        CHECK (status IN ('DRAFT', 'SCHEDULED', 'OPEN', 'CLOSED', 'CANCELLED'));
