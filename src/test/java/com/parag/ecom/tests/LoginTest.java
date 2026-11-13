package com.parag.ecom.tests;

import com.parag.ecom.api.AuthApi;
import com.parag.ecom.config.Config;
import com.parag.ecom.models.LoginRequest;
import com.parag.ecom.models.LoginResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.*;

@Epic("E-commerce API")
@Feature("Auth")
public class LoginTest extends BaseTest {
    private static final long MAX_RESPONSE_MS = 2000;
    private static final int SAMPLES = 3;

    @Test
    @Description("TC01: login with valid credentials returns a token and userId")
    @Severity(SeverityLevel.BLOCKER)
    public void loginWithValidCredentials() {   // TC01
        LoginResponse res = AuthApi.loginAsTestUser()
            .then()
                .statusCode(200)
                .extract().as(LoginResponse.class);

        assertNotNull(res.getToken());
        assertFalse(res.getToken().isEmpty());
        assertNotNull(res.getUserId());
        assertEquals(res.getMessage(), "Login Successfully");
    }

    @Test
    @Description("TC02: login with wrong password returns 400 and no token")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithWrongPassword() {      // TC02
        LoginResponse res = AuthApi.login(new LoginRequest(Config.email(), "wrong123"))
            .then()
                .statusCode(400)
                .extract().as(LoginResponse.class);

        assertEquals(res.getMessage(), "Incorrect email or password.");
        assertNull(res.getToken());
    }

    @Test
    @Description("TC03: login with unregistered email returns 400 and an error message")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithUnregisteredEmail() {  // TC03
        String email = "not-registered-" + System.currentTimeMillis() + "@example.com";
        LoginResponse res = AuthApi.login(new LoginRequest(email, "Abc@12345"))
            .then()
                .statusCode(400)
                .extract().as(LoginResponse.class);

        assertEquals(res.getMessage(), "Incorrect email or password.");
        assertNull(res.getToken());
    }

    @Test
    @Description("TC04: login with missing userEmail field returns 400 and a validation message")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithMissingEmail() {       // TC04
        LoginResponse res = AuthApi.login(new LoginRequest(null, "Abc@12345"))
            .then()
                .statusCode(400)
                .extract().as(LoginResponse.class);

        assertEquals(res.getMessage(), "Email is required");
    }

    @Test
    @Description("TC05: login with empty body returns 400")
    @Severity(SeverityLevel.MINOR)
    public void loginWithEmptyBody() {          // TC05
        LoginResponse res = AuthApi.login(new LoginRequest())
            .then()
                .statusCode(400)
                .extract().as(LoginResponse.class);

        assertEquals(res.getMessage(), "Email is required");
    }

    @Test
    @Description("TC06: login responds in under 2 seconds (median of 3 calls)")
    @Severity(SeverityLevel.MINOR)
    public void loginResponseTime() {           // TC06
        // The shared demo has random slow spikes, so one slow call is not a failure;
        // the median of 3 calls must still be under the limit
        long[] times = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            times[i] = AuthApi.loginAsTestUser()
                .then()
                    .statusCode(200)
                    .extract().time();
        }
        Arrays.sort(times);
        long median = times[SAMPLES / 2];
        System.out.println("Login response times (ms): " + Arrays.toString(times) + ", median: " + median);

        assertTrue(median < MAX_RESPONSE_MS,
                "Median login time " + median + " ms is not under " + MAX_RESPONSE_MS + " ms; all: " + Arrays.toString(times));
    }
}
