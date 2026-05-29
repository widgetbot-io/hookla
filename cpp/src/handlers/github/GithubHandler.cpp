#include "GithubHandler.h"
#include <algorithm>
#include <map>
#include <spdlog/spdlog.h>
#include "../../types/DiscordEmbed.h"
#include "../../util/Colours.h"
#include "../../util/EventHandlerUtils.h"

GithubHandler::GithubHandler(std::shared_ptr<DiscordMessageService> discord)
    : discord_(std::move(discord))
{
    handlers_["push"]      = [this](const nlohmann::json& j, const EventData& d) { handlePush(j.get<GithubPushPayload>(), d); };
    handlers_["issues"]    = [this](const nlohmann::json& j, const EventData& d) { handleIssue(j.get<GithubIssuePayload>(), d); };
    handlers_["check_run"] = [this](const nlohmann::json& j, const EventData& d) { handleCheckRun(j.get<GithubCheckRunPayload>(), d); };
    handlers_["create"]    = [this](const nlohmann::json& j, const EventData& d) { handleCreate(j.get<GithubCreatePayload>(), d); };
    handlers_["delete"]    = [this](const nlohmann::json& j, const EventData& d) { handleDelete(j.get<GithubDeletePayload>(), d); };
}

void GithubHandler::handle(
    const std::string& eventName,
    const nlohmann::json& body,
    const EventData& data)
{
    auto it = handlers_.find(eventName);
    if (it == handlers_.end()) {
        spdlog::warn("Unhandled GitHub event: {}", eventName);
        return;
    }
    it->second(body, data);
}

// ---- Push -----------------------------------------------------------------

void GithubHandler::handlePush(const GithubPushPayload& p, const EventData& d) {
    const std::string branchName = getBranchFromRef(p.ref);

    // Group commits by author email (preserves Scala groupBy behaviour)
    std::map<std::string, std::vector<const GithubCommit*>> byAuthor;
    for (auto& c : p.commits)
        byAuthor[c.author.email].push_back(&c);

    if (byAuthor.size() == 1) {
        auto& commits = byAuthor.begin()->second;
        std::string description;
        for (auto* c : commits)
            description += formatCommit(c->message, commits.size(), c->url, d.options) + "\n";

        DiscordEmbed embed;
        embed.description = description;
        embed.author      = EmbedAuthor{p.pusher.name, std::nullopt, p.sender.avatar_url};
        embed.url         = p.repository.html_url;
        embed.color       = Colours::PUSH;
        embed.footer      = EmbedFooter{p.repository.full_name + ":" + branchName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);

    } else if (byAuthor.size() > 1) {
        std::vector<EmbedField> fields;
        for (auto& [email, commits] : byAuthor) {
            std::string value;
            for (auto* c : commits)
                value += formatCommit(c->message, commits.size(), c->url, d.options) + "\n";
            fields.push_back({"Commits from " + commits.front()->author.name, value, false});
        }

        DiscordEmbed embed;
        embed.author = EmbedAuthor{p.pusher.name, std::nullopt, p.sender.avatar_url};
        embed.url    = p.repository.html_url;
        embed.color  = Colours::PUSH;
        embed.fields = std::move(fields);
        embed.footer = EmbedFooter{p.repository.full_name + ":" + branchName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);
    }
}

// ---- Issue ----------------------------------------------------------------

void GithubHandler::handleIssue(const GithubIssuePayload& p, const EventData& d) {
    // Scala original was a no-op stub (println only)
    spdlog::info("GitHub issue event: action={}", p.action);
}

// ---- Check Run ------------------------------------------------------------

void GithubHandler::handleCheckRun(const GithubCheckRunPayload& p, const EventData& d) {
    if (p.action != GithubCheckRunAction::Created) return;

    const auto& name = p.check_run.name;
    std::string lname = name;
    std::transform(lname.begin(), lname.end(), lname.begin(), ::tolower);

    if (!startsWith(lname, "deploy-") && !startsWith(lname, "deploy ")) return;

    const std::string environment = name.substr(7);
    if (environment.empty()) return;

    DiscordEmbed embed;
    embed.description = "Version " + p.check_run.head_branch + " is deploying to " + environment + "...";
    embed.author      = EmbedAuthor{p.sender.login, std::nullopt, p.sender.avatar_url};
    embed.url         = p.check_run.html_url;
    embed.color       = Colours::RUNNING;
    embed.footer      = EmbedFooter{
        p.repository.full_name + ":" + p.check_run.head_sha.substr(0, 7),
        std::string(LOGO)};
    discord_->sendMessageToDiscord(d.hook, embed);
}

// ---- Create ---------------------------------------------------------------

void GithubHandler::handleCreate(const GithubCreatePayload& p, const EventData& d) {
    std::string description;
    if (p.ref_type == GithubRefType::Branch)
        description = "Branch created: " + p.ref;
    else
        description = "Tag created: " + p.ref;

    DiscordEmbed embed;
    embed.description = description;
    embed.author      = EmbedAuthor{p.sender.login, std::nullopt, p.sender.avatar_url};
    embed.url         = p.repository.html_url;
    embed.color       = Colours::CREATED;
    embed.footer      = EmbedFooter{p.repository.full_name + ":" + p.ref, std::string(LOGO)};
    discord_->sendMessageToDiscord(d.hook, embed);
}

// ---- Delete ---------------------------------------------------------------

void GithubHandler::handleDelete(const GithubDeletePayload& p, const EventData& d) {
    std::string description;
    if (p.ref_type == GithubRefType::Branch)
        description = "Branch deleted: " + p.ref;
    else
        description = "Tag deleted: " + p.ref;

    DiscordEmbed embed;
    embed.description = description;
    embed.author      = EmbedAuthor{p.sender.login, std::nullopt, p.sender.avatar_url};
    embed.url         = p.repository.html_url;
    embed.color       = Colours::DELETED;
    embed.footer      = EmbedFooter{p.repository.full_name + ":" + p.ref, std::string(LOGO)};
    discord_->sendMessageToDiscord(d.hook, embed);
}
