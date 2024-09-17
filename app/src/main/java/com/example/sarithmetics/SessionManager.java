package com.example.sarithmetics;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context){
        sharedPreferences = context.getSharedPreferences("appkey", 0);
        editor = sharedPreferences.edit();
        editor.commit();
    }
    public void setLogin(boolean login){
        editor.putBoolean("key_login", login);
        editor.commit();
    }

    public void setUsername(String username) {
        editor.putString("key_username", username);
        editor.commit();
    }

    public void setMainStatus(int status){
        //0 - Home
        //1 - Items
        //2 - Restock
        //3 - History
        //4 - Insights
        editor.putInt("key_mainstatus", status);
        editor.commit();
    }

    public void setUser(FirebaseUser user) {
        editor.putString("key_uid", user.getUid());
        editor.putString("key_username", user.getDisplayName());
        editor.putString("key_email", user.getEmail());
        editor.commit();
    }
    public boolean getLogin(){
        return sharedPreferences.getBoolean("key_login", false);
    }

    public int getMainStatus(){
        return sharedPreferences.getInt("key_mainstatus", 0);
    }

    public String getUid() {
        return sharedPreferences.getString("key_uid", "");
    }
    public String getUsername() {
        return sharedPreferences.getString("key_username", "");
    }
    public String getEmail() {
        return sharedPreferences.getString("key_email", "");
    }

}
