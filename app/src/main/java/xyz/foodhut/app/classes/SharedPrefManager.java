package xyz.foodhut.app.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static SharedPrefManager mInstance;
    private static Context mCtx;
    final String prefName = "MySharedPref";
    final String keyPhone = "phone";
    final String keyUser = "userId";

    public SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getmInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public boolean userLogin(String phone) {
        SharedPreferences sp = mCtx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(keyUser, phone);
        editor.apply();
        return true;
    }

    public boolean isLoggedIn() {
        SharedPreferences sp = mCtx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (sp.getString(keyUser, null) != null) {
            return true;
        } else
            return false;
    }

    public boolean logout() {
        SharedPreferences sp = mCtx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    public String getUser() {
        SharedPreferences sp = mCtx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return sp.getString(keyUser, null);
    }

}
