package com.agi.dogapi.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {

    protected static RequestSpecification spec;

    protected static final String IMG_PATTERN =
            "^https://images\\.dog\\.ceo/breeds/.+\\.(jpg|jpeg|png)$";

    @BeforeAll
    static void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        spec =
                new RequestSpecBuilder()
                        .setBaseUri(resolveBaseUri())
                        .setBasePath("/api")
                        .setAccept(ContentType.JSON)
                        .build();
    }

    // Precedência: -DbaseUri > environments/<env>.properties > padrão.
    private static String resolveBaseUri() {
        String override = System.getProperty("baseUri");
        if (override != null && !override.isBlank()) {
            return override;
        }
        String env = System.getProperty("env", "dev");
        try (InputStream in =
                BaseApiTest.class.getResourceAsStream("/environments/" + env + ".properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String baseUri = props.getProperty("baseUri");
                if (baseUri != null && !baseUri.isBlank()) {
                    return baseUri.trim();
                }
            }
        } catch (Exception ignored) {
            // Sem arquivo do ambiente: cai no padrão.
        }
        return "https://dog.ceo";
    }
}
