
-- Add an optional assigned nurse to existing surgery records.
ALTER TABLE surgeries
ADD COLUMN assigned_nurse_id BIGINT;

-- Link the assigned nurse to an existing user account.
ALTER TABLE surgeries
ADD CONSTRAINT fk_surgeries_assigned_nurse
FOREIGN KEY (assigned_nurse_id) REFERENCES users (id);

-- Improve nurse availability conflict lookups.
CREATE INDEX idx_surgeries_nurse_time
ON surgeries (assigned_nurse_id, start_time, end_time);
