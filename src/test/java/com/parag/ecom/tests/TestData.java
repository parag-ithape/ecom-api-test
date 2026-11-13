package com.parag.ecom.tests;

import com.parag.ecom.api.OrderApi;
import com.parag.ecom.api.ProductApi;
import com.parag.ecom.models.CreateOrderResponse;
import com.parag.ecom.models.OrderRequest;
import com.parag.ecom.models.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Creates products and orders for a test and deletes them afterwards.
// Orders are deleted before products, since an order points at its product.
public class TestData {
    public static final File PRODUCT_IMAGE = new File("src/test/resources/images/product.png");

    private final String token;
    private final String userId;
    private final List<String> productIds = new ArrayList<>();
    private final List<String> orderIds = new ArrayList<>();

    public TestData(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public String createProduct() {
        String productId = ProductApi.addProduct(token, Product.valid(userId, PRODUCT_IMAGE))
            .then()
                .statusCode(201)
                .extract().jsonPath().getString("productId");
        productIds.add(productId);
        return productId;
    }

    public String createOrder(String country, String productId) {
        CreateOrderResponse res = OrderApi.createOrder(token, OrderRequest.of(country, productId))
            .then()
                .statusCode(201)
                .extract().as(CreateOrderResponse.class);
        String orderId = res.getOrders().get(0);
        orderIds.add(orderId);
        return orderId;
    }

    // Record ids created outside the helpers, so they still get cleaned up
    public void trackProduct(String productId) {
        if (productId != null) {
            productIds.add(productId);
        }
    }

    public void trackOrders(List<String> ids) {
        if (ids != null) {
            orderIds.addAll(ids);
        }
    }

    // Call after a test deleted something itself, so cleanup does not try again
    public void forgetOrder(String orderId) {
        orderIds.remove(orderId);
    }

    public void forgetProduct(String productId) {
        productIds.remove(productId);
    }

    public void cleanUp() {
        for (String orderId : orderIds) {
            OrderApi.deleteOrder(token, orderId);
        }
        for (String productId : productIds) {
            ProductApi.deleteProduct(token, productId);
        }
        orderIds.clear();
        productIds.clear();
    }
}
