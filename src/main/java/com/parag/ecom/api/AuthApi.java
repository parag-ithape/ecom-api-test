package com.parag.ecom.api;

import com.parag.ecom.config.Config;
import com.parag.ecom.models.LoginRequest;
import com.parag.ecom.specs.Specs;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class AuthApi {
    private static final String LOGIN = "/auth/login";

    private AuthApi() {
    }

    // Any body, attached to the report; use for negative cases with fake credentials
    public static Response login(LoginRequest body) {
        return given()
                .spec(Specs.json())
                .body(body)
            .when()
                .post(LOGIN);
    }

    // Logs in as the .env account; kept out of the report so the real password is never published
    public static Response loginAsTestUser() {
        return given()
                .spec(Specs.baseNotReported())
                .contentType(ContentType.JSON)
                .body(new LoginRequest(Config.email(), Config.password()))
            .when()
                .post(LOGIN);
    }
}
