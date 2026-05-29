#include <cstdlib>
#include <memory>
#include <string>
#include <drogon/drogon.h>
#include <spdlog/spdlog.h>
#include "controllers/WebhookController.h"
#include "db/ConnectionPool.h"
#include "handlers/MainHandler.h"
#include "handlers/github/GithubHandler.h"
#include "handlers/gitlab/GitlabHandler.h"
#include "handlers/sonarr/SonarrHandler.h"
#include "handlers/radarr/RadarrHandler.h"
#include "handlers/ombi/OmbiHandler.h"
#include "services/DiscordMessageService.h"
#include "services/DiscordWebhookService.h"
#include "services/EmbedOptionsService.h"
#include "services/ProviderSettingsService.h"

static std::string getenv_or(const char* key, const char* fallback) {
    const char* val = std::getenv(key);
    return val ? val : fallback;
}

int main() {
    spdlog::set_level(spdlog::level::debug);

    // Build PostgreSQL connection string from environment variables.
    // Mirrors the HOCON "postgres" block in application.conf.
    const std::string connStr =
        "host="     + getenv_or("DB_HOST",     "localhost") +
        " port="    + getenv_or("DB_PORT",     "5432")      +
        " dbname="  + getenv_or("DB_NAME",     "hookla")    +
        " user="    + getenv_or("DB_USER",     "hookla")    +
        " password="+ getenv_or("DB_PASSWORD", "")          +
        " sslmode=" + getenv_or("DB_SSLMODE",  "disable");

    const int    port      = std::stoi(getenv_or("APP_PORT", "8080"));
    const size_t poolSize  = std::stoul(getenv_or("DB_POOL_SIZE", "10"));

    // ---- Construct object graph (replaces HooklaModules + MacWire) --------

    auto pool              = std::make_shared<ConnectionPool>(connStr, poolSize);
    auto embedOptsSvc      = std::make_shared<EmbedOptionsService>(pool);
    auto providerSettsSvc  = std::make_shared<ProviderSettingsService>(pool, embedOptsSvc);
    auto discordWebhookSvc = std::make_shared<DiscordWebhookService>(pool);
    auto discordMsgSvc     = std::make_shared<DiscordMessageService>();

    auto mainHandler = std::make_shared<MainHandler>();
    mainHandler->registerHandler("github", std::make_shared<GithubHandler>(discordMsgSvc));
    mainHandler->registerHandler("gitlab", std::make_shared<GitlabHandler>(discordMsgSvc));
    mainHandler->registerHandler("sonarr", std::make_shared<SonarrHandler>(discordMsgSvc));
    mainHandler->registerHandler("radarr", std::make_shared<RadarrHandler>(discordMsgSvc));
    mainHandler->registerHandler("ombi",   std::make_shared<OmbiHandler>(discordMsgSvc));

    auto controller = std::make_shared<WebhookController>(
        providerSettsSvc, discordWebhookSvc, mainHandler);

    // ---- Start Drogon HTTP server -----------------------------------------

    drogon::app()
        .addListener("0.0.0.0", port)
        .setThreadNum(static_cast<size_t>(std::thread::hardware_concurrency()))
        .registerController(controller)
        .run();

    return 0;
}
