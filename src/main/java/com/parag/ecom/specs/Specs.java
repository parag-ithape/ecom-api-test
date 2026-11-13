package com.parag.ecom.specs;

import com.parag.ecom.config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Set;

public final class Specs {
    public static final String AUTH_HEADER = "Authorization";

    // The shared demo can stall; fail a request after 30 s instead of hanging forever.
    // The token header is masked in console logs and in the Allure report.
    private static final RestAssuredConfig CONFIG = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", 10_000)
                    .setParam("http.socket.timeout", 30_000))
            .logConfig(LogConfig.logConfig()
                    .blacklistHeader(AUTH_HEADER));

    private Specs() {
    }

    // Base URI, timeouts, full request/response in the console and in the Allure report
    public static RequestSpecification base() {
        return baseBuilder()
                // The token mask is passed here too; the console logger does not pick it up from CONFIG
                .addFilter(new RequestLoggingFilter(LogDetail.ALL, true, System.out, true, Set.of(AUTH_HEADER)))
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .build();
    }

    // For calls that carry the real password: console shows only method, URL and status
    // (no password, no token), and nothing goes to the report
    public static RequestSpecification baseNotReported() {
        return baseBuilder()
                .addFilter(new RequestLoggingFilter(LogDetail.METHOD))
                .addFilter(new RequestLoggingFilter(LogDetail.URI))
                .addFilter(new ResponseLoggingFilter(LogDetail.STATUS))
                .build();
    }

    public static RequestSpecification json() {
        return new RequestSpecBuilder()
                .addRequestSpecification(base())
                .setContentType(ContentType.JSON)
                .build();
    }

    public static RequestSpecification auth(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(base())
                .addHeader(AUTH_HEADER, token)
                .build();
    }

    public static RequestSpecification authJson(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(auth(token))
                .setContentType(ContentType.JSON)
                .build();
    }

    private static RequestSpecBuilder baseBuilder() {
        return new RequestSpecBuilder()
                .setBaseUri(Config.baseUri())
                .setBasePath("/api/ecom")
                .setConfig(CONFIG);
    }
}
