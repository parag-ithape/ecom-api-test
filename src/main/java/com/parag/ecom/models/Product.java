package com.parag.ecom.models;

import java.io.File;

// Form fields for add-product; sent as multipart, not JSON
public class Product {
    private String productName;
    private String productAddedBy;
    private String productCategory;
    private String productSubCategory;
    private String productPrice;
    private String productDescription;
    private String productFor;
    private File productImage;

    // A valid product for the given user; the name is unique per run (API allows 3-20 characters)
    public static Product valid(String userId, File image) {
        Product p = new Product();
        p.productName = "QA Shirt " + System.currentTimeMillis() % 1_000_000;
        p.productAddedBy = userId;
        p.productCategory = "fashion";
        p.productSubCategory = "shirts";
        p.productPrice = "1150";
        p.productDescription = "Added by API automation";
        p.productFor = "women";
        p.productImage = image;
        return p;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductAddedBy() {
        return productAddedBy;
    }

    public void setProductAddedBy(String productAddedBy) {
        this.productAddedBy = productAddedBy;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductSubCategory() {
        return productSubCategory;
    }

    public void setProductSubCategory(String productSubCategory) {
        this.productSubCategory = productSubCategory;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductFor() {
        return productFor;
    }

    public void setProductFor(String productFor) {
        this.productFor = productFor;
    }

    public File getProductImage() {
        return productImage;
    }

    public void setProductImage(File productImage) {
        this.productImage = productImage;
    }
}
