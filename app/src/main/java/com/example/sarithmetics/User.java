package com.example.sarithmetics;

import java.util.Map;

public class User {
    private String uid;
    private String first_name;
    private String last_name;
    private String business_code;
    private int user_type;

    public User() {
    }

    public User(String uid, String first_name, String last_name, String business_code, int user_type) {
        this.uid = uid;
        this.first_name = first_name;
        this.last_name = last_name;
        this.business_code = business_code;
        this.user_type = user_type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getBusiness_code() {
        return business_code;
    }

    public void setBusiness_code(String business_code) {
        this.business_code = business_code;
    }

    public int getUser_type() {
        return user_type;
    }

    public void setUser_type(int user_type) {
        this.user_type = user_type;
    }
}
