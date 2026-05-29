#pragma once
#include <memory>
#include <optional>
#include <string>
#include "../db/ConnectionPool.h"
#include "../models/EmbedOptions.h"

class EmbedOptionsService {
public:
    explicit EmbedOptionsService(std::shared_ptr<ConnectionPool> pool);

    std::optional<EmbedOptions> getById(const std::string& id);

private:
    std::shared_ptr<ConnectionPool> pool_;
};
