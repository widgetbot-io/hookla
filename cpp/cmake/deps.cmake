include(FetchContent)

# ---- Drogon (HTTP server + C++20 coroutines) ------------------------------
FetchContent_Declare(drogon
    GIT_REPOSITORY https://github.com/drogonframework/drogon
    GIT_TAG        v1.9.6
    GIT_SHALLOW    TRUE)

# ---- nlohmann/json (JSON parsing + serialisation) -------------------------
FetchContent_Declare(nlohmann_json
    GIT_REPOSITORY https://github.com/nlohmann/json
    GIT_TAG        v3.11.3
    GIT_SHALLOW    TRUE)

# ---- spdlog (structured logging) ------------------------------------------
FetchContent_Declare(spdlog
    GIT_REPOSITORY https://github.com/gabime/spdlog
    GIT_TAG        v1.14.1
    GIT_SHALLOW    TRUE)

# ---- cpp-httplib (outbound HTTPS to Discord) -------------------------------
FetchContent_Declare(httplib
    GIT_REPOSITORY https://github.com/yhirose/cpp-httplib
    GIT_TAG        v0.16.3
    GIT_SHALLOW    TRUE)

# libpqxx and OpenSSL are expected to be installed system-wide:
#   apt-get install -y libpq-dev libpqxx-dev libssl-dev
#   brew install libpqxx openssl
find_package(PostgreSQL REQUIRED)
find_package(OpenSSL     REQUIRED)

# Build libpqxx from system pkg-config if available, else find manually
find_package(PkgConfig QUIET)
if(PKG_CONFIG_FOUND)
    pkg_check_modules(PQXX libpqxx)
endif()
if(NOT PQXX_FOUND)
    find_library(PQXX_LIB pqxx REQUIRED)
    find_path(PQXX_INCLUDE pqxx/pqxx REQUIRED)
    set(PQXX_LIBRARIES    ${PQXX_LIB})
    set(PQXX_INCLUDE_DIRS ${PQXX_INCLUDE})
endif()

FetchContent_MakeAvailable(nlohmann_json spdlog httplib)

# Drogon brings many transitive deps; build it last
set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(drogon)
