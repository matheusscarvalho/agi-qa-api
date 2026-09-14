package com.agi.dogapi.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.agi.dogapi.clients.DogApiClient;
import com.agi.dogapi.models.ErrorResponse;
import com.agi.dogapi.support.BaseApiTest;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Casos de exceção da Dog API")
@Tag("regression")
@Tag("api")
@Tag("exception")
class ExceptionFlowsTest extends BaseApiTest {

    private DogApiClient api;

    @BeforeEach
    void init() {
        api = new DogApiClient(spec);
    }

    @ParameterizedTest(name = "raça inválida: \"{0}\"")
    @ValueSource(strings = {"racainexistente123", "!!!", "123", "hound1"})
    @DisplayName("raça inválida/inexistente retorna 404 com contrato de erro")
    void racaInvalidaRetorna404(String breed) {
        Response response = api.imagesByBreed(breed);

        response.then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/error.schema.json"));

        ErrorResponse dto = response.as(ErrorResponse.class);
        assertThat(dto.status, equalTo("error"));
        assertThat(dto.code, equalTo(404));
        assertThat(dto.message.toLowerCase(), containsString("not found"));
    }

    @Test
    @DisplayName("sub-raça inexistente retorna 404 indicando sub-raça")
    void subRacaInexistenteRetorna404() {
        Response response = api.imagesBySubBreed("hound", "subracainexistente");

        response.then()
                .statusCode(404)
                .body(matchesJsonSchemaInClasspath("schemas/error.schema.json"));

        ErrorResponse dto = response.as(ErrorResponse.class);
        assertThat(dto.code, equalTo(404));
        assertThat(dto.message.toLowerCase(), containsString("sub breed"));
    }

    @Test
    @DisplayName("método não permitido (POST em endpoint GET) retorna 405")
    void metodoNaoPermitidoRetorna405() {
        Response response = api.request(Method.POST, "/breeds/list/all");
        response.then().statusCode(405);
    }

    @Test
    @DisplayName("rota inexistente retorna 404")
    void rotaInexistenteRetorna404() {
        Response response = api.request(Method.GET, "/breeds/list/all/inexistente");
        response.then().statusCode(404);
    }
}
