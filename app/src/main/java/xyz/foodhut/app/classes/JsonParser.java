package xyz.foodhut.app.classes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonParser {
    public static JSONObject getJSONfromURL(String url) {

        // initialize
        InputStream is = null;
        String result = "";
        JSONObject jObject = null;

        HttpURLConnection connection=null;
        // convert response to string
        try {
            URL url1=new URL(url);
          connection= (HttpURLConnection) url1.openConnection();
            connection.connect();
            is=connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d("check", "getJSONfromURL:result "+result);
        } catch (Exception e) {
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        finally {
            connection.disconnect();
        }

        // try parse the string to a JSON object
        try {
            jObject = new JSONObject(result);
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data " + e.toString());
        }

        return jObject;
    }

}
