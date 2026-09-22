package org.acme.hibernate.orm;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.hibernate.Sort;
import org.hibernate.query.Order;

@Path("fruits")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class FruitResource {

    @Inject
    FruitRepository repository;

    @Inject
    Meter meter;

    @Inject
    Tracer tracer;

    private LongCounter requestCounter;

    @PostConstruct
    void initMetrics() {
        this.requestCounter = meter.counterBuilder("app_fruits_requests_total")
                .setDescription("Tracks total fruit resource API requests")
                .setUnit("1")
                .build();
    }

    @GET
    public List<Fruit> get() {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "get_fruits"));

        Span dbSpan = tracer.spanBuilder("repository.findAll").startSpan();
        List<Fruit> fruits;
        try (Scope scope = dbSpan.makeCurrent()) {
            fruits = repository.findAll(Order.by(Sort.asc(Fruit_.NAME))).toList();
        } finally {
            dbSpan.end();
        }

        Span.current().setAttribute("app.fruits.count", fruits.size());

        return fruits;
    }

    @GET
    @Path("{id}")
    public Fruit getSingle(@PathParam("id") Integer id) {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "get_single"));
        Span.current().setAttribute("app.fruit.id", id);

        Span dbSpan = tracer.spanBuilder("repository.findById").startSpan();
        try (Scope scope = dbSpan.makeCurrent()) {
            return repository.findById(id)
                    .orElseThrow(() -> new WebApplicationException(
                            "Fruit with id of %d does not exist.".formatted(id), 404));
        } finally {
            dbSpan.end();
        }
    }

    @POST
    public Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "create"));

        Span dbSpan = tracer.spanBuilder("repository.insert").startSpan();
        try (Scope scope = dbSpan.makeCurrent()) {
            repository.insert(fruit);
        } finally {
            dbSpan.end();
        }

        Span.current().setAttribute("app.fruit.name", fruit.getName());
        return Response.ok(fruit).status(201).build();
    }

    @PUT
    @Path("{id}")
    public Fruit update(@PathParam("id") Integer id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "update"));
        Span.current().setAttribute("app.fruit.id", id);

        Span dbSpan = tracer.spanBuilder("repository.update").startSpan();
        try (Scope scope = dbSpan.makeCurrent()) {
            repository.update(id, fruit.getName());
        } finally {
            dbSpan.end();
        }

        return fruit;
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Integer id) {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "delete"));
        Span.current().setAttribute("app.fruit.id", id);

        Span dbSpan = tracer.spanBuilder("repository.delete").startSpan();
        try (Scope scope = dbSpan.makeCurrent()) {
            repository.delete(id);
        } finally {
            dbSpan.end();
        }

        return Response.status(204).build();
    }
}
