#pragma once
#include <functional>
#include <memory>
#include <string>
#include <unordered_map>
#include "../BaseHandler.h"
#include "../../services/DiscordMessageService.h"
#include "../../types/providers/SonarrPayloads.h"

class SonarrHandler : public BaseHandler {
public:
    static constexpr const char* LOGO =
        "https://forums-sonarr-tv.s3.dualstack.us-east-1.amazonaws.com/original/2X/e/"
        "ef4553fe96f04a298ec502279731579698e96a9b.png";

    explicit SonarrHandler(std::shared_ptr<DiscordMessageService> discord);

    const char* eventKey() const override { return "eventType"; }
    bool isBody() const override { return true; }

    void handle(
        const std::string& eventName,
        const nlohmann::json& body,
        const EventData& data) override;

private:
    void handleGrab(const SonarrGrabEvent& p, const EventData& d);
    void handleDownload(const SonarrDownloadEvent& p, const EventData& d);
    void handleRename(const SonarrRenameEvent& p, const EventData& d);
    void handleTest(const SonarrTestEvent& p, const EventData& d);

    std::shared_ptr<DiscordMessageService> discord_;
    using HandlerFn = std::function<void(const nlohmann::json&, const EventData&)>;
    std::unordered_map<std::string, HandlerFn> handlers_;
};
