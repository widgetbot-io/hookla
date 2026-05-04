#pragma once
#include <memory>
#include <optional>
#include <string>
#include "../db/ConnectionPool.h"
#include "../models/DiscordWebhook.h"

class DiscordWebhookService {
public:
    explicit DiscordWebhookService(std::shared_ptr<ConnectionPool> pool);

    std::optional<DiscordWebhook> getById(const std::string& id);

private:
    std::shared_ptr<ConnectionPool> pool_;
};
