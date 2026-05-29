#pragma once
#include "../BaseHandler.h"
#include "../../services/DiscordMessageService.h"
#include <memory>
#include <spdlog/spdlog.h>

class RadarrHandler : public BaseHandler {
public:
    explicit RadarrHandler(std::shared_ptr<DiscordMessageService> discord)
        : discord_(std::move(discord)) {}

    const char* eventKey() const override { return "eventType"; }
    bool isBody() const override { return true; }

    void handle(const std::string& eventName, const nlohmann::json&, const EventData&) override {
        spdlog::warn("RadarrHandler not yet implemented (event: {})", eventName);
    }

private:
    std::shared_ptr<DiscordMessageService> discord_;
};
