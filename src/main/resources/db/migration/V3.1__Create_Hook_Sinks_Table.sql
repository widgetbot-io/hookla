CREATE TABLE hook_sinks
(
    hook_id    uuid         NOT NULL
        CONSTRAINT hook_sinks_hook_id_fkey REFERENCES hooks (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    -- This in the hookla codebase, not a database table
    sink_id    VARCHAR(128) NOT NULL,

    created_at timestamp    NOT NULL DEFAULT NOW(),
    updated_at timestamp    NOT NULL DEFAULT NOW(),

    CONSTRAINT hook_sinks_pkey PRIMARY KEY (hook_id, sink_id)
)