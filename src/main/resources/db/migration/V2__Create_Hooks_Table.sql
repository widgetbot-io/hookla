CREATE TABLE hooks
(
    id         uuid
        CONSTRAINT hooks_pkey PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id    uuid         NOT NULL
        CONSTRAINT hooks_team_id_fk
            REFERENCES teams (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    -- This in the hookla codebase, not a database table
    source_id  VARCHAR(128) NOT NULL,

    created_at timestamp    NOT NULL      DEFAULT NOW(),
    updated_at timestamp    NOT NULL      DEFAULT NOW()
)