package com.redhat.techlab.catalog;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProductResourceTest {

    @Test
    public void testListProductsEndpoint() {
        given()
                .when().get("/api/v1/products")
                .then()
                .statusCode(200);
    }
}
