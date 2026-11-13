package com.parag.ecom.tests;

import com.parag.ecom.api.AuthApi;
import com.parag.ecom.api.OrderApi;
import com.parag.ecom.api.ProductApi;
import com.parag.ecom.models.OrderRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.startsWith;

@Epic("E-commerce API")
@Feature("Contract")
public class ContractTest extends BaseTest {
    private static final String JSON = "application/json";

    private TestData data;

    @BeforeMethod
    public void setUpData() {
        data = new TestData(token, userId);
    }

    @Test
    @Description("TC26: login response matches its JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void loginMatchesSchema() {          // TC26
        AuthApi.loginAsTestUser()
            .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login-response.json"));
    }

    @Test
    @Description("TC26: create-order response matches its JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void createOrderMatchesSchema() {    // TC26
        String productId = data.createProduct();

        Response res = OrderApi.createOrder(token, OrderRequest.of("India", productId));
        data.trackOrders(res.jsonPath().getList("orders"));

        res.then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/create-order-response.json"));
    }

    @Test
    @Description("TC26: order-details response matches its JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void orderDetailsMatchesSchema() {   // TC26
        String orderId = data.createOrder("India", data.createProduct());

        OrderApi.getOrderDetails(token, orderId)
            .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/order-details-response.json"));
    }

    @Test
    @Description("TC27: JSON responses (login, product list, order details) have Content-Type application/json")
    @Severity(SeverityLevel.MINOR)
    public void jsonResponsesHaveJsonContentType() {    // TC27
        AuthApi.loginAsTestUser()
            .then()
                .header("Content-Type", startsWith(JSON));

        ProductApi.getAllProducts(token)
            .then()
                .header("Content-Type", startsWith(JSON));

        String orderId = data.createOrder("India", data.createProduct());
        OrderApi.getOrderDetails(token, orderId)
            .then()
                .header("Content-Type", startsWith(JSON));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        data.cleanUp();
    }
}
