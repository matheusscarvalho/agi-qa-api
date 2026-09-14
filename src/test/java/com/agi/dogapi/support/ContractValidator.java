package com.agi.dogapi.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.jsv.JsonSchemaValidator;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;

// Dog API é GET-only: o contrato de envio é validado sobre os path params.
// O mesmo mecanismo serve para o body de um POST/PUT quando existir.
public final class ContractValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ContractValidator() {
    }

    public static void assertRequestMatches(Map<String, ?> payload, String schemaClasspath) {
        String json = serialize(payload);
        assertThat("Contrato da requisição violado (" + schemaClasspath + "): " + json,
                json, JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaClasspath));
    }

    // versão não-lançadora, para casos negativos
    public static boolean requestMatches(Map<String, ?> payload, String schemaClasspath) {
        try {
            assertRequestMatches(payload, schemaClasspath);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    private static String serialize(Map<String, ?> payload) {
        try {
            return MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao serializar payload", e);
        }
    }
}
