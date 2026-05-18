# hookla

`hookla` is WidgetBot's webhook relay service. It receives webhooks from
third-party providers (GitHub, GitLab, Sonarr, and others), converts the
payloads into formatted Discord embeds, and posts them to a configured Discord
webhook.

## What it does

Each provider integration has a stored `ProviderSettings` row keyed by a unique
token. An external provider sends its webhook to `/process/<token>`; hookla
looks up the settings, identifies the provider, decodes the event payload, and
dispatches it to the matching handler. The handler builds a Discord embed and
sends it to the linked Discord webhook.

Supported inbound providers:

- GitHub (push, issue, check run, create, delete events)
- GitLab (push, issue, job, note, tag events)
- Sonarr (grab, download, rename, test events)
- Radarr and Ombi (handlers present)

## Architecture

- **Language / runtime**: Scala 2.13, runs on the JVM (Java 11).
- **HTTP layer**: [Finch](https://github.com/finagle/finch) endpoints served by
  Finagle's `Http.server` ([twitter-server](https://github.com/twitter/twitter-server)).
- **Dependency injection**: [MacWire](https://github.com/softwaremill/macwire)
  compile-time wiring (see `HooklaModules`).
- **Database**: PostgreSQL, accessed with [Quill](https://getquill.io/)
  (`quill-async-postgres`). Schema migrations are managed by
  [Flyway](https://flywaydb.org/) and applied automatically on startup.
- **JSON**: [circe](https://circe.github.io/circe/) for decoding inbound
  payloads and encoding Discord output.
- **Discord output**: embeds are built with [AckCord](https://github.com/Katrix/AckCord)
  data types and posted to `discord.com/api/webhooks/...`.

### Request flow

```
provider --> POST /process/{token} --> WebhookController
          --> ProviderSettingsService (token lookup)
          --> decode payload (circe)
          --> MainHandler --> provider handler --> Discord embed
          --> DiscordMessageService --> Discord webhook
```

`GET /process/{token}` returns the stored provider settings for a token.

## Key directories

- `src/main/scala/venix/hookla/` — application root (`App`, `HooklaConfig`, `HooklaModules`).
- `src/main/scala/venix/hookla/controllers/` — Finch HTTP endpoints (`WebhookController`).
- `src/main/scala/venix/hookla/handlers/` — per-provider event handlers (`github/`, `gitlab/`, `sonarr/`, `radarr/`, `ombi/`).
- `src/main/scala/venix/hookla/types/` — payload models and provider definitions.
- `src/main/scala/venix/hookla/services/` — database and Discord services.
- `src/main/scala/venix/hookla/models/` — Quill-mapped database models.
- `src/main/resources/db/migration/` — Flyway SQL migrations.
- `src/main/resources/application.conf` — HOCON configuration.

## Configuration

Configuration is loaded from `application.conf` via `circe-config`. It is driven
by environment variables:

- `ENV` — environment name (defaults to `development`).
- `INTERNAL_PORT` — HTTP listen port (defaults to `8443`).
- `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USER`, `DB_PASSWORD` — PostgreSQL
  connection details (used for both the Quill context and Flyway, with
  `sslmode=require`).

## Running locally

Requires a JDK (11) and sbt. A reachable PostgreSQL database is needed; Flyway
migrations run automatically on boot.

```sh
# set the DB_* env vars first
sbt run
```

The server logs `Server started on port <port>` once ready.

## Deployment

`hookla` ships as a Docker image. The GitHub Actions workflow
(`.github/workflows/scala.yml`) runs `sbt clean compile docker:stage` and builds
/ pushes the image to GitHub Container Registry on pushes to `master` and on
tags.

- Image: `ghcr.io/widgetbot-io/hookla`
- Base image: `gcr.io/distroless/java:11`
- Exposed port: `8443`
- Tags: `latest` on `v*` tags, `staging` on `master`, plus branch / PR / SHA /
  semver tags.
