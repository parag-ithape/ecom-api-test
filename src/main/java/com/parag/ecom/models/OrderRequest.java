package com.parag.ecom.models;

import java.util.ArrayList;
import java.util.List;

public class OrderRequest {
    private List<OrderItem> orders = new ArrayList<>();

    public OrderRequest() {
    }

    public OrderRequest(List<OrderItem> orders) {
        this.orders = orders;
    }

    // An order for one product shipped to one country
    public static OrderRequest of(String country, String productId) {
        return new OrderRequest(List.of(new OrderItem(country, productId)));
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }
}
