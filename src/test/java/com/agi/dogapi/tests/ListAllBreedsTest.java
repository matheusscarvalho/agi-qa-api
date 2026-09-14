package com.agi.dogapi.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.agi.dogapi.clients.DogApiClient;
import com.agi.dogapi.models.BreedsListResponse;
import com.agi.dogapi.support.BaseApiTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("GET /breeds/list/all")
@Tag("regression")
@Tag("api")
class ListAllBreedsTest extends BaseApiTest {

    private DogApiClient api;

    @BeforeEach
    void init() {
        api = new DogApiClient(spec);
    }

    @Test
    @DisplayName("contrato da resposta: 200, JSON, schema e tipos")
    @Tag("smoke")
    @Tag("contract")
    void contratoDaResposta() {
        Response response = api.listAllBreeds();

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", not(anEmptyMap()))
                .body(matchesJsonSchemaInClasspath("schemas/breeds-list.schema.json"));

        BreedsListResponse dto = response.as(BreedsListResponse.class);
        assertThat(dto.status, equalTo("success"));
        assertThat(dto.message, not(anEmptyMap()));
    }

    @Test
    @DisplayName("contém raças conhecidas e sub-raças coerentes")
    void deveConterRacasConhecidas() {
        Response response = api.listAllBreeds();

        BreedsListResponse dto = response.as(BreedsListResponse.class);
        assertThat(dto.message.keySet(), hasItems("hound", "bulldog", "retriever"));

        List<String> bulldog = dto.message.get("bulldog");
        assertThat(bulldog, hasItems("boston", "english", "french"));
    }
}
