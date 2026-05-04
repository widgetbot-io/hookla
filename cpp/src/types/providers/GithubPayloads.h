#pragma once
#include <optional>
#include <string>
#include <vector>
#include <nlohmann/json.hpp>

// ---- Enums ----------------------------------------------------------------

enum class GithubRefType        { Branch, Tag };
enum class GithubCheckRunAction { Created, Completed, ReRequested, RequestedAction };
enum class GithubCheckRunStatus { Queued, InProgress, Completed };
enum class GithubCheckRunConclusion {
    Success, Failure, Neutral, Cancelled, TimedOut, ActionRequired, Stale
};

inline void from_json(const nlohmann::json& j, GithubRefType& v) {
    auto s = j.get<std::string>();
    if (s == "branch") v = GithubRefType::Branch;
    else               v = GithubRefType::Tag;
}
inline void from_json(const nlohmann::json& j, GithubCheckRunAction& v) {
    auto s = j.get<std::string>();
    if      (s == "created")          v = GithubCheckRunAction::Created;
    else if (s == "completed")        v = GithubCheckRunAction::Completed;
    else if (s == "rerequested")      v = GithubCheckRunAction::ReRequested;
    else                              v = GithubCheckRunAction::RequestedAction;
}
inline void from_json(const nlohmann::json& j, GithubCheckRunStatus& v) {
    auto s = j.get<std::string>();
    if      (s == "queued")      v = GithubCheckRunStatus::Queued;
    else if (s == "in_progress") v = GithubCheckRunStatus::InProgress;
    else                         v = GithubCheckRunStatus::Completed;
}
inline void from_json(const nlohmann::json& j, GithubCheckRunConclusion& v) {
    auto s = j.get<std::string>();
    if      (s == "success")         v = GithubCheckRunConclusion::Success;
    else if (s == "failure")         v = GithubCheckRunConclusion::Failure;
    else if (s == "neutral")         v = GithubCheckRunConclusion::Neutral;
    else if (s == "cancelled")       v = GithubCheckRunConclusion::Cancelled;
    else if (s == "timed_out")       v = GithubCheckRunConclusion::TimedOut;
    else if (s == "action_required") v = GithubCheckRunConclusion::ActionRequired;
    else                             v = GithubCheckRunConclusion::Stale;
}

// ---- Structs --------------------------------------------------------------

struct GithubPusher {
    std::string name;
    std::string email;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubPusher, name, email)

struct GithubSender {
    std::string login;
    std::string avatar_url;
    std::string html_url;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubSender, login, avatar_url, html_url)

struct GithubOwner {
    std::string login;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubOwner, login)

struct GithubRepository {
    std::string name;
    std::string html_url;
    std::string full_name;
    GithubOwner owner;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubRepository, name, html_url, full_name, owner)

struct GithubCommit {
    std::string message;
    std::string url;
    GithubPusher author;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubCommit, message, url, author)

struct GithubCheckRun {
    std::string head_branch;
    std::string html_url;
    std::string head_sha;
    GithubCheckRunStatus status;
    std::optional<GithubCheckRunConclusion> conclusion;
    std::string name;
};

inline void from_json(const nlohmann::json& j, GithubCheckRun& v) {
    j.at("head_branch").get_to(v.head_branch);
    j.at("html_url").get_to(v.html_url);
    j.at("head_sha").get_to(v.head_sha);
    j.at("status").get_to(v.status);
    j.at("name").get_to(v.name);
    if (j.contains("conclusion") && !j["conclusion"].is_null())
        v.conclusion = j["conclusion"].get<GithubCheckRunConclusion>();
}

// ---- Payloads -------------------------------------------------------------

struct GithubPushPayload {
    std::string ref;
    std::string before;
    std::string after;
    std::vector<GithubCommit> commits;
    GithubPusher pusher;
    GithubSender sender;
    GithubRepository repository;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubPushPayload, ref, before, after, commits, pusher, sender, repository)

struct GithubIssuePayload {
    std::string action;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubIssuePayload, action)

struct GithubCheckRunPayload {
    GithubCheckRunAction action;
    GithubCheckRun check_run;
    GithubRepository repository;
    GithubSender sender;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubCheckRunPayload, action, check_run, repository, sender)

struct GithubCreatePayload {
    std::string ref;
    GithubRefType ref_type;
    std::string pusher_type;
    GithubRepository repository;
    GithubSender sender;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubCreatePayload, ref, ref_type, pusher_type, repository, sender)

struct GithubDeletePayload {
    std::string ref;
    GithubRefType ref_type;
    std::string pusher_type;
    GithubRepository repository;
    GithubSender sender;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GithubDeletePayload, ref, ref_type, pusher_type, repository, sender)
