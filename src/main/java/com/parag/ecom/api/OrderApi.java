package com.parag.ecom.api;

import com.parag.ecom.models.OrderRequest;
import com.parag.ecom.specs.Specs;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class OrderApi {
    private static final String CREATE = "/order/create-order";
    private static final String DETAILS = "/order/get-orders-details";
    private static final String FOR_CUSTOMER = "/order/get-orders-for-customer/{userId}";
    private static final String DELETE = "/order/delete-order/{orderId}";

    private OrderApi() {
    }

    // Pass null as token to send no Authorization header
    public static Response createOrder(String token, OrderRequest body) {
        return given()
                .spec(token == null ? Specs.json() : Specs.authJson(token))
                .body(body)
            .when()
                .post(CREATE);
    }

    public static Response getOrderDetails(String token, String orderId) {
        return given()
                .spec(Specs.auth(token))
                .queryParam("id", orderId)
            .when()
                .get(DETAILS);
    }

    public static Response getOrdersForCustomer(String token, String userId) {
        return given()
                .spec(Specs.auth(token))
                .pathParam("userId", userId)
            .when()
                .get(FOR_CUSTOMER);
    }

    public static Response deleteOrder(String token, String orderId) {
        return given()
                .spec(Specs.auth(token))
                .pathParam("orderId", orderId)
            .when()
                .delete(DELETE);
    }
}
