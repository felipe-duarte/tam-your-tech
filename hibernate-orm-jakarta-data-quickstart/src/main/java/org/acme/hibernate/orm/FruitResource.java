package org.acme.hibernate.orm;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import jakarta.data.Order;
import jakarta.data.Sort;

@Path("fruits")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

    @Inject
    FruitRepository repository;

    @Inject 
    OpenTelemetry openTelemetry;

    private LongCounter requestCounter;

    @PostConstruct
    void initMetrics() {
        Meter meter = openTelemetry.getMeter("org.acme.hibernate"); 
        this.requestCounter = meter.counterBuilder("app_fruits_requests_total")
            .setDescription("Tracks total fruit resource API requests")
            .setUnit("1")
            .build();
    }

    @GET
    public List<Fruit> get() {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "get_fruits"));

        List<Fruit> fruits = repository.findAll(Order.by(Sort.asc("name"))).toList();

        // Safely enrich the automatically created HTTP server span
        Span.current().setAttribute("app.fruits.count", fruits.size());
        return fruits;
    }

    @GET
    @Path("{id}")
    public Fruit getSingle(@PathParam("id") Integer id) {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "get_single"));
        Span.current().setAttribute("app.fruit.id", id);

        return repository.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Fruit with id of %d does not exist.".formatted(id), 404));
    }

    @POST
    @Transactional
    public Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "create"));

        repository.insert(fruit);

        Span.current().setAttribute("app.fruit.name", fruit.getName());
        return Response.ok(fruit).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Fruit update(@PathParam("id") Integer id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "update"));
        Span.current().setAttribute("app.fruit.id", id);

        repository.update(id, fruit.getName());
        return fruit;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("action"), "delete"));
        Span.current().setAttribute("app.fruit.id", id);

        repository.delete(id);
        return Response.status(204).build();
    }
}

