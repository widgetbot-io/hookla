CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY,
    discordId VARCHAR(64) UNIQUE NOT NULL,

    createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_id ON users(id);
CREATE INDEX idx_users_discordId ON users(discordId);