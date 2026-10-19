package com.network.service;

import com.network.model.AuthSession;
import io.micronaut.cache.CacheManager;
import io.micronaut.cache.SyncCache;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RedisSessionService {
    private static final Logger LOG = LoggerFactory.getLogger(RedisSessionService.class);
    public static final String CACHE_NAME = "peermesh-sessions";

    private final CacheManager<Object> cacheManager;
    private SyncCache<Object> sessionCache;
    private final Map<String, AuthSession> inMemoryFallback = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = false;

    public RedisSessionService(CacheManager<Object> cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        try {
            this.sessionCache = cacheManager.getCache(CACHE_NAME);
            if (this.sessionCache != null) {
                // Perform quick health probe
                sessionCache.put("__probe__", "ok");
                Optional<String> probe = sessionCache.get("__probe__", String.class);
                sessionCache.invalidate("__probe__");
                if (probe.isPresent()) {
                    this.redisAvailable = true;
                    LOG.info("Micronaut Cache [{}] backed by Redis Lettuce is connected and active.", CACHE_NAME);
                }
            }
        } catch (Exception e) {
            this.redisAvailable = false;
            LOG.warn("Redis is not running on host/port. Micronaut Cache is using high-performance in-memory fallback: {}", e.getMessage());
        }
    }

    public boolean isRedisAvailable() {
        if (!redisAvailable && sessionCache != null) {
            try {
                sessionCache.put("__probe__", "ok");
                Optional<String> probe = sessionCache.get("__probe__", String.class);
                sessionCache.invalidate("__probe__");
                if (probe.isPresent()) {
                    this.redisAvailable = true;
                }
            } catch (Exception ignored) {}
        }
        return redisAvailable;
    }

    public void saveSession(AuthSession session, long ttlSeconds) {
        if (session == null || session.sessionId() == null) return;

        if (isRedisAvailable() && sessionCache != null) {
            try {
                sessionCache.put(session.sessionId(), session);
                LOG.debug("Saved session to Micronaut Redis cache: {}", session.sessionId());
            } catch (Exception e) {
                LOG.warn("Failed writing session to Micronaut Redis cache, writing to memory fallback: {}", e.getMessage());
                redisAvailable = false;
            }
        }

        inMemoryFallback.put(session.sessionId(), session);
    }

    public Optional<AuthSession> getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }

        if (isRedisAvailable() && sessionCache != null) {
            try {
                Optional<AuthSession> cached = sessionCache.get(sessionId, AuthSession.class);
                if (cached.isPresent()) {
                    return cached;
                }
            } catch (Exception e) {
                LOG.warn("Failed reading from Micronaut Redis cache, checking in-memory fallback: {}", e.getMessage());
                redisAvailable = false;
            }
        }

        return Optional.ofNullable(inMemoryFallback.get(sessionId));
    }

    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;

        if (isRedisAvailable() && sessionCache != null) {
            try {
                sessionCache.invalidate(sessionId);
            } catch (Exception e) {
                LOG.warn("Failed to invalidate session in Micronaut Redis cache: {}", e.getMessage());
            }
        }

        inMemoryFallback.remove(sessionId);
    }
}
