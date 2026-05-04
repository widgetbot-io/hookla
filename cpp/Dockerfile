# ---- Build stage -----------------------------------------------------------
FROM debian:bookworm-slim AS builder

RUN apt-get update && apt-get install -y --no-install-recommends \
    cmake ninja-build clang-17 \
    libssl-dev \
    libpq-dev libpqxx-dev \
    pkg-config git ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ENV CC=clang-17 CXX=clang++-17

WORKDIR /src
COPY . .

RUN cmake -B build -G Ninja \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_CXX_COMPILER=clang++-17 \
    && cmake --build build --parallel

# ---- Runtime stage ---------------------------------------------------------
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl3 libpq5 libpqxx-7.7 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /src/build/hookla /usr/local/bin/hookla

ENV APP_PORT=8080 \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=hookla \
    DB_USER=hookla \
    DB_SSLMODE=disable

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/hookla"]
