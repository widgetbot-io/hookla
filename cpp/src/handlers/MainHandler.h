#pragma once
#include <algorithm>
#include <memory>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <nlohmann/json.hpp>
#include <spdlog/spdlog.h>
#include "BaseHandler.h"
#include "../types/EventData.h"

class MainHandler {
public:
    void registerHandler(const std::string& slug, std::shared_ptr<BaseHandler> handler) {
        handlers_[slug] = std::move(handler);
    }

    void handle(
        const std::string& slug,
        const nlohmann::json& body,
        const std::unordered_map<std::string, std::string>& headers,
        const EventData& data)
    {
        auto it = handlers_.find(slug);
        if (it == handlers_.end()) {
            spdlog::warn("Unhandled provider slug: {}", slug);
            return;
        }
        auto& handler = *it->second;

        std::string eventName;
        if (handler.isBody()) {
            eventName = body.at(handler.eventKey()).get<std::string>();
        } else {
            std::string key = handler.eventKey();
            std::transform(key.begin(), key.end(), key.begin(), ::tolower);
            auto hit = headers.find(key);
            if (hit == headers.end())
                throw std::runtime_error("event header not found: " + key);
            eventName = hit->second;
        }

        handler.handle(eventName, body, data);
    }

private:
    std::unordered_map<std::string, std::shared_ptr<BaseHandler>> handlers_;
};
