package xyz.foodhut.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.provider.HomeProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Profile extends AppCompatActivity {

    Bundle extras;
    String type;
    EditText name, address, location;
    String mName, mAddress,mLocation;

    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    double lattitude, longitude;
    boolean gps_check;
    int provider;

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

     //   name = findViewById(R.id.etUpName);
     //   address = findViewById(R.id.etUpAddress);
     //   location = findViewById(R.id.etUpLocation);

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            type = extras.getString("type");
            mName=extras.getString("name");
            mAddress=extras.getString("address");

            Log.d("check", "info: "+mName+" "+mAddress);

         //   name.setText(mName);
         //   address.setText(mAddress);
        }

         requestPermission();

        client = LocationServices.getFusedLocationProviderClient(this);

        //show();

         getOrdinate();

        //getAddress(23.798428,90.353462);

    }

    public void updateProfile(View view) {
        mName = name.getText().toString();
        mAddress = address.getText().toString();
        mLocation=location.getText().toString();

        if (type.equals("customer")) {
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("about").child("name").setValue(mName);
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("about").child("address").setValue(mAddress);
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("about").child("location").setValue(mLocation);

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeCustomer.class));

        }
        if (type.equals("provider")) {
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("about").child("name").setValue(mName);
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("about").child("address").setValue(mAddress);
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("about").child("location").setValue(mLocation);

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeProvider.class));
        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    public void getOrdinate() {
        if (ActivityCompat.checkSelfPermission(Profile.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(Profile.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    lattitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Log.d("check", "location " + lattitude + " " + longitude);

                    getAddress(lattitude, longitude);
                }
                else{
                    show();
                }

            }
        });
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(Profile.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubLocality();
            add = add + "\n" + obj.getPremises();
            add = add + "\n" + obj.getSubThoroughfare();

            if(obj.getSubLocality()!=null) {
                location.setText(obj.getSubLocality());
            }
            else{
                location.setText(obj.getLocality());
            }

            Log.v("check", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

     /*   try {
            JSONObject jsonObj = get("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + ","
                    + longitude + "&sensor=true");
            String Status = jsonObj.getString("status");
            if (Status.equalsIgnoreCase("OK")) {
                JSONArray Results = jsonObj.getJSONArray("results");
                JSONObject location = Results.getJSONObject(0);
              String  finalAddress = location.getString("formatted_address");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }



    public void show() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            provider = 1;
            getLocation();
            Log.d("check method", "network condition");
        } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {

            provider = 2;
            getLocation();
            Log.d("check method", "passive condition");

        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            Log.d("check method", "gps alert");

        } else if (gps_check && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            provider = 3;
            getLocation();
            Log.d("check method", "gps condition");
        }
        Log.d("check provider", String.valueOf(provider));
    }

    private void getLocation() {
        Log.d("check method", "from getLocation");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }

        if (provider == 1) {

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (location != null) {
                    double latti = location.getLatitude();
                    double longi = location.getLongitude();
                    lattitude = latti;
                    longitude = longi;
                    Log.d("check method", "provider:"+provider);

                    Log.d("check", "location "+lattitude+" "+longitude);

                    getAddress(lattitude,longitude);

                } else {

                    Toast.makeText(this, "Unble to Trace your location, Please On your Location/GPS!", Toast.LENGTH_LONG).show();


                }
                Log.d("check method", "network");
            }
            if (provider == 2) {
                Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (location2 != null) {
                    double latti = location2.getLatitude();
                    double longi = location2.getLongitude();
                    lattitude = latti;
                    longitude = longi;
                    Log.d("check method", "provider:"+provider);
                    Log.d("check", "location "+lattitude+" "+longitude);

                    getAddress(lattitude,longitude);

                } else {

                    Toast.makeText(this, "Unble to Trace your location, Please On your Location/GPS!", Toast.LENGTH_LONG).show();

                }
                Log.d("check method", "passive");

            }
            if (provider == 3) {
                Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location1 != null) {
                    double latti = location1.getLatitude();
                    double longi = location1.getLongitude();
                    lattitude = latti;
                    longitude = longi;
                    Log.d("check method", "provider:"+provider);
                    Log.d("check", "location "+lattitude+" "+longitude);

                    getAddress(lattitude,longitude);

                } else {

                    Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();

                }
                Log.d("check method", "gps");

            }

    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        gps_check = true;

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        Log.d("check method", "from alert");
    }

}
