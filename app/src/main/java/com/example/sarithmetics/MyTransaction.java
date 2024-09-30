package com.example.sarithmetics;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class MyTransaction {

    //Implement parcelable later sleepy night night time

    private double customer_change;
    private double customer_payment;
    private double subtotal;
    private int item_count;
    private boolean is_out;
    private Object transaction_date;
    private List<Item> items;
    private String employee_name;

    public MyTransaction() {}

    public MyTransaction(double customer_change, double customer_payment, double subtotal, int item_count, boolean is_out, Object transaction_date, List<Item> items, String employee_name) {
        this.customer_change = customer_change;
        this.customer_payment = customer_payment;
        this.subtotal = subtotal;
        this.item_count = item_count;
        this.is_out = is_out;
        this.transaction_date = transaction_date;
        this.items = items;
        this.employee_name = employee_name;
    }

    public double getCustomer_change() {
        return customer_change;
    }

    public void setCustomer_change(double customer_change) {
        this.customer_change = customer_change;
    }

    public double getCustomer_payment() {
        return customer_payment;
    }

    public void setCustomer_payment(double customer_payment) {
        this.customer_payment = customer_payment;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public int getItem_count() {
        return item_count;
    }

    public void setItem_count(int item_count) {
        this.item_count = item_count;
    }

    public Object getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(Object transaction_date) {
        this.transaction_date = transaction_date;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public boolean isIs_out() {
        return is_out;
    }

    public void setIs_out(boolean is_out) {
        this.is_out = is_out;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }
}
