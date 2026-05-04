#pragma once
#include <optional>
#include "../models/DiscordWebhook.h"
#include "../models/EmbedOptions.h"

struct EventData {
    DiscordWebhook hook;
    std::optional<EmbedOptions> options;
};
