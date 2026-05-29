#pragma once
#include <functional>
#include <memory>
#include <string>
#include <unordered_map>
#include "../BaseHandler.h"
#include "../../services/DiscordMessageService.h"
#include "../../types/providers/GithubPayloads.h"

class GithubHandler : public BaseHandler {
public:
    static constexpr const char* LOGO =
        "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png";

    explicit GithubHandler(std::shared_ptr<DiscordMessageService> discord);

    const char* eventKey() const override { return "x-github-event"; }

    void handle(
        const std::string& eventName,
        const nlohmann::json& body,
        const EventData& data) override;

private:
    void handlePush(const GithubPushPayload& p, const EventData& d);
    void handleIssue(const GithubIssuePayload& p, const EventData& d);
    void handleCheckRun(const GithubCheckRunPayload& p, const EventData& d);
    void handleCreate(const GithubCreatePayload& p, const EventData& d);
    void handleDelete(const GithubDeletePayload& p, const EventData& d);

    std::shared_ptr<DiscordMessageService> discord_;
    using HandlerFn = std::function<void(const nlohmann::json&, const EventData&)>;
    std::unordered_map<std::string, HandlerFn> handlers_;
};
