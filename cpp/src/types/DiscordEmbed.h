#pragma once
#include <optional>
#include <string>
#include <vector>
#include <nlohmann/json.hpp>
#include <chrono>
#include <iomanip>
#include <sstream>

struct EmbedAuthor {
    std::string name;
    std::optional<std::string> url;
    std::optional<std::string> icon_url;
};

struct EmbedFooter {
    std::string text;
    std::optional<std::string> icon_url;
};

struct EmbedField {
    std::string name;
    std::string value;
    bool inline_ = false;
};

struct DiscordEmbed {
    std::optional<std::string> title;
    std::optional<std::string> description;
    std::optional<std::string> url;
    std::optional<int> color;
    std::optional<EmbedAuthor> author;
    std::optional<EmbedFooter> footer;
    std::vector<EmbedField> fields;
};

inline std::string isoTimestamp() {
    auto now = std::chrono::system_clock::now();
    auto t   = std::chrono::system_clock::to_time_t(now);
    std::ostringstream oss;
    oss << std::put_time(std::gmtime(&t), "%Y-%m-%dT%H:%M:%SZ");
    return oss.str();
}

inline nlohmann::json embedToJson(const DiscordEmbed& e) {
    nlohmann::json j;
    if (e.title)       j["title"]       = *e.title;
    if (e.description) j["description"] = *e.description;
    if (e.url)         j["url"]         = *e.url;
    if (e.color)       j["color"]       = *e.color;
    j["timestamp"] = isoTimestamp();

    if (e.author) {
        auto& a = *e.author;
        j["author"]["name"] = a.name;
        if (a.url)      j["author"]["url"]      = *a.url;
        if (a.icon_url) j["author"]["icon_url"] = *a.icon_url;
    }
    if (e.footer) {
        auto& f = *e.footer;
        j["footer"]["text"] = f.text;
        if (f.icon_url) j["footer"]["icon_url"] = *f.icon_url;
    }
    if (!e.fields.empty()) {
        j["fields"] = nlohmann::json::array();
        for (auto& f : e.fields) {
            j["fields"].push_back({
                {"name",   f.name},
                {"value",  f.value},
                {"inline", f.inline_}
            });
        }
    }
    return j;
}
