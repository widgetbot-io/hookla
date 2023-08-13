CREATE TABLE team_users
(
    team_id uuid NOT NULL
        CONSTRAINT team_users_team_id_fk
            REFERENCES teams (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    user_id uuid NOT NULL
        CONSTRAINT team_users_user_id_fk
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    created_at timestamp NOT NULL DEFAULT NOW(),
    updated_at timestamp NOT NULL DEFAULT NOW(),

    CONSTRAINT team_users_pkey PRIMARY KEY (team_id, user_id)
)

