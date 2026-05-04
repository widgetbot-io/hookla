#include "ProviderSettingsService.h"
#include <pqxx/pqxx>
#include <spdlog/spdlog.h>

ProviderSettingsService::ProviderSettingsService(
    std::shared_ptr<ConnectionPool> pool,
    std::shared_ptr<EmbedOptionsService> embedOptionsService)
    : pool_(std::move(pool))
    , embedOptionsService_(std::move(embedOptionsService)) {}

std::optional<ProviderSettings> ProviderSettingsService::getById(const std::string& id) {
    return fetchWhere("id", id);
}

std::optional<ProviderSettings> ProviderSettingsService::getByToken(const std::string& token) {
    return fetchWhere("token", token);
}

std::optional<EmbedOptions> ProviderSettingsService::getOptionsForProvider(const ProviderSettings& ps) {
    if (!ps.optionsId) return std::nullopt;
    return embedOptionsService_->getById(*ps.optionsId);
}

std::optional<ProviderSettings> ProviderSettingsService::fetchWhere(
    const std::string& col, const std::string& val)
{
    auto conn = pool_->acquire();
    try {
        pqxx::work txn(*conn);
        // pqxx doesn't parameterise column names; col is internal so direct interpolation is safe
        auto rows = txn.exec_params(
            "SELECT id, user_id, discord_webhook_id, options_id, slug, token "
            "FROM provider_settings WHERE " + col + " = $1",
            val);
        txn.commit();
        pool_->release(std::move(conn));

        if (rows.empty()) return std::nullopt;

        const auto& r = rows[0];
        ProviderSettings ps;
        ps.id               = r["id"].as<std::string>();
        ps.userId           = r["user_id"].as<std::string>();
        ps.discordWebhookId = r["discord_webhook_id"].as<std::string>();
        if (!r["options_id"].is_null())
            ps.optionsId = r["options_id"].as<std::string>();
        ps.slug  = r["slug"].as<std::string>();
        ps.token = r["token"].as<std::string>();
        return ps;
    } catch (const std::exception& e) {
        pool_->release(std::move(conn));
        spdlog::error("ProviderSettingsService error: {}", e.what());
        return std::nullopt;
    }
}
