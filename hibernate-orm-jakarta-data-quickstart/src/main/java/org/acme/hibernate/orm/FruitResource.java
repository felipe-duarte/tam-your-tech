package org.acme.hibernate.orm;

import java.util.List;

import org.jboss.logging.Logger;

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.micrometer.core.instrument.MeterRegistry; 
import io.opentelemetry.api.trace.Span; 
import io.opentelemetry.instrumentation.annotations.WithSpan;


@Path("fruits")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class FruitResource {

	@Inject
	FruitRepository repository;

	@Inject MeterRegistry registry;

	@GET
	@WithSpan("get-fruits")
	public List<Fruit> get() {
		registry.counter("app.fruits.requests.total", "action", "get_fruits").increment();
		Span.current().setAttribute("app.fruits.count", fruits.size());
		return repository.findAll( Order.by( Sort.asc( Fruit_.NAME ) ) ).toList();
	}

	@GET
	@Path("{id}")
	@WithSpan("get-fruit")	
	public Fruit getSingle(Integer id) {
		return repository.findById( id )
				.orElseThrow( () -> new WebApplicationException( "Fruit with id of %d does not exist.".formatted( id ), 404 ) );
	}

	@POST
	@Transactional
	@WithSpan("create-fruit")
	public Response create(Fruit fruit) {
		if ( fruit.getId() != null ) {
			throw new WebApplicationException( "Id was invalidly set on request.", 422 );
		}

		repository.insert( fruit );
		registry.counter("app.fruits.requests.total", "action", "create").increment();
		return Response.ok( fruit ).status( 201 ).build();
	}

	@PUT
	@Path("{id}")
	@Transactional
	@WithSpan("update-fruit")
	public Fruit update(Integer id, Fruit fruit) {
		if ( fruit.getName() == null ) {
			throw new WebApplicationException( "Fruit Name was not set on request.", 422 );
		}

		repository.update( id, fruit.getName() );
		registry.counter("app.fruits.requests.total", "action", "update").increment();
		return fruit;
	}

	@DELETE
	@Path("{id}")
	@Transactional
	@WithSpan("delete-fruit")
	public Response delete(Integer id) {
		repository.delete( id );
		registry.counter("app.fruits.requests.total", "action", "delete").increment();
		return Response.status( 204 ).build();
	}
}
