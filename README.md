# E-commerce API Test Automation (REST Assured + Java)

[![API Tests](https://github.com/parag-ithape/ecom-api-test/actions/workflows/api-tests.yml/badge.svg)](https://github.com/parag-ithape/ecom-api-test/actions/workflows/api-tests.yml) · **[Latest report](https://parag-ithape.github.io/ecom-api-test/)**

API test automation framework for an e-commerce backend: authentication, product management and orders. It runs against the public practice app at [rahulshettyacademy.com/client](https://rahulshettyacademy.com/client).

Built to show how I approach API testing on a real project: a layered framework, negative and security cases (not just happy paths), self-cleaning test data, and reports a non-technical stakeholder can read.

## Tech stack

Java 17 · REST Assured 5 (+ JSON Schema Validator) · TestNG 7 · Jackson · Allure · Maven · dotenv-java · GitHub Actions

## What's covered

| ID | Area | Scenario | Type | Status |
| --- | --- | --- | --- | --- |
| TC01 | Auth | Login with valid credentials | Smoke | ✅ |
| TC02 | Auth | Login with wrong password | Negative | ✅ |
| TC03 | Auth | Login with unregistered email | Negative | ✅ |
| TC04 | Auth | Login with missing email field | Negative | ✅ |
| TC05 | Auth | Login with empty body | Negative | ✅ |
| TC06 | Auth | Login response time under 2 s (median of 3) | Performance sanity | ✅ |
| TC07 | Product | Add product (multipart form + image) | Smoke | ✅ |
| TC08 | Product | Add product with no token | Security | ✅ |
| TC09 | Product | Add product with tampered token | Security | ✅ |
| TC10 | Product | Add product without name | Negative | ⚠️ known defect: API returns 500 instead of 4xx |
| TC11 | Product | Add product with non-numeric price | Negative | ⚠️ known defect: API returns 500 instead of 4xx |
| TC12 | Product | New product appears in product list | Functional | ✅ |
| TC13 | Product | Delete own product | Functional | ✅ |
| TC14 | Product | Delete a product that no longer exists | Negative | ✅ |
| TC15 | Order | Create order for a valid product | Smoke | ✅ |
| TC16 | Order | Get order details by orderId | Functional | ✅ |
| TC17 | Order | Customer's order list contains the new order | Functional | ✅ |
| TC18 | Order | Create order with a productId that doesn't exist | Negative | ✅ |
| TC19 | Order | Create order with empty orders array | Negative | ⚠️ known defect: API accepts it (201) |
| TC20 | Order | Get order details with an orderId that doesn't exist | Negative | ✅ |
| TC21 | Order | Create order with no token | Security | ✅ |
| TC22 | Order | Delete order, then fetch it | Functional | ✅ |
| TC23 | Order | Read another user's order | Security | ⏭️ needs a second test account |
| TC24 | Order | Create orders for several countries (data-driven) | Functional | ✅ |
| TC25 | E2E | Login → add product → order → verify → delete order → delete product | E2E | ✅ |
| TC26 | Contract | JSON schema check on login, create-order, order-details | Contract | ✅ |
| TC27 | Contract | Content-Type header on JSON responses | Contract | ✅ |

### Defects found

| Where | Input | Expected | Actual |
| --- | --- | --- | --- |
| Add product (TC10) | No `productName` | 4xx + validation message | 500 with a raw Mongoose `ValidationError` |
| Add product (TC11) | `productPrice` = `"abc"` | 4xx + validation message | 500 with a raw Mongoose `ValidationError` |
| Create order (TC19) | `"orders": []` | 4xx | 201 "Order Placed Successfully" with no order created |
| Create order / order details | Malformed id such as `"abc"` | 4xx | 500 with a raw `CastError` |
| Customer order list (TC17) | – | `productOrderedId` is the product id | `productOrderedId` holds a date string (e.g. `"Fri Nov 13"`) |

Tests for the first three are written against the expected behaviour and disabled (`enabled = false`), so they can be switched on once the API is fixed.

   The full test plan, with expected results and priorities, is in [docs/test-plan.md](docs/test-plan.md).

## Project structure

```
src/main/java/com/parag/ecom
├── config/   Config.java       – reads BASE_URI and credentials from env vars or .env
├── specs/    Specs.java        – shared request specs: base URI, timeouts, logging, Allure, token masking
├── api/      AuthApi, ProductApi, OrderApi – one method per endpoint; tests never call REST Assured directly
└── models/   LoginRequest/Response, Product, OrderRequest, OrderItem,
              CreateOrderResponse, OrderDetails(Response) – request/response POJOs

src/test/java/com/parag/ecom/tests
├── BaseTest.java         – logs in once per suite and shares the token
├── LoginTest.java        – TC01–TC06
├── ProductTest.java      – TC07–TC14 (TC10–TC11 disabled: known defect)
├── OrderTest.java        – TC15–TC24 (TC19 disabled: known defect; TC23 not written yet)
├── EndToEndTest.java     – TC25, each step shown as an Allure step
├── ContractTest.java     – TC26–TC27
├── TestData.java         – creates products/orders for a test and deletes them afterwards
├── NetworkRetry.java     – retries only on network errors, never on failed assertions
└── TestLogListener.java  – readable start/pass/fail lines and a summary in the console

src/test/resources/schemas – JSON schemas for TC26
```

## Design decisions

- **API layer between tests and HTTP.** If an endpoint changes, only one method changes.
- **Secrets never leave the machine.** Credentials come from env vars or a gitignored `.env`. The token is masked in console logs and the Allure report, and the login call that carries the real password is kept out of the report entirely.
- **Self-cleaning data.** Every product and order a test creates is deleted in `@AfterMethod(alwaysRun = true)` (orders first, then products), so the shared demo stays clean even when a test fails.
- **Retries only where they're honest.** The demo server is shared and sometimes slow, so timeouts and dropped connections are retried (up to 2 times). A failed assertion is a real result and is never retried.
- **Exact assertions.** Negative tests assert the API's real status codes and messages, not just "not 200".

## How to run

**Prerequisites:** Java 17+, Maven 3.9+, and a free account on [rahulshettyacademy.com/client](https://rahulshettyacademy.com/client).

1. Clone the repo and create your `.env` from the template:
   ```
   cp .env.example .env
   ```
   Fill in `RSA_EMAIL` and `RSA_PASSWORD`. Real environment variables override `.env`, which is how CI passes them.

2. Run the tests:
   ```
   mvn clean test
   ```
   Run one class with `mvn test -Dtest=LoginTest`.

3. View the Allure report:
   ```
   mvn allure:report
   ```
   Then open `target/site/allure-maven-plugin/index.html`. On Windows, `report.cmd` does all three steps and opens the report.

## Notes

- The target is a public practice app, so occasional slowness or downtime is environmental, not a framework failure.
- No load testing is run against it. Performance testing lives in a separate repo against a locally hosted app.

## Author

**Parag Ithape** – Senior QA Engineer / SDET · [GitHub](https://github.com/parag-ithape)
