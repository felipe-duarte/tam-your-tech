package com.redhat.techlab.catalog.service;

import java.time.Duration;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CacheService {

    private static final long DEFAULT_TTL_SECONDS = 300;
    private static final String KEY_PREFIX = "product:";

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;

    @Inject
    public CacheService(RedisDataSource redisDataSource) {
        this.valueCommands = redisDataSource.value(String.class);
        this.keyCommands = redisDataSource.key();
    }

    public String get(String key) {
        return valueCommands.get(KEY_PREFIX + key);
    }

    public void set(String key, String value) {
        set(key, value, DEFAULT_TTL_SECONDS);
    }

    public void set(String key, String value, long ttlSeconds) {
        valueCommands.set(KEY_PREFIX + key, value, new SetArgs().ex(Duration.ofSeconds(ttlSeconds)));
    }

    public void delete(String key) {
        keyCommands.del(KEY_PREFIX + key);
    }

    public boolean exists(String key) {
        return keyCommands.exists(KEY_PREFIX + key);
    }
}
