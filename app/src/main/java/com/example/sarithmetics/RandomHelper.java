package com.example.sarithmetics;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class RandomHelper {
    private static final String DB = "https://sarithmetics-f53d1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private FirebaseDatabase firebaseDatabase;

    public RandomHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance(DB);
    }

    public void generateUniqueBusinessCode(OnUniqueIdGenerated callback) {
        String generated_id = generate5LetterCharString();
        checkIfExistsAndRegenerate(generated_id, callback);
    }

    private void checkIfExistsAndRegenerate(String id, OnUniqueIdGenerated callback) {
        firebaseDatabase.getReference("businesses").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Id already exists
                    checkIfExistsAndRegenerate(generate5LetterCharString(), callback);
                } else {
                    //Id is unique
                    callback.onGenrated(id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Error
                callback.onError(error.toException());
            }
        });
    }

    private String generate5LetterCharString() {
        int length = 5;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextInt(26) + 'A');
            sb.append(c);
        }
        return sb.toString();
    }

    public interface OnUniqueIdGenerated {
        void onGenrated(String unique_id);
        void onError(Exception e);
    }

    public String generateRandom5NumberCharString() {
        int length = 5;
        String numbers = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(numbers.length());
            sb.append(numbers.charAt(randomIndex));
        }
        return sb.toString();
    }
}
