package com.agi.dogapi.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.agi.dogapi.clients.DogApiClient;
import com.agi.dogapi.models.SingleImageResponse;
import com.agi.dogapi.support.BaseApiTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("GET /breeds/image/random")
@Tag("regression")
@Tag("api")
class RandomImageTest extends BaseApiTest {

    private DogApiClient api;

    @BeforeEach
    void init() {
        api = new DogApiClient(spec);
    }

    @Test
    @DisplayName("contrato da resposta: 200, JSON, schema e URL válida")
    @Tag("smoke")
    @Tag("contract")
    void contratoDaResposta() {
        Response response = api.randomImage();

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", matchesPattern(IMG_PATTERN))
                .body(matchesJsonSchemaInClasspath("schemas/random-image.schema.json"));

        SingleImageResponse dto = response.as(SingleImageResponse.class);
        assertThat(dto.status, equalTo("success"));
        assertThat(dto.message, matchesPattern(IMG_PATTERN));
    }

    @Test
    @DisplayName("chamadas sucessivas retornam imagens variadas")
    void deveVariarEntreChamadas() {
        Set<String> urls = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            urls.add(api.randomImage().jsonPath().getString("message"));
        }
        assertThat("esperava mais de uma URL distinta em 10 chamadas",
                urls.size(), greaterThan(1));
    }
}
