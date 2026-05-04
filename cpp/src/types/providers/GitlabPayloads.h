#pragma once
#include <optional>
#include <string>
#include <vector>
#include <nlohmann/json.hpp>

struct GitlabAuthor {
    std::string name;
    std::string email;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabAuthor, name, email)

struct GitlabProject {
    std::string name;
    std::string path_with_namespace;
    std::string web_url;
    std::string description;
    std::string url;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabProject, name, path_with_namespace, web_url, description, url)

struct GitlabRepository {
    std::string name;
    std::string url;
    std::string description;
    std::string homepage;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabRepository, name, url, description, homepage)

struct GitlabCommit {
    std::string id;
    std::string message;
    std::string timestamp;
    std::string url;
    GitlabAuthor author;
    std::vector<std::string> added;
    std::vector<std::string> modified;
    std::vector<std::string> removed;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabCommit, id, message, timestamp, url, author, added, modified, removed)

struct GitlabMergeRequest {
    int id;
    int iid;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabMergeRequest, id, iid)

struct GitlabObjectAttributes {
    std::string noteable_type;
    std::string note;
    std::string url;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabObjectAttributes, noteable_type, note, url)

struct GitlabUser {
    std::string name;
    std::string username;
    std::string avatar_url;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabUser, name, username, avatar_url)

struct GitlabPushPayload {
    std::string object_kind;
    std::string before;
    std::string after;
    std::string ref;
    std::string checkout_sha;
    int user_id;
    std::string user_name;
    std::string user_email;
    std::string user_avatar;
    int project_id;
    GitlabProject project;
    GitlabRepository repository;
    std::vector<GitlabCommit> commits;
    int total_commits_count;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabPushPayload,
    object_kind, before, after, ref, checkout_sha,
    user_id, user_name, user_email, user_avatar, project_id,
    project, repository, commits, total_commits_count)

struct GitlabTagPushPayload {
    std::string object_kind;
    std::string before;
    std::string after;
    std::string ref;
    std::string checkout_sha;
    int user_id;
    std::string user_name;
    std::string user_email;
    std::string user_avatar;
    int project_id;
    GitlabProject project;
    GitlabRepository repository;
    std::vector<GitlabCommit> commits;
    int total_commits_count;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabTagPushPayload,
    object_kind, before, after, ref, checkout_sha,
    user_id, user_name, user_email, user_avatar, project_id,
    project, repository, commits, total_commits_count)

struct GitlabNotePayload {
    std::string object_kind;
    GitlabUser user;
    int project_id;
    GitlabProject project;
    GitlabRepository repository;
    GitlabObjectAttributes object_attributes;
    std::optional<GitlabCommit> commit;
    std::optional<GitlabMergeRequest> merge_request;
};

inline void from_json(const nlohmann::json& j, GitlabNotePayload& v) {
    j.at("object_kind").get_to(v.object_kind);
    j.at("user").get_to(v.user);
    j.at("project_id").get_to(v.project_id);
    j.at("project").get_to(v.project);
    j.at("repository").get_to(v.repository);
    j.at("object_attributes").get_to(v.object_attributes);
    if (j.contains("commit") && !j["commit"].is_null())
        v.commit = j["commit"].get<GitlabCommit>();
    if (j.contains("merge_request") && !j["merge_request"].is_null())
        v.merge_request = j["merge_request"].get<GitlabMergeRequest>();
}

struct GitlabIssuePayload {
    std::string action;
};
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(GitlabIssuePayload, action)

struct GitlabJobPayload {
    std::string object_kind;
    std::string ref;
    bool tag;
    std::string before_sha;
    std::string sha;
    int build_id;
    std::string build_name;
    std::string build_stage;
    std::string build_status;
    std::string build_started_at;
    std::optional<std::string> build_finished_at;
    float build_duration;
    bool build_allow_failure;
    int project_id;
    std::string project_name;
    GitlabUser user;
    GitlabRepository repository;
};

inline void from_json(const nlohmann::json& j, GitlabJobPayload& v) {
    j.at("object_kind").get_to(v.object_kind);
    j.at("ref").get_to(v.ref);
    j.at("tag").get_to(v.tag);
    j.at("before_sha").get_to(v.before_sha);
    j.at("sha").get_to(v.sha);
    j.at("build_id").get_to(v.build_id);
    j.at("build_name").get_to(v.build_name);
    j.at("build_stage").get_to(v.build_stage);
    j.at("build_status").get_to(v.build_status);
    j.at("build_started_at").get_to(v.build_started_at);
    j.at("build_duration").get_to(v.build_duration);
    j.at("build_allow_failure").get_to(v.build_allow_failure);
    j.at("project_id").get_to(v.project_id);
    j.at("project_name").get_to(v.project_name);
    j.at("user").get_to(v.user);
    j.at("repository").get_to(v.repository);
    if (j.contains("build_finished_at") && !j["build_finished_at"].is_null())
        v.build_finished_at = j["build_finished_at"].get<std::string>();
}
