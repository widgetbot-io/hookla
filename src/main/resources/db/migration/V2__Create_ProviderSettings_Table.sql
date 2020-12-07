CREATE TABLE IF NOT EXISTS provider_settings (
    id uuid PRIMARY KEY,

    userId uuid NOT NULL
        CONSTRAINT provider_settings_users_id_fk
            REFERENCES users(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    discordWebhookId uuid NOT NULL
        CONSTRAINT provider_settings_discord_webhooks_id_fk
            REFERENCES discord_webhooks(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    token VARCHAR(32) UNIQUE NOT NULL,
    createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_provider_settings_id ON provider_settings(id);
CREATE INDEX idx_provider_settings_token ON provider_settings(token);