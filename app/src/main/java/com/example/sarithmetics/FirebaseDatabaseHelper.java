package com.example.sarithmetics;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
}
