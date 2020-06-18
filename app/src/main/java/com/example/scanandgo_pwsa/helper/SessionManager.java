package com.example.scanandgo_pwsa.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    // Logcat tag
    private static String TAG = SessionManager.class.getSimpleName();
 
    // Shared Preferences
    private SharedPreferences pref;
 
    private Editor editor;

    // Shared preferences file name
    private static final String PREF_NAME = "Projekt";
     
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_IS_PRODUCTSET = "isProductSet";
    private static final String KEY_IS_FIRSTRUN = "isFirstRun";
    private static final String KEY_IS_SHOPSET = "isShopSet";
    private static final String KEY_IS_SHOPSELECT = "isShopSelect";
    private static final String KEY_IS_SCANSHOPSELECT = "isScanShopSelect";
    private static final String KEY_IS_SCANANDGOSTARTED = "isScanAndGoStarted";

    public SessionManager(Context context) {
        // Shared pref mode
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public String isShopSelect(){
        return pref.getString(KEY_IS_SHOPSELECT, null);
    }

    public void setShopSelect(String isShopSelect) {
 
        editor.putString(KEY_IS_SHOPSELECT, isShopSelect);

        editor.commit();
 
        Log.d(TAG, "Shop select modified!");
    }

    public String isScanShopSelect(){
        return pref.getString(KEY_IS_SCANSHOPSELECT, null);
    }

    public void setScanShopSelect(String isShopSelect) {

        editor.putString(KEY_IS_SCANSHOPSELECT, isShopSelect);

        editor.commit();

        Log.d(TAG, "Shop select modified!");
    }

    public void setShop(int isShopSet){
        editor.putInt(KEY_IS_SHOPSET, isShopSet);

        editor.commit();

        Log.d(TAG, "Shop set modified!");
    }

    public int isShopSet(){
        return pref.getInt(KEY_IS_SHOPSET, -1);
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isFirstRun(){
        return pref.getBoolean(KEY_IS_FIRSTRUN, true);
    }

    public void setFirstRun(boolean isFristRun) {

        editor.putBoolean(KEY_IS_FIRSTRUN, isFristRun);

        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public void setProduct(boolean isProductSet) {

        editor.putBoolean(KEY_IS_PRODUCTSET, isProductSet);

        editor.commit();

        Log.d(TAG, "Product session modified!");
    }

    public boolean isScanAndGoStarted() {
        return pref.getBoolean(KEY_IS_SCANANDGOSTARTED, false);
    }

    public void setScanAndGoStarted(boolean isScanAndGo) {

        editor.putBoolean(KEY_IS_SCANANDGOSTARTED, isScanAndGo);

        editor.commit();

        Log.d(TAG, "ScanAndGo session modified!");
    }
}