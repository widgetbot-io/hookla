#pragma once
#include <string>
#include <unordered_map>
#include <nlohmann/json.hpp>
#include "../types/EventData.h"

class BaseHandler {
public:
    virtual ~BaseHandler() = default;

    // The HTTP header (or body field) that contains the event name.
    virtual const char* eventKey() const = 0;

    // True if the event key lives inside the JSON body rather than an HTTP header.
    virtual bool isBody() const { return false; }

    // Dispatch to the appropriate event handler.
    virtual void handle(
        const std::string& eventName,
        const nlohmann::json& body,
        const EventData& data) = 0;
};
