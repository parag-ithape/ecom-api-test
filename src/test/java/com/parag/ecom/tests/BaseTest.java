package com.parag.ecom.tests;

import com.parag.ecom.api.AuthApi;
import com.parag.ecom.models.LoginResponse;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

@Listeners(TestLogListener.class)
public class BaseTest {
    private static final int LOGIN_ATTEMPTS = 3;

    protected static String token;
    protected static String userId;

    // Logs in once per suite so every test can reuse the token.
    // Retried on network errors, because if this fails every test is skipped.
    @BeforeSuite
    public void loginOnce() {
        for (int attempt = 1; ; attempt++) {
            try {
                LoginResponse login = AuthApi.loginAsTestUser()
                    .then()
                        .statusCode(200)
                        .extract().as(LoginResponse.class);

                token = login.getToken();
                userId = login.getUserId();
                return;
            } catch (RuntimeException e) {
                if (attempt >= LOGIN_ATTEMPTS || !NetworkRetry.isNetworkError(e)) {
                    throw e;
                }
                System.out.println(">>> RETRY suite login (" + attempt + "/" + (LOGIN_ATTEMPTS - 1)
                        + ") after network error: " + e);
            }
        }
    }
}
