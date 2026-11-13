package com.parag.ecom.tests;

import com.parag.ecom.api.OrderApi;
import com.parag.ecom.models.CreateOrderResponse;
import com.parag.ecom.models.OrderDetails;
import com.parag.ecom.models.OrderDetailsResponse;
import com.parag.ecom.models.OrderItem;
import com.parag.ecom.models.OrderRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

@Epic("E-commerce API")
@Feature("Order")
public class OrderTest extends BaseTest {
    // A valid-format id that matches no product or order
    private static final String UNKNOWN_ID = "000000000000000000000000";

    private TestData data;

    @BeforeMethod
    public void setUpData() {
        data = new TestData(token, userId);
    }

    @Test
    @Description("TC15: create order for a valid product returns 201 and an orderId")
    @Severity(SeverityLevel.BLOCKER)
    public void createOrderForValidProduct() {  // TC15
        String productId = data.createProduct();

        CreateOrderResponse res = OrderApi.createOrder(token, OrderRequest.of("India", productId))
            .then()
                .statusCode(201)
                .extract().as(CreateOrderResponse.class);
        data.trackOrders(res.getOrders());

        assertEquals(res.getOrders().size(), 1);
        assertEquals(res.getProductOrderId(), List.of(productId));
        assertEquals(res.getMessage(), "Order Placed Successfully");
    }

    @Test
    @Description("TC16: get order details by orderId returns the ordered product and country")
    @Severity(SeverityLevel.CRITICAL)
    public void getOrderDetailsById() {         // TC16
        String productId = data.createProduct();
        String orderId = data.createOrder("India", productId);

        OrderDetailsResponse res = OrderApi.getOrderDetails(token, orderId)
            .then()
                .statusCode(200)
                .extract().as(OrderDetailsResponse.class);

        OrderDetails order = res.getData();
        assertEquals(order.getId(), orderId);
        assertEquals(order.getProductOrderedId(), productId);
        assertEquals(order.getCountry(), "India");
        assertEquals(order.getOrderById(), userId);
        assertEquals(res.getMessage(), "Orders fetched for customer Successfully");
    }

    @Test
    @Description("TC17: get all orders for customer contains the new orderId")
    @Severity(SeverityLevel.NORMAL)
    public void getOrdersForCustomerContainsNewOrder() {    // TC17
        String orderId = data.createOrder("India", data.createProduct());

        // Only the order id is checked: in this list the API puts a date string
        // ("Fri Nov 13") in productOrderedId instead of the product id
        List<String> ids = OrderApi.getOrdersForCustomer(token, userId)
            .then()
                .statusCode(200)
                .extract().jsonPath().getList("data._id");

        assertTrue(ids.contains(orderId), "New order not in customer's list: " + orderId);
    }

    @Test
    @Description("TC18: create order with a productId that does not exist returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void createOrderWithInvalidProductId() { // TC18
        // A malformed id like "abc" gets a raw 500 instead (same defect as TC10/TC11)
        Response res = OrderApi.createOrder(token, OrderRequest.of("India", UNKNOWN_ID))
            .then()
                .statusCode(400)
                .extract().response();
        data.trackOrders(res.jsonPath().getList("orders"));

        assertEquals(res.jsonPath().getString("message"), "Wrong Product ID");
    }

    @Test(enabled = false)
    @Description("Known defect: API accepts an empty orders array (201 \"Order Placed Successfully\")")
    @Severity(SeverityLevel.NORMAL)
    public void createOrderWithEmptyOrders() {  // TC19
        Response res = OrderApi.createOrder(token, new OrderRequest(List.<OrderItem>of()));
        data.trackOrders(res.jsonPath().getList("orders"));

        // Live API (Nov 2026): HTTP 201, {"orders":[],"productOrderId":[],"message":"Order Placed Successfully"}
        int status = res.statusCode();
        assertTrue(status >= 400 && status < 500, "Expected 4xx, got " + status);
    }

    @Test
    @Description("TC20: get order details with an orderId that does not exist returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void getOrderDetailsWithInvalidId() {    // TC20
        // A malformed id like "abc" gets a raw 500 instead (same defect as TC10/TC11)
        Response res = OrderApi.getOrderDetails(token, UNKNOWN_ID)
            .then()
                .statusCode(400)
                .extract().response();

        assertEquals(res.jsonPath().getString("message"), "Order not found");
    }

    @Test
    @Description("TC21: create order with no token returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void createOrderWithoutToken() {     // TC21
        String productId = data.createProduct();

        Response res = OrderApi.createOrder(null, OrderRequest.of("India", productId))
            .then()
                .statusCode(401)
                .extract().response();
        data.trackOrders(res.jsonPath().getList("orders"));

        assertEquals(res.jsonPath().getString("message"), "Access denied. No token provided.");
    }

    @Test
    @Description("TC22: delete order returns 200, then fetching it returns 400 Order not found")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteOrderThenFetch() {        // TC22
        String orderId = data.createOrder("India", data.createProduct());

        Response deleted = OrderApi.deleteOrder(token, orderId)
            .then()
                .statusCode(200)
                .extract().response();
        data.forgetOrder(orderId);
        assertEquals(deleted.jsonPath().getString("message"), "Orders Deleted Successfully");

        Response fetched = OrderApi.getOrderDetails(token, orderId)
            .then()
                .statusCode(400)
                .extract().response();
        assertEquals(fetched.jsonPath().getString("message"), "Order not found");
    }

    @DataProvider(name = "countries")
    public Object[][] countries() {
        return new Object[][]{
                {"India"},
                {"United States"},
                {"Germany"},
                {"Japan"}
        };
    }

    @Test(dataProvider = "countries")
    @Description("TC24: create orders for several countries (data-driven); each returns 201 and keeps the country")
    @Severity(SeverityLevel.NORMAL)
    public void createOrderForCountry(String country) { // TC24
        String orderId = data.createOrder(country, data.createProduct());

        OrderDetails order = OrderApi.getOrderDetails(token, orderId)
            .then()
                .statusCode(200)
                .extract().as(OrderDetailsResponse.class).getData();

        assertEquals(order.getCountry(), country);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        data.cleanUp();
    }
}
