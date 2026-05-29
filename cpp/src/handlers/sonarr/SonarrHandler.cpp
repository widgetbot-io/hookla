#include "SonarrHandler.h"
#include <spdlog/spdlog.h>
#include "../../types/DiscordEmbed.h"
#include "../../util/Colours.h"

SonarrHandler::SonarrHandler(std::shared_ptr<DiscordMessageService> discord)
    : discord_(std::move(discord))
{
    handlers_["Grab"]     = [this](const nlohmann::json& j, const EventData& d) { handleGrab(j.get<SonarrGrabEvent>(), d); };
    handlers_["Download"] = [this](const nlohmann::json& j, const EventData& d) { handleDownload(j.get<SonarrDownloadEvent>(), d); };
    handlers_["Rename"]   = [this](const nlohmann::json& j, const EventData& d) { handleRename(j.get<SonarrRenameEvent>(), d); };
    handlers_["Test"]     = [this](const nlohmann::json& j, const EventData& d) { handleTest(j.get<SonarrTestEvent>(), d); };
}

void SonarrHandler::handle(
    const std::string& eventName,
    const nlohmann::json& body,
    const EventData& data)
{
    auto it = handlers_.find(eventName);
    if (it == handlers_.end()) {
        spdlog::warn("Unhandled Sonarr event: {}", eventName);
        return;
    }
    it->second(body, data);
}

// Sonarr Grab and Download/Rename are stubs in the Scala original.
// Implementing Test as it's the only fully implemented one.

void SonarrHandler::handleGrab(const SonarrGrabEvent& p, const EventData& d) {
    // Unimplemented in Scala original
    spdlog::info("Sonarr grab event for series: {}", p.series.title);
}

void SonarrHandler::handleDownload(const SonarrDownloadEvent& p, const EventData& d) {
    // Unimplemented in Scala original
    spdlog::info("Sonarr download event for series: {}", p.series.title);
}

void SonarrHandler::handleRename(const SonarrRenameEvent& p, const EventData& d) {
    // Unimplemented in Scala original
    spdlog::info("Sonarr rename event for series: {}", p.series.title);
}

void SonarrHandler::handleTest(const SonarrTestEvent& p, const EventData& d) {
    DiscordEmbed embed;
    embed.description = "Sonarr Test Hook!";
    embed.author      = EmbedAuthor{"Sonarr", std::nullopt, std::string(LOGO)};
    embed.color       = Colours::CREATED;
    embed.footer      = EmbedFooter{"Sonarr", std::string(LOGO)};
    discord_->sendMessageToDiscord(d.hook, embed);
}
