package xyz.foodhut.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import xyz.foodhut.app.model.User;


public class SharedPreferenceHelper {
    private static SharedPreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_PHONE = "phone";
    private static String SHARE_KEY_AVATA = "avatar";
    private static String SHARE_KEY_UID = "uid";
    private static String LOCATION = "location";


    private SharedPreferenceHelper() {}

    public static SharedPreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceHelper();
            preferences = context.getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
        }
        return instance;
    }

    public void saveUserInfo(User user) {
        editor = preferences.edit();
        editor.putString("type", user.type);
        //editor.putString(SHARE_KEY_NAME, user.name);
        editor.putString(SHARE_KEY_PHONE, user.phone);
        //editor.putString(SHARE_KEY_AVATA, user.avatar);
        editor.putString(SHARE_KEY_UID, StaticConfig.UID);
        editor.apply();
    }

    public User getUserInfo(){

        String userName = preferences.getString(SHARE_KEY_NAME, "");
        String email = preferences.getString(SHARE_KEY_PHONE, "");
        String avatar = preferences.getString(SHARE_KEY_AVATA, "default");

        User user = new User();
        user.name = userName;
        user.phone = email;
        user.avatar = avatar;

        return user;
    }

    public boolean isLoggedIn() {
        if (preferences.getString(SHARE_KEY_UID, null) != null) {
            return true;
        } else
            return false;
    }

    public void logout() {
        editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public void setLatLong(String lat,String longi) {

        Log.d("check", "lat long in SPH: "+lat+" "+longi);

        editor = preferences.edit();

        editor.putString("lat", lat);
        editor.putString("long", longi);
        editor.apply();
    }

    public String getLat(){
        Log.d("check", "lat  in SPH: "+preferences.getString("lat", ""));
        return preferences.getString("lat", "");
    }

    public String getLong(){
        Log.d("check", "long  in SPH: "+preferences.getString("long", ""));
        return preferences.getString("long", "");
    }

    public void setLocation(String location) {
        editor = preferences.edit();

        editor.putString(LOCATION, location);
        editor.apply();
    }

    public String getLOCATION(){
        return preferences.getString(LOCATION, "");
    }

    public void setUID(String id) {
        editor = preferences.edit();

        editor.putString(SHARE_KEY_UID, id);
        editor.apply();
    }
    public void setType(String type) {
        editor = preferences.edit();

        editor.putString("type", type);
        editor.apply();
    }
    public String getUID(){
        return preferences.getString(SHARE_KEY_UID, "");
    }

    public String getType(){
        return preferences.getString("type", "");
    }

}
