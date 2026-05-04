#pragma once
#include <functional>
#include <memory>
#include <string>
#include <unordered_map>
#include "../BaseHandler.h"
#include "../../services/DiscordMessageService.h"
#include "../../types/DiscordEmbed.h"
#include "../../types/providers/GitlabPayloads.h"

class GitlabHandler : public BaseHandler {
public:
    static constexpr const char* LOGO =
        "https://about.gitlab.com/images/press/logo/png/gitlab-icon-rgb.png";

    explicit GitlabHandler(std::shared_ptr<DiscordMessageService> discord);

    const char* eventKey() const override { return "x-gitlab-event"; }

    void handle(
        const std::string& eventName,
        const nlohmann::json& body,
        const EventData& data) override;

private:
    void handlePush(const GitlabPushPayload& p, const EventData& d);
    void handleTag(const GitlabTagPushPayload& p, const EventData& d);
    void handleNote(const GitlabNotePayload& p, const EventData& d);
    void handleIssue(const GitlabIssuePayload& p, const EventData& d);
    void handleJob(const GitlabJobPayload& p, const EventData& d);

    DiscordEmbed makeJobEmbed(const GitlabJobPayload& p, int colour, const std::string& description);

    std::shared_ptr<DiscordMessageService> discord_;
    using HandlerFn = std::function<void(const nlohmann::json&, const EventData&)>;
    std::unordered_map<std::string, HandlerFn> handlers_;
};
