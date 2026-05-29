#pragma once
#include "../models/DiscordWebhook.h"
#include "../types/DiscordEmbed.h"

class DiscordMessageService {
public:
    DiscordMessageService();
    void sendMessageToDiscord(const DiscordWebhook& hook, const DiscordEmbed& embed);
};
