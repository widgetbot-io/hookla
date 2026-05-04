#pragma once
#include <memory>
#include <optional>
#include <string>
#include "../db/ConnectionPool.h"
#include "../models/EmbedOptions.h"
#include "../models/ProviderSettings.h"
#include "EmbedOptionsService.h"

class ProviderSettingsService {
public:
    ProviderSettingsService(
        std::shared_ptr<ConnectionPool> pool,
        std::shared_ptr<EmbedOptionsService> embedOptionsService);

    std::optional<ProviderSettings> getById(const std::string& id);
    std::optional<ProviderSettings> getByToken(const std::string& token);
    std::optional<EmbedOptions> getOptionsForProvider(const ProviderSettings& ps);

private:
    std::shared_ptr<ConnectionPool> pool_;
    std::shared_ptr<EmbedOptionsService> embedOptionsService_;

    std::optional<ProviderSettings> fetchWhere(const std::string& col, const std::string& val);
};
