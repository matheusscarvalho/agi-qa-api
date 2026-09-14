package com.agi.dogapi.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.agi.dogapi.clients.DogApiClient;
import com.agi.dogapi.models.ImagesResponse;
import com.agi.dogapi.support.BaseApiTest;
import com.agi.dogapi.support.ContractValidator;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("GET /breed/{breed}/images")
@Tag("regression")
@Tag("api")
class BreedImagesTest extends BaseApiTest {

    private DogApiClient api;

    @BeforeEach
    void init() {
        api = new DogApiClient(spec);
    }

    @ParameterizedTest(name = "raça válida: {0}")
    @ValueSource(strings = {"hound", "bulldog", "poodle"})
    @DisplayName("raça válida: contrato de envio + contrato de resposta")
    @Tag("smoke")
    @Tag("contract")
    void deveRetornarImagensParaRacaValida(String breed) {
        ContractValidator.assertRequestMatches(
                Map.of("breed", breed), "schemas/request/breed-path-param.schema.json");

        Response response = api.imagesByBreed(breed);

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body(matchesJsonSchemaInClasspath("schemas/images-list.schema.json"));

        ImagesResponse dto = response.as(ImagesResponse.class);
        assertThat(dto.status, equalTo("success"));
        assertThat(dto.message, is(not(empty())));
        assertThat("toda imagem deve ser URL http(s) da própria raça",
                dto.message, everyItem(matchesPattern(IMG_PATTERN)));
    }

    @Test
    @DisplayName("contrato de envio rejeita raça fora do padrão antes da chamada")
    @Tag("contract")
    void contratoDeEnvioRejeitaRacaInvalida() {
        assertFalse(
                ContractValidator.requestMatches(
                        Map.of("breed", "Hound123"),
                        "schemas/request/breed-path-param.schema.json"),
                "raça com maiúsculas/dígitos viola o contrato de envio");

        assertFalse(
                ContractValidator.requestMatches(
                        Map.of("breed", ""),
                        "schemas/request/breed-path-param.schema.json"),
                "raça vazia viola o contrato de envio");
    }

    @Test
    @DisplayName("raça inexistente retorna 404 e contrato de erro")
    @Tag("exception")
    void deveRetornar404ParaRacaInexistente() {
        Response response = api.imagesByBreed("racainexistente123");

        response.then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", equalTo("error"))
                .body("code", equalTo(404))
                .body("message", containsStringIgnoringCase("not found"))
                .body(matchesJsonSchemaInClasspath("schemas/error.schema.json"));
    }

    @Test
    @DisplayName("nomes de raça são tratados sem distinção de caixa (HOUND == hound)")
    @Tag("alternative")
    void deveTratarRacaSemDistincaoDeCaixa() {
        List<String> lower = api.imagesByBreed("hound").jsonPath().getList("message");
        List<String> upper = api.imagesByBreed("HOUND").jsonPath().getList("message");

        assertThat(upper, is(not(empty())));
        assertThat("mesmo conjunto de imagens independente da caixa",
                upper, containsInAnyOrder(lower.toArray()));
    }
}
