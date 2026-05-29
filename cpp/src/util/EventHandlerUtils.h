#pragma once
#include <optional>
#include <string>
#include <vector>
#include "../models/EmbedOptions.h"

inline bool startsWith(const std::string& s, const std::string& prefix) {
    return s.size() >= prefix.size() && s.substr(0, prefix.size()) == prefix;
}

// private branch names begin with ! or $
inline bool isPrivateBranch(const std::string& branchName) {
    return startsWith(branchName, "!") || startsWith(branchName, "$");
}

// "refs/heads/main" -> "main"
inline std::string getBranchFromRef(const std::string& ref) {
    size_t first = ref.find('/');
    if (first == std::string::npos) return ref;
    size_t second = ref.find('/', first + 1);
    if (second == std::string::npos) return ref;
    return ref.substr(second + 1);
}

inline std::string formatCommit(
    const std::string& message,
    size_t length,
    const std::string& url,
    const std::optional<EmbedOptions>& embedOptions)
{
    static const std::vector<std::string> defaultChars = {"!", "$"};
    static const std::string defaultMsg = "This commit message has been marked as private.";

    const std::string prefix = (length > 1) ? "- " : "";

    if (!embedOptions) {
        std::vector<std::string> privateDenotations = defaultChars;
        for (auto& c : defaultChars) privateDenotations.push_back("Revert " + c);

        bool isPrivate = false;
        for (auto& d : privateDenotations) {
            if (startsWith(message, d)) { isPrivate = true; break; }
        }
        return prefix + (isPrivate ? defaultMsg : message);
    }

    const EmbedOptions& opts = *embedOptions;
    std::vector<std::string> privateChar = opts.privateCharacter
        ? std::vector<std::string>{*opts.privateCharacter}
        : defaultChars;

    std::vector<std::string> privateDenotations = privateChar;
    for (auto& c : privateChar)    privateDenotations.push_back("Revert " + c);
    for (auto& c : defaultChars)   privateDenotations.push_back(c);
    for (auto& c : defaultChars)   privateDenotations.push_back("Revert " + c);

    bool isPrivate = false;
    for (auto& d : privateDenotations) {
        if (startsWith(message, d)) { isPrivate = true; break; }
    }

    const std::string clickableMsg = opts.areCommitsClickable
        ? "[" + message + "](" + url + ")"
        : message;

    std::string finalMsg;
    if (!opts.showPrivateCommits) {
        if (isPrivate)
            finalMsg = opts.privateMessage.value_or(defaultMsg);
        else
            finalMsg = opts.privateCommitPrefix.value_or("[Private] ") + clickableMsg;
    } else {
        finalMsg = clickableMsg;
    }

    return prefix + finalMsg;
}
