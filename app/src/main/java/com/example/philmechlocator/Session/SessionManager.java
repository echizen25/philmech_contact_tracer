package com.example.philmechlocator.Session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.se.omapi.Session;

import com.example.philmechlocator.LoginActivity;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    Context _context;

    int Private_mode = 0;

    private static final String PREF_NAME = "AndroidHivePref";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_USERNAME = "username";

    public static final String KEY_PASSWORD = "password";

    public static final String KEY_MEM = "memcode";

    public static final String KEY_GENDER = "gender";


    public SessionManager (Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Private_mode);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String password, String memcodes, String genders){
        editor.putBoolean(IS_LOGIN,true);

        editor.putString(KEY_USERNAME, username);

        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_MEM, memcodes);

        editor.putString(KEY_GENDER, genders);

        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            _context.startActivity(i);
        }
    }

    public HashMap<String , String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME,null));

        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD,null));

        user.put(KEY_MEM, pref.getString(KEY_MEM,null));

        user.put(KEY_GENDER, pref.getString(KEY_GENDER,null));

        return user;
    }

    public void logoutUser(){
        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(i);
    }
}