package com.parag.ecom.models;

import com.fasterxml.jackson.annotation.JsonInclude;

// Null fields are left out of the JSON, so negative tests can drop a field
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {
    private String userEmail;
    private String userPassword;

    public LoginRequest() {
    }

    public LoginRequest(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
