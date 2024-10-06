package com.example.sarithmetics;

public class Item {
    private String name;
    private double price;
    private double cost_price;
    private int quantity;
    private int restock_quantity;
    private int in_cart_quantity;
    private String category;

    public Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.cost_price = 0.0;
        this.restock_quantity = 0;
    }

    public Item(String name, double price, double cost_price, int quantity) {
        this.name = name;
        this.price = price;
        this.cost_price = cost_price;
        this.quantity = quantity;
        this.restock_quantity = 0;
    }

    public Item(String name, double price, double cost_price, int quantity, int restock_quantity) {
        this.name = name;
        this.price = price;
        this.cost_price = cost_price;
        this.quantity = quantity;
        this.restock_quantity = restock_quantity;
    }

    public Item(String name, double price, int quantity, int restock_quantity) {
        this.name = name;
        this.price = price;
        this.cost_price = 0.0;
        this.quantity = quantity;
        this.restock_quantity = restock_quantity;
    }

    public Item(String name, double price, int quantity, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Item(String name, double price, double cost_price, String category) {
        this.name = name;
        this.price = price;
        this.cost_price = cost_price;
        this.category = category;
    }

    public Item() {

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getRestock_quantity() {
        return restock_quantity;
    }

    public void setRestock_quantity(int restock_quantity) {
        this.restock_quantity = restock_quantity;
    }

    public int getIn_cart_quantity() {
        return in_cart_quantity;
    }

    public void setIn_cart_quantity(int in_cart_quantity) {
        this.in_cart_quantity = in_cart_quantity;
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

    public void setCost_price(double cost_price) {
        this.cost_price = cost_price;
    }

    public double getCost_price() {
        return cost_price;
    }
}
