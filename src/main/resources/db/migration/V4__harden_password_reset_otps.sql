ALTER TABLE otps
    ADD COLUMN last_sent_at DATETIME(6) NULL,
    ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0;

UPDATE otps
SET last_sent_at = expires_at;

ALTER TABLE otps
    MODIFY COLUMN last_sent_at DATETIME(6) NOT NULL;

CREATE INDEX idx_otps_email_expires_at ON otps (email, expires_at);
