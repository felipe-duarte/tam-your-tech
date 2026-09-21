package com.redhat.techlab.catalog.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;

@Readiness
@ApplicationScoped
public class RedisHealthCheck implements HealthCheck {

    private final ValueCommands<String, String> commands;

    @Inject
    public RedisHealthCheck(RedisDataSource redisDataSource) {
        this.commands = redisDataSource.value(String.class);
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Redis connection");
        try {
            // Attempt a read operation to verify connectivity
            commands.get("health-check");
            builder.up();
        } catch (Exception e) {
            builder.down().withData("error", e.getMessage());
        }
        return builder.build();
    }
}
