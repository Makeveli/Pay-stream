CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE idempotency_keys (
  idempotency_key VARCHAR(255) PRIMARY KEY,
  payment_id      VARCHAR(255) NOT NULL,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Index to periodically clean up old keys if needed
CREATE INDEX idx_idempotency_created_at ON idempotency_keys(created_at);
