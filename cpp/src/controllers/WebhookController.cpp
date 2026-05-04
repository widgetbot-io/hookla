#include "WebhookController.h"
#include <algorithm>
#include <nlohmann/json.hpp>
#include <spdlog/spdlog.h>

WebhookController::WebhookController(
    std::shared_ptr<ProviderSettingsService> providerSettings,
    std::shared_ptr<DiscordWebhookService>   discordWebhooks,
    std::shared_ptr<MainHandler>             mainHandler)
    : providerSettings_(std::move(providerSettings))
    , discordWebhooks_(std::move(discordWebhooks))
    , mainHandler_(std::move(mainHandler)) {}

// GET /process/:token — returns raw provider settings as JSON (debug endpoint)
drogon::Task<drogon::HttpResponsePtr> WebhookController::getHookInfo(
    drogon::HttpRequestPtr req,
    std::string token)
{
    auto settings = providerSettings_->getByToken(token);
    if (!settings) {
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k401Unauthorized);
        resp->setBody("invalid token");
        co_return resp;
    }

    nlohmann::json j;
    j["id"]               = settings->id;
    j["slug"]             = settings->slug;
    j["discordWebhookId"] = settings->discordWebhookId;
    if (settings->optionsId) j["optionsId"] = *settings->optionsId;

    auto resp = drogon::HttpResponse::newHttpResponse();
    resp->setContentTypeCode(drogon::CT_APPLICATION_JSON);
    resp->setBody(j.dump());
    co_return resp;
}

// POST /process/:token — receive and forward a webhook
drogon::Task<drogon::HttpResponsePtr> WebhookController::process(
    drogon::HttpRequestPtr req,
    std::string token)
{
    // Normalise headers to lowercase (mirrors the Scala controller)
    std::unordered_map<std::string, std::string> headers;
    for (auto& [k, v] : req->getHeaders()) {
        std::string lk = k;
        std::transform(lk.begin(), lk.end(), lk.begin(), ::tolower);
        headers[lk] = v;
    }

    auto settings = providerSettings_->getByToken(token);
    if (!settings) {
        spdlog::warn("Invalid token: {}", token);
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k401Unauthorized);
        resp->setBody("invalid token");
        co_return resp;
    }

    spdlog::debug("Received webhook for provider: {}", settings->slug);

    nlohmann::json body = nlohmann::json::parse(req->getBody(), nullptr, false);
    if (body.is_discarded()) {
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k400BadRequest);
        resp->setBody("invalid JSON body");
        co_return resp;
    }

    auto hook = discordWebhooks_->getById(settings->discordWebhookId);
    if (!hook) {
        spdlog::error("Discord webhook not found for id: {}", settings->discordWebhookId);
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k500InternalServerError);
        resp->setBody("discord webhook not configured");
        co_return resp;
    }

    auto options = providerSettings_->getOptionsForProvider(*settings);
    EventData eventData{*hook, options};

    try {
        mainHandler_->handle(settings->slug, body, headers, eventData);
    } catch (const std::exception& e) {
        spdlog::error("Handler error for {}: {}", settings->slug, e.what());
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k400BadRequest);
        resp->setBody(e.what());
        co_return resp;
    }

    auto resp = drogon::HttpResponse::newHttpResponse();
    resp->setStatusCode(drogon::k200OK);
    resp->setBody("success");
    co_return resp;
}
