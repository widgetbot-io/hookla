ALTER TABLE provider_settings
    ADD COLUMN optionsId uuid
        CONSTRAINT provider_settings_embed_options_id_fk
            REFERENCES embed_options(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE