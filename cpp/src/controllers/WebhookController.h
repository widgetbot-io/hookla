#pragma once
#include <drogon/HttpController.h>
#include <memory>
#include "../handlers/MainHandler.h"
#include "../services/DiscordWebhookService.h"
#include "../services/ProviderSettingsService.h"

// Registered manually (false = not auto-created by Drogon) so we can inject deps.
class WebhookController : public drogon::HttpController<WebhookController, false> {
public:
    METHOD_LIST_BEGIN
        ADD_METHOD_TO(WebhookController::process,     "/process/{token}", drogon::Post);
        ADD_METHOD_TO(WebhookController::getHookInfo, "/process/{token}", drogon::Get);
    METHOD_LIST_END

    WebhookController(
        std::shared_ptr<ProviderSettingsService> providerSettings,
        std::shared_ptr<DiscordWebhookService>   discordWebhooks,
        std::shared_ptr<MainHandler>             mainHandler);

    drogon::Task<drogon::HttpResponsePtr> process(
        drogon::HttpRequestPtr req,
        std::string token);

    drogon::Task<drogon::HttpResponsePtr> getHookInfo(
        drogon::HttpRequestPtr req,
        std::string token);

private:
    std::shared_ptr<ProviderSettingsService> providerSettings_;
    std::shared_ptr<DiscordWebhookService>   discordWebhooks_;
    std::shared_ptr<MainHandler>             mainHandler_;
};
