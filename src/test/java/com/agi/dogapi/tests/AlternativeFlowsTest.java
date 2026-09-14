package com.agi.dogapi.tests;

import com.agi.dogapi.clients.DogApiClient;
import com.agi.dogapi.models.ImagesResponse;
import com.agi.dogapi.models.SingleImageResponse;
import com.agi.dogapi.models.SubBreedsResponse;
import com.agi.dogapi.support.BaseApiTest;
import com.agi.dogapi.support.ContractValidator;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Fluxos alternativos da Dog API")
@Tag("regression")
@Tag("api")
@Tag("alternative")
class AlternativeFlowsTest extends BaseApiTest {

    private static final String IMG_PATTERN =
            "^https://images\\.dog\\.ceo/breeds/.+\\.(jpg|jpeg|png)$";

    private DogApiClient api;

    @BeforeEach
    void init() {
        api = new DogApiClient(spec);
    }

    @Test
    @DisplayName("GET /breed/{breed}/list retorna as sub-raças")
    void deveListarSubRacas() {
        Response response = api.subBreeds("hound");

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body(matchesJsonSchemaInClasspath("schemas/sub-breeds.schema.json"));

        SubBreedsResponse dto = response.as(SubBreedsResponse.class);
        assertThat(dto.message, hasItems("afghan", "basset", "english"));
    }

    @Test
    @DisplayName("raça sem sub-raças retorna lista vazia")
    void racaSemSubRacasRetornaListaVazia() {
        SubBreedsResponse dto = api.subBreeds("hound").as(SubBreedsResponse.class);
        assertThat(dto.message, is(notNullValue()));

        SubBreedsResponse afghanless = api.subBreeds("affenpinscher").as(SubBreedsResponse.class);
        assertThat(afghanless.message, is(empty()));
    }

    @Test
    @DisplayName("GET /breed/{breed}/{sub}/images: contrato de envio + resposta")
    void deveRetornarImagensDeSubRaca() {
        ContractValidator.assertRequestMatches(
                Map.of("breed", "hound", "subBreed", "afghan"),
                "schemas/request/subbreed-path-param.schema.json");

        Response response = api.imagesBySubBreed("hound", "afghan");

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body(matchesJsonSchemaInClasspath("schemas/images-list.schema.json"));

        ImagesResponse dto = response.as(ImagesResponse.class);
        assertThat(dto.message, is(not(empty())));
        assertThat(dto.message, everyItem(containsString("hound-afghan")));
    }

    @Test
    @DisplayName("GET /breed/{breed}/images/random/{n} retorna N imagens")
    void deveRetornarQuantidadeSolicitadaDeImagens() {
        int amount = 3;
        Response response = api.randomImagesByBreed("hound", amount);

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body(matchesJsonSchemaInClasspath("schemas/multiple-images.schema.json"));

        List<String> images = response.jsonPath().getList("message");
        assertThat(images, hasSize(amount));
        assertThat(images, everyItem(matchesPattern(IMG_PATTERN)));
    }

    @Test
    @DisplayName("GET /breed/{breed}/images/random retorna uma única imagem da raça")
    void deveRetornarUmaImagemAleatoriaDaRaca() {
        Response response = api.randomImageByBreed("hound");

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/random-image.schema.json"));

        SingleImageResponse dto = response.as(SingleImageResponse.class);
        assertThat(dto.message, matchesPattern(IMG_PATTERN));
        assertThat(dto.message, containsString("/hound"));
    }
}
