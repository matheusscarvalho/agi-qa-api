package com.agi.dogapi.clients;

import static io.restassured.RestAssured.given;

import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DogApiClient {

    private final RequestSpecification spec;

    public DogApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response listAllBreeds() {
        return given().spec(spec).when().get("/breeds/list/all");
    }

    public Response subBreeds(String breed) {
        return given().spec(spec).pathParam("breed", breed)
                .when().get("/breed/{breed}/list");
    }

    public Response imagesByBreed(String breed) {
        return given().spec(spec).pathParam("breed", breed)
                .when().get("/breed/{breed}/images");
    }

    public Response imagesBySubBreed(String breed, String subBreed) {
        return given().spec(spec).pathParam("breed", breed).pathParam("sub", subBreed)
                .when().get("/breed/{breed}/{sub}/images");
    }

    public Response randomImage() {
        return given().spec(spec).when().get("/breeds/image/random");
    }

    public Response randomImageByBreed(String breed) {
        return given().spec(spec).pathParam("breed", breed)
                .when().get("/breed/{breed}/images/random");
    }

    public Response randomImagesByBreed(String breed, int amount) {
        return given().spec(spec).pathParam("breed", breed).pathParam("amount", amount)
                .when().get("/breed/{breed}/images/random/{amount}");
    }

    public Response request(Method method, String path) {
        return given().spec(spec).when().request(method, path);
    }
}
