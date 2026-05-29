#pragma once
#include <condition_variable>
#include <memory>
#include <mutex>
#include <queue>
#include <string>
#include <pqxx/pqxx>

// Thread-safe PostgreSQL connection pool.
// Callers acquire() a connection, use it synchronously, then release() it.
// DB methods run on a dedicated std::thread to avoid blocking the HTTP event loop.
class ConnectionPool {
public:
    ConnectionPool(const std::string& connStr, size_t poolSize) {
        for (size_t i = 0; i < poolSize; ++i)
            pool_.push(std::make_unique<pqxx::connection>(connStr));
    }

    std::unique_ptr<pqxx::connection> acquire() {
        std::unique_lock lock(mutex_);
        cv_.wait(lock, [this] { return !pool_.empty(); });
        auto conn = std::move(pool_.front());
        pool_.pop();
        return conn;
    }

    void release(std::unique_ptr<pqxx::connection> conn) {
        std::lock_guard lock(mutex_);
        pool_.push(std::move(conn));
        cv_.notify_one();
    }

private:
    std::queue<std::unique_ptr<pqxx::connection>> pool_;
    std::mutex mutex_;
    std::condition_variable cv_;
};
