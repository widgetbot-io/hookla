CREATE TABLE hook_settings
(
    id         uuid PRIMARY KEY   DEFAULT uuid_generate_v4(),
    name       text      NOT NULL DEFAULT 'Untitled',
    "default"  boolean   NOT NULL DEFAULT false,
    hook_id    uuid      NOT NULL
        CONSTRAINT hook_settings_hook_id_fk REFERENCES hooks (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    created_at timestamp NOT NULL DEFAULT NOW(),
    updated_at timestamp NOT NULL DEFAULT NOW()
)