package com.example.sarithmetics;

import java.util.Date;

public class Item {
    private String name;
    private double price;
    private double cost_price;
    private int quantity;
    private int restock_quantity;
    private String category;

    public Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.cost_price = 0.0;
        this.restock_quantity = 0;
        this.quantity = quantity;
    }

    public Item(String name, double price, double cost_price, int quantity) {
        this.name = name;
        this.price = price;
        this.cost_price = cost_price;
        this.quantity = quantity;
        this.restock_quantity = 0;
    }

    public Item(String name, double price, int quantity, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Item() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCostPrice(double cost_price) {
        this.cost_price = cost_price;
    }

    public double getCostPrice() {
        return cost_price;
    }
}
