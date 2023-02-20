ALTER TABLE users RENAME COLUMN discordId to discord_id;
ALTER TABLE users RENAME COLUMN createdAt to created_at;

ALTER TABLE discord_webhooks RENAME COLUMN userId to user_id;
ALTER TABLE discord_webhooks RENAME COLUMN discordWebhookId to discord_webhook_id;
ALTER TABLE discord_webhooks RENAME COLUMN discordWebhookToken to discord_webhook_token;
ALTER TABLE discord_webhooks RENAME COLUMN createdAt to created_at;

ALTER TABLE provider_settings RENAME COLUMN userId to user_id;
ALTER TABLE provider_settings RENAME COLUMN discordWebhookId to discord_webhook_id;
ALTER TABLE provider_settings RENAME COLUMN optionsId to options_id;
ALTER TABLE provider_settings RENAME COLUMN createdAt to created_at;

ALTER TABLE embed_options RENAME COLUMN userId to user_id;
ALTER TABLE embed_options RENAME COLUMN descriptionFormat to description_format;
ALTER TABLE embed_options RENAME COLUMN createdAt to created_at;
ALTER TABLE embed_options RENAME COLUMN privateMessage to private_message;
ALTER TABLE embed_options RENAME COLUMN privateCharacter to private_character;
ALTER TABLE embed_options RENAME COLUMN areCommitsClickable to are_commits_clickable;