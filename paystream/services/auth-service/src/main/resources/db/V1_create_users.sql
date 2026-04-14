-- File: src/main/resources/db/migration/V1__create_users.sql
 
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- enables gen_random_uuid()
 
CREATE TABLE users (
  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(60),                  -- bcrypt hash, null for OAuth users
  name          VARCHAR(255) NOT NULL,
  roles         TEXT[]       NOT NULL DEFAULT ARRAY['ROLE_USER'],
  enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
 
CREATE TABLE refresh_tokens (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash   VARCHAR(64) NOT NULL UNIQUE,
  expires_at   TIMESTAMPTZ NOT NULL,
  revoked      BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
 
-- Indexes for fast lookup
CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_rt_user_id        ON refresh_tokens(user_id);
CREATE INDEX idx_rt_token_hash     ON refresh_tokens(token_hash);


