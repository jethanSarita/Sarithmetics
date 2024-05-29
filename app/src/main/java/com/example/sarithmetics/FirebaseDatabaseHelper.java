package com.example.sarithmetics;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public DatabaseReference getUserRef() {
        return firebaseDatabase.getReference("Users").child(firebaseUser.getUid());
    }

    public DatabaseReference getItemRef(String business_code) {
        return firebaseDatabase.getReference("businesses").child(business_code).child("items");
    }

    public DatabaseReference getCartRef(String user_uid) {
        return firebaseDatabase.getReference("Users").child(user_uid).child("cart");
    }
}
