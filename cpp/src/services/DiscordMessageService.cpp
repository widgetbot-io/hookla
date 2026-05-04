#include "DiscordMessageService.h"
#include <httplib.h>
#include <nlohmann/json.hpp>
#include <spdlog/spdlog.h>

DiscordMessageService::DiscordMessageService() = default;

void DiscordMessageService::sendMessageToDiscord(const DiscordWebhook& hook, const DiscordEmbed& embed) {
    nlohmann::json payload = {{"embeds", nlohmann::json::array({embedToJson(embed)})}};
    const std::string body = payload.dump();
    const std::string path = "/api/webhooks/" + hook.discordWebhookId + "/" + hook.discordWebhookToken;

    spdlog::debug("Sending to Discord: {}", body);

    httplib::SSLClient client("discord.com");
    client.set_connection_timeout(5);
    client.set_read_timeout(10);

    auto res = client.Post(path, body, "application/json");

    if (!res)
        spdlog::error("Discord request failed: no response");
    else if (res->status < 200 || res->status >= 300)
        spdlog::error("Discord returned HTTP {}: {}", res->status, res->body);
    else
        spdlog::debug("Discord webhook sent successfully (HTTP {})", res->status);
}
