package com.example.sarithmetics;

import android.content.Context;
import android.content.SharedPreferences;

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
    public boolean getLogin(){
        return sharedPreferences.getBoolean("key_login", false);
    }
    public void setUsername(String username){
        editor.putString("key_username", username);
        editor.commit();
    }
    public String getUsername(){
        return sharedPreferences.getString("key_username", "");
    }
    public void setMainStatus(boolean status){
        editor.putBoolean("key_mainstatus", status);
        editor.commit();
    }
    public boolean getMainStatus(){
        return sharedPreferences.getBoolean("key_mainstatus", false);
    }

}
