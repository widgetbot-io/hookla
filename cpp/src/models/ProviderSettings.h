#pragma once
#include <optional>
#include <string>

struct ProviderSettings {
    std::string id;
    std::string userId;
    std::string discordWebhookId;
    std::optional<std::string> optionsId;
    std::string slug;
    std::string token;
};
