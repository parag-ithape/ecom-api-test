package com.parag.ecom.api;

import com.parag.ecom.models.Product;
import com.parag.ecom.specs.Specs;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public final class ProductApi {
    private static final String ADD = "/product/add-product";
    private static final String GET_ALL = "/product/get-all-products";
    private static final String DELETE = "/product/delete-product/{productId}";

    private ProductApi() {
    }

    // Pass null as token to send no Authorization header
    public static Response addProduct(String token, Product product) {
        RequestSpecification req = given()
                .spec(token == null ? Specs.base() : Specs.auth(token))
                .multiPart("productName", product.getProductName())
                .multiPart("productAddedBy", product.getProductAddedBy())
                .multiPart("productCategory", product.getProductCategory())
                .multiPart("productSubCategory", product.getProductSubCategory())
                .multiPart("productPrice", product.getProductPrice())
                .multiPart("productDescription", product.getProductDescription())
                .multiPart("productFor", product.getProductFor());
        if (product.getProductImage() != null) {
            req.multiPart("productImage", product.getProductImage());
        }
        return req
            .when()
                .post(ADD);
    }

    public static Response getAllProducts(String token) {
        return given()
                .spec(Specs.authJson(token))
                .body(Map.of())
            .when()
                .post(GET_ALL);
    }

    public static Response deleteProduct(String token, String productId) {
        return given()
                .spec(Specs.auth(token))
                .pathParam("productId", productId)
            .when()
                .delete(DELETE);
    }
}
