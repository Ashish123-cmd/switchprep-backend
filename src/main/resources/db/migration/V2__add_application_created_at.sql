ALTER TABLE applications
    ADD COLUMN created_at TIMESTAMPTZ;

UPDATE applications
SET created_at = updated_at
WHERE created_at IS NULL;

ALTER TABLE applications
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at SET NOT NULL;

CREATE INDEX idx_applications_user_mode_created_at
    ON applications (user_id, application_mode, created_at);