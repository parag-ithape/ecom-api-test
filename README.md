# E-commerce API Test Automation (REST Assured + Java)

[![API Tests](https://github.com/parag-ithape/ecom-api-test/actions/workflows/api-tests.yml/badge.svg)](https://github.com/parag-ithape/ecom-api-test/actions/workflows/api-tests.yml) · **[Latest report](https://parag-ithape.github.io/ecom-api-test/)**

API test automation framework for an e-commerce backend: authentication, product management and (in progress) orders. It runs against the public practice app at [rahulshettyacademy.com/client](https://rahulshettyacademy.com/client).

Built to show how I approach API testing on a real project: a layered framework, negative and security cases (not just happy paths), self-cleaning test data, and reports a non-technical stakeholder can read.

## Tech stack

Java 17 · REST Assured 5 · TestNG 7 · Jackson · Allure · Maven · dotenv-java

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
| TC15–TC24 | Order | Create, view, list, delete orders + negative and security cases | Mixed | 🔜 |
| TC25 | E2E | Login → add product → order → verify → clean up | E2E | 🔜 |
| TC26–TC27 | Contract | JSON schema and header checks | Contract | 🔜 |

The full test plan, with expected results and priorities, is in [`docs/`](docs/).

## Project structure

```
src/main/java/com/parag/ecom
├── config/   Config.java       – reads BASE_URI and credentials from env vars or .env
├── specs/    Specs.java        – shared request specs: base URI, timeouts, logging, Allure, token masking
├── api/      AuthApi, ProductApi – one method per endpoint; tests never call REST Assured directly
└── models/   LoginRequest, LoginResponse, Product – request/response POJOs

src/test/java/com/parag/ecom/tests
├── BaseTest.java         – logs in once per suite and shares the token
├── LoginTest.java        – TC01–TC06
├── ProductTest.java      – TC07–TC14 (TC10–TC11 disabled: known defect)
├── NetworkRetry.java     – retries only on network errors, never on failed assertions
└── TestLogListener.java  – readable start/pass/fail lines and a summary in the console
```

## Design decisions

- **API layer between tests and HTTP.** If an endpoint changes, only one method changes.
- **Secrets never leave the machine.** Credentials come from env vars or a gitignored `.env`. The token is masked in console logs and the Allure report, and the login call that carries the real password is kept out of the report entirely.
- **Self-cleaning data.** Every product a test creates is deleted in `@AfterMethod(alwaysRun = true)`, so the shared demo stays clean even when a test fails.
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
