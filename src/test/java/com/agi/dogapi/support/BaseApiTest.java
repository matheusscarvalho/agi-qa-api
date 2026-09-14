package com.agi.dogapi.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {

    protected static RequestSpecification spec;

    @BeforeAll
    static void setUp() {
        String baseUri = System.getProperty("baseUri", "https://dog.ceo");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        spec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setBasePath("/api")
                .setAccept(ContentType.JSON)
                .build();
    }
}
