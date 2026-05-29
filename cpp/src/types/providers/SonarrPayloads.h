#pragma once
#include <optional>
#include <string>
#include <vector>
#include <nlohmann/json.hpp>

// ---- Enum -----------------------------------------------------------------

enum class SonarrEventType { Download, Grab, Rename, Test };

inline void from_json(const nlohmann::json& j, SonarrEventType& v) {
    auto s = j.get<std::string>();
    if      (s == "Download") v = SonarrEventType::Download;
    else if (s == "Grab")     v = SonarrEventType::Grab;
    else if (s == "Rename")   v = SonarrEventType::Rename;
    else                      v = SonarrEventType::Test;
}

// ---- Structs --------------------------------------------------------------

struct SonarrSeries {
    int id;
    std::string title;
    std::string path;
    std::optional<int> tvdbId;
};

inline void from_json(const nlohmann::json& j, SonarrSeries& v) {
    j.at("id").get_to(v.id);
    j.at("title").get_to(v.title);
    j.at("path").get_to(v.path);
    if (j.contains("tvdbId") && !j["tvdbId"].is_null())
        v.tvdbId = j["tvdbId"].get<int>();
}

struct SonarrRelease {
    std::optional<std::string> quality;
    std::optional<int>         qualityVersion;
    std::optional<std::string> releaseGroup;
    std::optional<std::string> releaseTitle;
    std::optional<std::string> indexer;
    std::optional<int>         size;
};

inline void from_json(const nlohmann::json& j, SonarrRelease& v) {
    auto get_opt_str = [&](const char* key, std::optional<std::string>& f) {
        if (j.contains(key) && !j[key].is_null()) f = j[key].get<std::string>();
    };
    auto get_opt_int = [&](const char* key, std::optional<int>& f) {
        if (j.contains(key) && !j[key].is_null()) f = j[key].get<int>();
    };
    get_opt_str("quality",      v.quality);
    get_opt_int("qualityVersion", v.qualityVersion);
    get_opt_str("releaseGroup", v.releaseGroup);
    get_opt_str("releaseTitle", v.releaseTitle);
    get_opt_str("indexer",      v.indexer);
    get_opt_int("size",         v.size);
}

struct SonarrEpisode {
    int id;
    int episodeNumber;
    int seasonNumber;
    std::string title;
    std::optional<std::string> airDateUtc;
};

inline void from_json(const nlohmann::json& j, SonarrEpisode& v) {
    j.at("id").get_to(v.id);
    j.at("episodeNumber").get_to(v.episodeNumber);
    j.at("seasonNumber").get_to(v.seasonNumber);
    j.at("title").get_to(v.title);
    if (j.contains("airDateUtc") && !j["airDateUtc"].is_null())
        v.airDateUtc = j["airDateUtc"].get<std::string>();
}

struct SonarrEpisodeFile {
    int id;
    std::string relativePath;
    std::string path;
    std::optional<std::string> quality;
    std::optional<int>         qualityVersion;
    std::optional<std::string> releaseGroup;
    std::optional<std::string> sceneName;
};

inline void from_json(const nlohmann::json& j, SonarrEpisodeFile& v) {
    j.at("id").get_to(v.id);
    j.at("relativePath").get_to(v.relativePath);
    j.at("path").get_to(v.path);
    auto get_opt_str = [&](const char* key, std::optional<std::string>& f) {
        if (j.contains(key) && !j[key].is_null()) f = j[key].get<std::string>();
    };
    auto get_opt_int = [&](const char* key, std::optional<int>& f) {
        if (j.contains(key) && !j[key].is_null()) f = j[key].get<int>();
    };
    get_opt_str("quality",      v.quality);
    get_opt_int("qualityVersion", v.qualityVersion);
    get_opt_str("releaseGroup", v.releaseGroup);
    get_opt_str("sceneName",    v.sceneName);
}

// ---- Payloads -------------------------------------------------------------

struct SonarrGrabEvent {
    SonarrEventType eventType;
    SonarrSeries series;
    std::vector<SonarrEpisode> episodes;
    SonarrRelease release;
    std::optional<SonarrEpisodeFile> episodeFile;
    std::optional<bool> isUpgrade;
};

inline void from_json(const nlohmann::json& j, SonarrGrabEvent& v) {
    j.at("eventType").get_to(v.eventType);
    j.at("series").get_to(v.series);
    j.at("episodes").get_to(v.episodes);
    j.at("release").get_to(v.release);
    if (j.contains("episodeFile") && !j["episodeFile"].is_null())
        v.episodeFile = j["episodeFile"].get<SonarrEpisodeFile>();
    if (j.contains("isUpgrade") && !j["isUpgrade"].is_null())
        v.isUpgrade = j["isUpgrade"].get<bool>();
}

struct SonarrDownloadEvent {
    SonarrEventType eventType;
    SonarrSeries series;
    std::vector<SonarrEpisode> episodes;
    std::optional<SonarrRelease> release;
    SonarrEpisodeFile episodeFile;
    bool isUpgrade;
};

inline void from_json(const nlohmann::json& j, SonarrDownloadEvent& v) {
    j.at("eventType").get_to(v.eventType);
    j.at("series").get_to(v.series);
    j.at("episodes").get_to(v.episodes);
    j.at("episodeFile").get_to(v.episodeFile);
    j.at("isUpgrade").get_to(v.isUpgrade);
    if (j.contains("release") && !j["release"].is_null())
        v.release = j["release"].get<SonarrRelease>();
}

struct SonarrRenameEvent {
    SonarrEventType eventType;
    SonarrSeries series;
    std::optional<std::vector<SonarrEpisode>> episodes;
    std::optional<SonarrRelease> release;
    std::optional<SonarrEpisodeFile> episodeFile;
    std::optional<bool> isUpgrade;
};

inline void from_json(const nlohmann::json& j, SonarrRenameEvent& v) {
    j.at("eventType").get_to(v.eventType);
    j.at("series").get_to(v.series);
    if (j.contains("episodes") && !j["episodes"].is_null())
        v.episodes = j["episodes"].get<std::vector<SonarrEpisode>>();
    if (j.contains("release") && !j["release"].is_null())
        v.release = j["release"].get<SonarrRelease>();
    if (j.contains("episodeFile") && !j["episodeFile"].is_null())
        v.episodeFile = j["episodeFile"].get<SonarrEpisodeFile>();
    if (j.contains("isUpgrade") && !j["isUpgrade"].is_null())
        v.isUpgrade = j["isUpgrade"].get<bool>();
}

struct SonarrTestEvent {
    SonarrEventType eventType;
    SonarrSeries series;
    std::optional<std::vector<SonarrEpisode>> episodes;
    std::optional<SonarrRelease> release;
    std::optional<SonarrEpisodeFile> episodeFile;
    std::optional<bool> isUpgrade;
};

inline void from_json(const nlohmann::json& j, SonarrTestEvent& v) {
    j.at("eventType").get_to(v.eventType);
    j.at("series").get_to(v.series);
    if (j.contains("episodes") && !j["episodes"].is_null())
        v.episodes = j["episodes"].get<std::vector<SonarrEpisode>>();
    if (j.contains("release") && !j["release"].is_null())
        v.release = j["release"].get<SonarrRelease>();
    if (j.contains("episodeFile") && !j["episodeFile"].is_null())
        v.episodeFile = j["episodeFile"].get<SonarrEpisodeFile>();
    if (j.contains("isUpgrade") && !j["isUpgrade"].is_null())
        v.isUpgrade = j["isUpgrade"].get<bool>();
}
