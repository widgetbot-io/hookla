CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    discord_id VARCHAR(64) UNIQUE NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_id ON users(id);
CREATE INDEX idx_users_discordId ON users(discord_id);