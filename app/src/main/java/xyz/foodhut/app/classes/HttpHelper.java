package xyz.foodhut.app.classes;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.URL;

/**
 * Created by Student on 3/31/2018.
 */

public class HttpHelper {
    private static final String TAG=HttpHelper.class.getSimpleName();
    public HttpHelper(){

    }
public String serviceCall(double lati,double longi) throws IOException{
   String reqUrl= "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lati + ","
            + longi + "&sensor=true";
       String response=null;
    try{
       URL url=new URL(reqUrl);
        HttpURLConnection con= (HttpURLConnection) url.openConnection();
       // con.setRequestMethod("GET");
        InputStream inputStream=new BufferedInputStream(con.getInputStream());
        response=convertStreamToString(inputStream);

    }catch (MalformedURLException e){
        Log.e(TAG, "MalformedURLException: "+e.getMessage());
    }catch (PortUnreachableException e){
        Log.e(TAG, "PortUnreachableException: "+e.getMessage());
    }catch (IOException e){
        Log.e(TAG, "IOException: "+e.getMessage());
    }catch (Exception e){
        Log.e(TAG, "Exception: "+e.getMessage());
    }

        return response;
}

    @NonNull
    private String convertStreamToString(InputStream inputStream) {
        BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb=new StringBuilder();
        String line;
        try{
            while ((line=reader.readLine()) !=null){
                sb.append(line).append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        Log.d("check", "convertStreamToString: jsonstring"+sb.toString());
        return sb.toString();
    }
}
