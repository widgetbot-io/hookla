cmake_minimum_required(VERSION 3.25)
project(hookla CXX)

set(CMAKE_CXX_STANDARD          20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS        OFF)

# Enable TLS in cpp-httplib
add_compile_definitions(CPPHTTPLIB_OPENSSL_SUPPORT)

include(cmake/deps.cmake)

# ---- Source files ----------------------------------------------------------
set(SOURCES
    src/main.cpp
    src/controllers/WebhookController.cpp
    src/services/DiscordMessageService.cpp
    src/services/DiscordWebhookService.cpp
    src/services/EmbedOptionsService.cpp
    src/services/ProviderSettingsService.cpp
    src/handlers/github/GithubHandler.cpp
    src/handlers/gitlab/GitlabHandler.cpp
    src/handlers/sonarr/SonarrHandler.cpp
)

add_executable(hookla ${SOURCES})

target_include_directories(hookla PRIVATE
    src
    ${PQXX_INCLUDE_DIRS}
)

target_link_libraries(hookla PRIVATE
    Drogon::Drogon
    nlohmann_json::nlohmann_json
    spdlog::spdlog
    httplib::httplib
    OpenSSL::SSL
    OpenSSL::Crypto
    ${PQXX_LIBRARIES}
    PostgreSQL::PostgreSQL
)

# Drogon needs coroutine support on Clang
if(CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
    target_compile_options(hookla PRIVATE -fcoroutines)
endif()

# ---- Install ---------------------------------------------------------------
install(TARGETS hookla DESTINATION bin)
