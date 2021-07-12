ALTER TABLE users ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE discord_webhooks ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE provider_settings ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE embed_options ALTER COLUMN id SET DEFAULT uuid_generate_v4();