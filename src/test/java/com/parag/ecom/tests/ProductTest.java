package com.parag.ecom.tests;

import com.parag.ecom.api.ProductApi;
import com.parag.ecom.models.Product;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

@Epic("E-commerce API")
@Feature("Product")
public class ProductTest extends BaseTest {
    private static final File PRODUCT_IMAGE = new File("src/test/resources/images/product.png");

    // Products created by a test; deleted after each test so the shared demo stays clean
    private final List<String> createdProductIds = new ArrayList<>();

    @Test
    @Description("TC07: add product with valid multipart form and image returns 201 and a productId")
    @Severity(SeverityLevel.BLOCKER)
    public void addProductWithValidData() {     // TC07
        Response res = ProductApi.addProduct(token, Product.valid(userId, PRODUCT_IMAGE))
            .then()
                .statusCode(201)
                .extract().response();

        String productId = res.jsonPath().getString("productId");
        createdProductIds.add(productId);
        assertNotNull(productId);
        assertEquals(res.jsonPath().getString("message"), "Product Added Successfully");
    }

    @Test
    @Description("TC08: add product with no token returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void addProductWithoutToken() {      // TC08
        Response res = ProductApi.addProduct(null, Product.valid(userId, PRODUCT_IMAGE))
            .then()
                .statusCode(401)
                .extract().response();

        assertEquals(res.jsonPath().getString("message"), "Access denied. No token provided.");
    }

    @Test
    @Description("TC09: add product with tampered token returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void addProductWithTamperedToken() { // TC09
        Response res = ProductApi.addProduct(token + "tampered", Product.valid(userId, PRODUCT_IMAGE))
            .then()
                .statusCode(401)
                .extract().response();

        assertEquals(res.jsonPath().getString("message"), "Session Timeout");
    }

    @Test
    @Description("TC12: get all products after adding one contains the new productId")
    @Severity(SeverityLevel.NORMAL)
    public void getAllProductsContainsNewProduct() {    // TC12
        String productId = addProduct();

        List<String> ids = ProductApi.getAllProducts(token)
            .then()
                .statusCode(200)
                .extract().jsonPath().getList("data._id");

        assertTrue(ids.contains(productId), "New product not in list: " + productId);
    }

    @Test
    @Description("TC13: delete own product returns 200")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteOwnProduct() {            // TC13
        String productId = addProduct();

        Response res = ProductApi.deleteProduct(token, productId)
            .then()
                .statusCode(200)
                .extract().response();
        createdProductIds.remove(productId);

        assertEquals(res.jsonPath().getString("message"), "Product Deleted Successfully");
    }

    @Test
    @Description("TC14: delete a productId that does not exist returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void deleteNonExistentProduct() {    // TC14
        String productId = addProduct();
        ProductApi.deleteProduct(token, productId).then().statusCode(200);
        createdProductIds.remove(productId);

        Response res = ProductApi.deleteProduct(token, productId)
            .then()
                .statusCode(400)
                .extract().response();

        // "foundd" is the API's own spelling
        assertEquals(res.jsonPath().getString("message"), "Product not foundd");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        for (String productId : createdProductIds) {
            ProductApi.deleteProduct(token, productId);
        }
        createdProductIds.clear();
    }

    private String addProduct() {
        String productId = ProductApi.addProduct(token, Product.valid(userId, PRODUCT_IMAGE))
            .then()
                .statusCode(201)
                .extract().jsonPath().getString("productId");
        createdProductIds.add(productId);
        return productId;
    }
}
