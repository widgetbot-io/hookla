CREATE TABLE IF NOT EXISTS discord_webhooks (
    id uuid PRIMARY KEY,

    userId uuid NOT NULL
        CONSTRAINT discord_webhooks_users_id_fk
            REFERENCES users(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    discordWebhookId TEXT NOT NULL,
    discordWebhookToken TEXT NOT NULL,

    createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_discord_webhooks_id ON discord_webhooks(id);