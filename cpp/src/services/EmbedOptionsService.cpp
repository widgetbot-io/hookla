#include "EmbedOptionsService.h"
#include <pqxx/pqxx>
#include <spdlog/spdlog.h>

EmbedOptionsService::EmbedOptionsService(std::shared_ptr<ConnectionPool> pool)
    : pool_(std::move(pool)) {}

std::optional<EmbedOptions> EmbedOptionsService::getById(const std::string& id) {
    auto conn = pool_->acquire();
    try {
        pqxx::work txn(*conn);
        auto rows = txn.exec_params(
            "SELECT id, user_id, are_commits_clickable, show_private_commits, "
            "private_commit_prefix, description_format, private_message, private_character "
            "FROM embed_options WHERE id = $1",
            id);
        txn.commit();
        pool_->release(std::move(conn));

        if (rows.empty()) return std::nullopt;

        const auto& r = rows[0];
        EmbedOptions opts;
        opts.id                  = r["id"].as<std::string>();
        opts.userId              = r["user_id"].as<std::string>();
        opts.areCommitsClickable = r["are_commits_clickable"].as<bool>();
        opts.showPrivateCommits  = r["show_private_commits"].as<bool>();
        if (!r["private_commit_prefix"].is_null())
            opts.privateCommitPrefix = r["private_commit_prefix"].as<std::string>();
        if (!r["description_format"].is_null())
            opts.descriptionFormat = r["description_format"].as<std::string>();
        if (!r["private_message"].is_null())
            opts.privateMessage = r["private_message"].as<std::string>();
        if (!r["private_character"].is_null())
            opts.privateCharacter = r["private_character"].as<std::string>();
        return opts;
    } catch (const std::exception& e) {
        pool_->release(std::move(conn));
        spdlog::error("EmbedOptionsService::getById error: {}", e.what());
        return std::nullopt;
    }
}
