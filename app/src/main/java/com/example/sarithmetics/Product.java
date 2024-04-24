package com.example.sarithmetics;

import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;


public class Product implements Serializable {
    private int productID;
    private String productName;
    private float productPrice;
    private int productQty;

    public Product(int productID, String productName, float productPrice, int productQty) {
        this.productID = productID;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQty = productQty;
    }

    public int getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public float getProductPrice() {
        return productPrice;
    }

    public int getProductQty() {
        return productQty;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductPrice(float productPrice) {
        this.productPrice = productPrice;
    }

    public void setProductQty(int productQty) {
        this.productQty = productQty;
    }
}
