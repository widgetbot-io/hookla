#include "GitlabHandler.h"
#include <map>
#include <spdlog/spdlog.h>
#include "../../types/DiscordEmbed.h"
#include "../../util/Colours.h"
#include "../../util/EventHandlerUtils.h"

static const std::string ZEROES = "0000000000000000000000000000000000000000";

GitlabHandler::GitlabHandler(std::shared_ptr<DiscordMessageService> discord)
    : discord_(std::move(discord))
{
    auto job = [this](const nlohmann::json& j, const EventData& d) { handleJob(j.get<GitlabJobPayload>(), d); };
    handlers_["Push Hook"]     = [this](const nlohmann::json& j, const EventData& d) { handlePush(j.get<GitlabPushPayload>(), d); };
    handlers_["Tag Push Hook"] = [this](const nlohmann::json& j, const EventData& d) { handleTag(j.get<GitlabTagPushPayload>(), d); };
    handlers_["Note Hook"]     = [this](const nlohmann::json& j, const EventData& d) { handleNote(j.get<GitlabNotePayload>(), d); };
    handlers_["Issue Hook"]    = [this](const nlohmann::json& j, const EventData& d) { handleIssue(j.get<GitlabIssuePayload>(), d); };
    handlers_["Job Hook"]      = job;
    handlers_["Build Hook"]    = job;
}

void GitlabHandler::handle(
    const std::string& eventName,
    const nlohmann::json& body,
    const EventData& data)
{
    auto it = handlers_.find(eventName);
    if (it == handlers_.end()) {
        spdlog::warn("Unhandled GitLab event: {}", eventName);
        return;
    }
    it->second(body, data);
}

// ---- Push -----------------------------------------------------------------

void GitlabHandler::handlePush(const GitlabPushPayload& p, const EventData& d) {
    const std::string branchName = getBranchFromRef(p.ref);
    if (isPrivateBranch(branchName)) return;

    // Branch created/deleted notification (same logic as Scala handleBranches)
    if (p.before == ZEROES) {
        DiscordEmbed embed;
        embed.description = "Branch created: " + branchName;
        embed.author      = EmbedAuthor{p.user_name, std::nullopt, p.user_avatar};
        embed.url         = p.project.web_url;
        embed.color       = Colours::CREATED;
        embed.footer      = EmbedFooter{p.project.path_with_namespace + ":" + branchName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);
    } else if (p.after == ZEROES) {
        DiscordEmbed embed;
        embed.description = "Branch deleted: " + branchName;
        embed.author      = EmbedAuthor{p.user_name, std::nullopt, p.user_avatar};
        embed.url         = p.project.web_url;
        embed.color       = Colours::DELETED;
        embed.footer      = EmbedFooter{p.project.path_with_namespace + ":" + branchName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);
    }

    // Commit notification
    std::map<std::string, std::vector<const GitlabCommit*>> byAuthor;
    for (auto& c : p.commits)
        byAuthor[c.author.email].push_back(&c);

    if (byAuthor.size() == 1) {
        auto& commits = byAuthor.begin()->second;
        std::string description;
        for (auto* c : commits)
            description += formatCommit(c->message, commits.size(), c->url, d.options) + "\n";

        DiscordEmbed embed;
        embed.description = description;
        embed.author      = EmbedAuthor{p.user_name, std::nullopt, p.user_avatar};
        embed.url         = p.project.web_url;
        embed.color       = Colours::PUSH;
        embed.footer      = EmbedFooter{p.project.path_with_namespace + ":" + branchName, std::string(LOGO)};
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
        embed.author = EmbedAuthor{p.user_name, std::nullopt, p.user_avatar};
        embed.url    = p.project.web_url;
        embed.color  = Colours::PUSH;
        embed.fields = std::move(fields);
        embed.footer = EmbedFooter{p.project.path_with_namespace + ":" + branchName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);
    }
}

// ---- Tag ------------------------------------------------------------------

void GitlabHandler::handleTag(const GitlabTagPushPayload& p, const EventData& d) {
    const std::string refName = getBranchFromRef(p.ref);

    if (p.before == ZEROES) {
        DiscordEmbed embed;
        embed.description = "Tag created: " + refName;
        embed.author      = EmbedAuthor{p.user_name, std::nullopt, p.user_avatar};
        embed.url         = p.project.web_url;
        embed.color       = Colours::CREATED;
        embed.footer      = EmbedFooter{p.project.path_with_namespace + ":" + refName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);
    } else if (p.after == ZEROES) {
        DiscordEmbed embed;
        embed.description = "Tag deleted: " + refName;
        embed.author      = EmbedAuthor{p.user_name, std::nullopt, p.user_avatar};
        embed.url         = p.project.web_url;
        embed.color       = Colours::DELETED;
        embed.footer      = EmbedFooter{p.project.path_with_namespace + ":" + refName, std::string(LOGO)};
        discord_->sendMessageToDiscord(d.hook, embed);
    }
}

// ---- Note -----------------------------------------------------------------

void GitlabHandler::handleNote(const GitlabNotePayload& p, const EventData& d) {
    std::string title = "Unknown";
    std::string url   = p.project.web_url;

    if (p.object_attributes.noteable_type == "Commit") {
        title = "Commit (" + p.commit->id.substr(0, 7) + ")";
    } else if (p.object_attributes.noteable_type == "MergeRequest") {
        title = "Merge Request #" + std::to_string(p.merge_request->iid);
        url   = p.object_attributes.url;
    }
    // Issue and Snippet are unimplemented in the Scala original too

    DiscordEmbed embed;
    embed.title       = title;
    embed.description = p.object_attributes.note;
    embed.author      = EmbedAuthor{p.user.name, std::nullopt, p.user.avatar_url};
    embed.url         = url;
    embed.color       = Colours::NOTE;
    embed.footer      = EmbedFooter{p.project.path_with_namespace, std::string(LOGO)};
    discord_->sendMessageToDiscord(d.hook, embed);
}

// ---- Issue ----------------------------------------------------------------

void GitlabHandler::handleIssue(const GitlabIssuePayload& p, const EventData& d) {
    // Scala original was unimplemented (???)
    spdlog::info("GitLab issue event: action={}", p.action);
}

// ---- Job ------------------------------------------------------------------

DiscordEmbed GitlabHandler::makeJobEmbed(
    const GitlabJobPayload& p, int colour, const std::string& description)
{
    // Extract "owner/repo" from homepage URL by dropping the scheme+host
    std::string footer_text;
    auto parts = p.repository.homepage;
    size_t pos = 0;
    int slashes = 0;
    for (size_t i = 0; i < parts.size(); ++i) {
        if (parts[i] == '/') {
            if (++slashes == 3) { pos = i + 1; break; }
        }
    }
    footer_text = parts.substr(pos) + ":" + p.ref;

    DiscordEmbed embed;
    embed.description = description;
    embed.author      = EmbedAuthor{p.user.name, std::nullopt, p.user.avatar_url};
    embed.url         = p.repository.homepage + "/-/jobs/" + std::to_string(p.build_id);
    embed.color       = colour;
    embed.footer      = EmbedFooter{footer_text, std::string(LOGO)};
    return embed;
}

void GitlabHandler::handleJob(const GitlabJobPayload& p, const EventData& d) {
    if (p.build_status == "failed") {
        if (!p.build_allow_failure)
            discord_->sendMessageToDiscord(d.hook, makeJobEmbed(p, Colours::FAILED, "The job has failed."));

    } else if (p.build_status == "canceled") {
        discord_->sendMessageToDiscord(d.hook, makeJobEmbed(p, Colours::CANCELED, "The job has been canceled."));

    } else if (p.build_status == "running") {
        if (!startsWith(p.build_name, "deploy-")) return;
        const std::string env = p.build_name.substr(7);
        if (env.empty()) return;
        auto embed = p.tag
            ? makeJobEmbed(p, Colours::RUNNING,  "Version " + p.ref + " is deploying to " + env + "...")
            : makeJobEmbed(p, Colours::CANCELED, "Deploying latest commit to " + env + "...");
        discord_->sendMessageToDiscord(d.hook, embed);

    } else if (p.build_status == "success") {
        if (!startsWith(p.build_name, "deploy-")) return;
        const std::string env = p.build_name.substr(7);
        if (env.empty()) return;
        auto embed = p.tag
            ? makeJobEmbed(p, Colours::RUNNING,  "Version " + p.ref + " has been deployed to " + env + ".")
            : makeJobEmbed(p, Colours::CANCELED, "Deployed latest commit to " + env + ".");
        discord_->sendMessageToDiscord(d.hook, embed);
    }
}
