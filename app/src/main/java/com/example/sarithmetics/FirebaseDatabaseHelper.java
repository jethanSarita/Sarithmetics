package com.example.sarithmetics;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class FirebaseDatabaseHelper {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;

    public FirebaseDatabaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance(DB);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public void setFirebaseDatabase(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public DatabaseReference getDatabaseRef(String ref){
        return firebaseDatabase.getReference(ref);
    }

    public DatabaseReference getCurrentUserRef() {
        return firebaseDatabase.getReference("Users").child(firebaseUser.getUid());
    }

    public DatabaseReference getUserRef(String uid) {
        return firebaseDatabase.getReference("Users").child(uid);
    }

    public DatabaseReference getItemsRef(String business_code) {
        return firebaseDatabase.getReference("businesses").child(business_code).child("items");
    }

    public DatabaseReference getCartRef(String user_uid) {
        return firebaseDatabase.getReference("Users").child(user_uid).child("cart");
    }

    public DatabaseReference getBusinessCodeRef(String business_code) {
        return firebaseDatabase.getReference("businesses").child(business_code);
    }

    public DatabaseReference getItemsCategories(String business_code) {
        return firebaseDatabase.getReference("businesses").child(business_code).child("categories");
    }

    public Query getEmployeesQuery(String business_code) {
        return firebaseDatabase.getReference("Users").orderByChild("business_code").equalTo(business_code);
    }

    public DatabaseReference getBusinessRef() {
        return firebaseDatabase.getReference("businesses");
    }

    public DatabaseReference getBusinessTransactionHistoryRef(String business_code) {
        return firebaseDatabase.getReference("businesses").child(business_code).child("history");
    }

    public String getDayOfWeek(int context){
        ArrayList<String> today_list = new ArrayList<>(Arrays.asList("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"));
        ArrayList<String> yesterday_list = new ArrayList<>(Arrays.asList("saturday", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday"));
        switch (context) {
            case 0:
                return today_list.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
            case 1:
                return yesterday_list.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
            default:
                return "Error DaysOfWeek";
        }
    }
}
