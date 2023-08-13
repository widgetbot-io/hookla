CREATE TABLE hook_sink_events
(
    hook_sink_id     uuid         NOT NULL
        CONSTRAINT hook_sink_events_hook_sink_id_fkey REFERENCES hook_sinks (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
            NOT DEFERRABLE,
    hook_settings_id uuid         NULL     DEFAULT NULL
        CONSTRAINT hook_sink_events_hook_settings_id_fkey REFERENCES hook_settings (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    event_id         VARCHAR(128) NOT NULL,
    created_at       timestamp    NOT NULL DEFAULT NOW(),
    updated_at       timestamp    NOT NULL DEFAULT NOW()

)