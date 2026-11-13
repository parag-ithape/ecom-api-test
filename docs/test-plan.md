# E-commerce API Test Plan – Rahul Shetty Academy Client App

Nov 4, 2026 · Parag Ithape

## Scope and approach

This plan covers API-level automation of the e-commerce backend behind
rahulshettyacademy.com/client: auth, products and orders, end to end. It
is written the way a client engagement would be scoped, so it doubles as
a portfolio piece for recruiters.

**Objectives**

- Prove the core purchase flow works through the API: login → add
  product → place order → view → delete.

- Catch contract breaks early with JSON schema checks on every key
  response.

- Prove the API rejects bad input and unauthorised calls (negative and
  security basics).

- Run headless in CI on every push, with a readable report.

**In scope:** REST endpoints for auth, product and order; status codes,
response body, schema, headers, basic response time.

**Out of scope:** UI testing (covered in the Playwright repo), load
testing, payment gateway, register/forgot-password flows (to avoid
creating junk users on a shared demo).

**Environment:** public demo at https://rahulshettyacademy.com, one
dedicated test account, credentials passed via environment variables /
CI secrets.

**Tools:** Java 17, REST Assured, TestNG, Jackson (POJOs),
json-schema-validator, Allure, Maven, GitHub Actions.

## Endpoints under test

Base path: https://rahulshettyacademy.com/api/ecom. All calls except
login need the token in the Authorization header (raw token, no Bearer
prefix).

| Area    | Method | Endpoint                                | Body / params                                                                                                                                       |
|---------|--------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| Auth    | POST   | /auth/login                             | JSON: userEmail, userPassword                                                                                                                       |
| Product | POST   | /product/add-product                    | Multipart form: productName, productAddedBy, productCategory, productSubCategory, productPrice, productDescription, productFor, productImage (file) |
| Product | POST   | /product/get-all-products               | JSON filter body                                                                                                                                    |
| Product | DELETE | /product/delete-product/{productId}     | Path param                                                                                                                                          |
| Order   | POST   | /order/create-order                     | JSON: orders\[{country, productOrderedId}\]                                                                                                         |
| Order   | GET    | /order/get-orders-details?id={orderId}  | Query param                                                                                                                                         |
| Order   | GET    | /order/get-orders-for-customer/{userId} | Path param                                                                                                                                          |
| Order   | DELETE | /order/delete-order/{orderId}           | Path param                                                                                                                                          |

## Test cases

27 cases, all automatable; 12 are P1 and form the smoke suite. For
negative cases the demo API's exact codes and messages aren't
documented, so record the actual response on the first run and lock the
assertion to it.

| ID   | Area     | Scenario                                                             | Type               | Priority | Expected result                                  |
|------|----------|----------------------------------------------------------------------|--------------------|----------|--------------------------------------------------|
| TC01 | Auth     | Login with valid credentials                                         | Smoke              | P1       | 200; token and userId non-empty; success message |
| TC02 | Auth     | Login with wrong password                                            | Negative           | P1       | 4xx; no token in body                            |
| TC03 | Auth     | Login with unregistered email                                        | Negative           | P2       | 4xx; error message                               |
| TC04 | Auth     | Login with missing userEmail field                                   | Negative           | P2       | 4xx; validation message                          |
| TC05 | Auth     | Login with empty body                                                | Negative           | P3       | 4xx                                              |
| TC06 | Auth     | Login response time                                                  | Performance sanity | P3       | Under 2 s                                        |
| TC07 | Product  | Add product with valid multipart form + image                        | Smoke              | P1       | 201; productId returned; success message         |
| TC08 | Product  | Add product with no token                                            | Security           | P1       | 401; product not created                         |
| TC09 | Product  | Add product with tampered token                                      | Security           | P1       | 401                                              |
| TC10 | Product  | Add product without productName                                      | Negative           | P2       | 4xx; validation message                          |
| TC11 | Product  | Add product with non-numeric price                                   | Negative           | P2       | 4xx, or log as defect if accepted                |
| TC12 | Product  | Get all products after adding one                                    | Functional         | P2       | 200; list contains new productId                 |
| TC13 | Product  | Delete own product                                                   | Functional         | P1       | 200; product gone from list                      |
| TC14 | Product  | Delete non-existent productId                                        | Negative           | P2       | 4xx                                              |
| TC15 | Order    | Create order for a valid product                                     | Smoke              | P1       | 201; orderId returned; schema valid              |
| TC16 | Order    | Get order details by orderId                                         | Functional         | P1       | 200; productOrderedId and country match request  |
| TC17 | Order    | Get all orders for customer                                          | Functional         | P2       | 200; list contains new orderId                   |
| TC18 | Order    | Create order with invalid productId                                  | Negative           | P2       | 4xx; no order created                            |
| TC19 | Order    | Create order with empty orders array                                 | Negative           | P2       | 4xx                                              |
| TC20 | Order    | Get order details with invalid orderId                               | Negative           | P2       | 4xx                                              |
| TC21 | Order    | Create order with no token                                           | Security           | P1       | 401                                              |
| TC22 | Order    | Delete order, then fetch it                                          | Functional         | P1       | Delete 200; fetch returns not found              |
| TC23 | Order    | Read another user's order (needs 2nd account)                        | Security           | P2       | Access denied                                    |
| TC24 | Order    | Create orders for several countries (data-driven)                    | Functional         | P2       | 201 for each row                                 |
| TC25 | E2E      | Login → add product → order → verify → delete order → delete product | E2E                | P1       | Every step passes; no test data left behind      |
| TC26 | Contract | Schema check on login, create-order, order-details                   | Contract           | P1       | Responses match JSON schemas                     |
| TC27 | Contract | Content-Type header on JSON responses                                | Contract           | P3       | application/json                                 |

## Automation strategy

Tests call a thin API layer, never raw REST Assured, so a changed
endpoint is fixed in one place.

**Framework layers**

- **Config:** base URL and credentials from a properties file,
  overridden by environment variables.

- **Specs:** shared RequestSpec/ResponseSpec (base URI, headers,
  logging, Allure filter).

- **API layer:** AuthApi, ProductApi, OrderApi — one method per
  endpoint, returns the Response.

- **Models:** POJOs for request and response bodies (Jackson).

- **Tests:** TestNG classes per area, grouped as smoke and regression;
  token fetched once per suite.

**Test data:** unique product names per run (timestamp suffix); every
test that creates data deletes it in @AfterMethod, so the shared demo
stays clean.

**CI and reporting:** GitHub Actions runs smoke on every push and full
regression nightly; Allure report published to GitHub Pages; credentials
stored as repo secrets.

**Build order**

1.  TC01 login + token extraction

2.  TC07, TC13 add/delete product (multipart)

3.  TC15, TC16, TC22 order flow with POJOs

4.  TC25 end-to-end chain

5.  Refactor into specs + API layer; add TC26 schema checks

6.  Negative and security cases (TC02–TC05, TC08–TC11, TC14, TC18–TC21)

7.  Data-driven TC24, CI pipeline, Allure on Pages, README

## What this shows clients and recruiters

| Audience                | What they look for                                              | Where this plan shows it                                                                 |
|-------------------------|-----------------------------------------------------------------|------------------------------------------------------------------------------------------|
| Freelance client        | Coverage of their critical flow, fast feedback, low maintenance | E2E purchase flow (TC25), smoke suite in CI on every push, one-place endpoint changes    |
| Freelance client        | Proof it actually runs                                          | Green CI badge + public Allure report link                                               |
| Recruiter / interviewer | REST Assured depth                                              | Multipart upload, POJO serialization, request chaining, spec builders, schema validation |
| Recruiter / interviewer | Tester mindset, not just happy paths                            | 16 negative, security and contract cases with priorities                                 |
| Recruiter / interviewer | Framework design thinking                                       | Layered structure, config via env vars, self-cleaning test data                          |

## Risks and assumptions

- **Endpoints unverified live:** taken from the course's published API
  contract; confirm paths and fields in Postman before coding.

- **Shared public demo:** it can be slow, reset or change without
  notice. Use retries on network errors only, and note in the README
  that failures may be environmental.

- **Undocumented error codes:** negative-case assertions are set after
  observing real responses (see Test cases).

- **TC23 needs a second account:** skip it until one exists.

- **No real payment or banking logic:** this repo shows framework skill;
  domain depth comes from the Parabank and payments repos.
