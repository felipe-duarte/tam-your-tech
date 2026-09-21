package com.redhat.techlab.catalog.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import com.redhat.techlab.catalog.repository.ProductRepository;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    Instance<ProductRepository> productRepositoryInstance;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Database connection");
        try {
            productRepositoryInstance.get().listAll();
            builder.up();
        } catch (Exception e) {
            builder.down().withData("error", e.getMessage());
        }
        return builder.build();
    }
}
