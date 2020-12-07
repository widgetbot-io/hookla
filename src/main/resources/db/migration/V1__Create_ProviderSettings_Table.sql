CREATE TABLE IF NOT EXISTS provider_settings (
    id uuid PRIMARY KEY,
    userId VARCHAR(64) NOT NULL
        CONSTRAINT provider_settings_users_id_fk
            REFERENCES users(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    discordWebhookUrl TEXT NOT NULL,

    createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()
);