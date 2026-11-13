package com.parag.ecom.tests;

import com.parag.ecom.api.AuthApi;
import com.parag.ecom.api.OrderApi;
import com.parag.ecom.api.ProductApi;
import com.parag.ecom.models.CreateOrderResponse;
import com.parag.ecom.models.LoginResponse;
import com.parag.ecom.models.OrderDetails;
import com.parag.ecom.models.OrderDetailsResponse;
import com.parag.ecom.models.OrderRequest;
import com.parag.ecom.models.Product;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

@Epic("E-commerce API")
@Feature("End to end")
public class EndToEndTest extends BaseTest {
    private TestData data;

    @Test
    @Description("TC25: login, add product, order it, verify, delete order, delete product; nothing left behind")
    @Severity(SeverityLevel.BLOCKER)
    public void purchaseFlow() {                // TC25
        // Logs in again (not the shared suite token) so the whole chain is covered
        LoginResponse login = Allure.step("Login", () -> AuthApi.loginAsTestUser()
            .then()
                .statusCode(200)
                .extract().as(LoginResponse.class));
        String e2eToken = login.getToken();
        data = new TestData(e2eToken, login.getUserId());

        String productId = Allure.step("Add product", () -> {
            String id = ProductApi.addProduct(e2eToken, Product.valid(login.getUserId(), TestData.PRODUCT_IMAGE))
                .then()
                    .statusCode(201)
                    .extract().jsonPath().getString("productId");
            // Tracked right away so cleanup runs even if a later step fails
            data.trackProduct(id);
            assertNotNull(id);
            return id;
        });

        String orderId = Allure.step("Place order", () -> {
            CreateOrderResponse res = OrderApi.createOrder(e2eToken, OrderRequest.of("India", productId))
                .then()
                    .statusCode(201)
                    .extract().as(CreateOrderResponse.class);
            data.trackOrders(res.getOrders());
            return res.getOrders().get(0);
        });

        Allure.step("Verify order details", () -> {
            OrderDetails order = OrderApi.getOrderDetails(e2eToken, orderId)
                .then()
                    .statusCode(200)
                    .extract().as(OrderDetailsResponse.class).getData();
            assertEquals(order.getProductOrderedId(), productId);
            assertEquals(order.getCountry(), "India");
        });

        Allure.step("Delete order and confirm it is gone", () -> {
            OrderApi.deleteOrder(e2eToken, orderId).then().statusCode(200);
            data.forgetOrder(orderId);
            OrderApi.getOrderDetails(e2eToken, orderId).then().statusCode(400);
        });

        Allure.step("Delete product and confirm it is gone", () -> {
            ProductApi.deleteProduct(e2eToken, productId).then().statusCode(200);
            data.forgetProduct(productId);
            List<String> ids = ProductApi.getAllProducts(e2eToken)
                .then()
                    .statusCode(200)
                    .extract().jsonPath().getList("data._id");
            assertFalse(ids.contains(productId), "Product still listed after delete: " + productId);
        });
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        if (data != null) {
            data.cleanUp();
        }
    }
}
