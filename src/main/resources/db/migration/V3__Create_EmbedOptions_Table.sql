CREATE TABLE IF NOT EXISTS embed_options(
    id uuid PRIMARY KEY,

    userId uuid NOT NULL
        CONSTRAINT embed_options_id_fk
            REFERENCES users(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    descriptionFormat text,

    createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_embed_options_id ON embed_options(id);