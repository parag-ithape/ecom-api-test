package com.parag.ecom.models;

// One line of a create-order request
public class OrderItem {
    private String country;
    private String productOrderedId;

    public OrderItem() {
    }

    public OrderItem(String country, String productOrderedId) {
        this.country = country;
        this.productOrderedId = productOrderedId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProductOrderedId() {
        return productOrderedId;
    }

    public void setProductOrderedId(String productOrderedId) {
        this.productOrderedId = productOrderedId;
    }
}
