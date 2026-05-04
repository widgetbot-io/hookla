#pragma once
#include <optional>
#include <string>

struct EmbedOptions {
    std::string id;
    std::string userId;
    bool areCommitsClickable;
    bool showPrivateCommits;
    std::optional<std::string> privateCommitPrefix;
    std::optional<std::string> descriptionFormat;
    std::optional<std::string> privateMessage;
    std::optional<std::string> privateCharacter;
};
