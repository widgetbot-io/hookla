#include "DiscordWebhookService.h"
#include <pqxx/pqxx>
#include <spdlog/spdlog.h>

DiscordWebhookService::DiscordWebhookService(std::shared_ptr<ConnectionPool> pool)
    : pool_(std::move(pool)) {}

std::optional<DiscordWebhook> DiscordWebhookService::getById(const std::string& id) {
    auto conn = pool_->acquire();
    try {
        pqxx::work txn(*conn);
        auto rows = txn.exec_params(
            "SELECT id, user_id, discord_webhook_id, discord_webhook_token "
            "FROM discord_webhooks WHERE id = $1",
            id);
        txn.commit();
        pool_->release(std::move(conn));

        if (rows.empty()) return std::nullopt;

        const auto& r = rows[0];
        DiscordWebhook hook;
        hook.id                   = r["id"].as<std::string>();
        hook.userId               = r["user_id"].as<std::string>();
        hook.discordWebhookId     = r["discord_webhook_id"].as<std::string>();
        hook.discordWebhookToken  = r["discord_webhook_token"].as<std::string>();
        return hook;
    } catch (const std::exception& e) {
        pool_->release(std::move(conn));
        spdlog::error("DiscordWebhookService::getById error: {}", e.what());
        return std::nullopt;
    }
}
