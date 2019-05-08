package xyz.foodhut.app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import xyz.foodhut.app.R;
import xyz.foodhut.app.classes.RequestHandler;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.provider.HomeProvider;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ProfileUpdate extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Bundle extras;
    String type;
    EditText name, kitchen, address,phone, apartment, house, street, area;
    CardView cvKitchen, cvLocation;
    ImageView photo;
    CheckBox check;
    String mType, mName = "", mPhone = "", mKitchenName = "", mAddress = "", autoAddress, mImageUrl="default", mApartment, mHouse, mStreet, mArea;
    public String userID, isUpdate = "false";


    byte[] byteArray;
    Bitmap bitmap;
    public static byte[] bytes;
    private static final int IMAGE_REQUEST = 1;
    private Uri filePath;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    ProgressDialog dialog;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    double latitude = 0, mLatitude, longitude = 0, mLongitude;
    boolean gps_check, isChecked;
    int provider;

    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;
    private FusedLocationProviderClient client;

    private String address1, address2, city, state, country, county, PIN;
    // private static final String LOG_TAG = ReverseGeoCoding.class.getSimpleName();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    }
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    }
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        client = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        dialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }

        cvKitchen = findViewById(R.id.cvKitchen);
        cvLocation = findViewById(R.id.cvLocation);
        check = findViewById(R.id.check);
        name = findViewById(R.id.etName);
        phone = findViewById(R.id.etPhone);
        kitchen = findViewById(R.id.etKitchen);
        photo = findViewById(R.id.circlePhoto);

        apartment = findViewById(R.id.etApartment);
        house = findViewById(R.id.etHouse);
        street = findViewById(R.id.etStreet);
        area = findViewById(R.id.etArea);
        // location = findViewById(R.id.etUpLocation);

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mType = extras.getString("type");
            isUpdate = extras.getString("isUpdate");
            mName = extras.getString("name");
            mAddress = extras.getString("address");
            mImageUrl = extras.getString("avatar");
            mPhone = extras.getString("phone");

            if (mType.equals("provider"))
                mKitchenName = extras.getString("kitchen");
            if (mType.equals("customer")) {
                cvKitchen.setVisibility(View.GONE);
                cvLocation.setVisibility(View.GONE);
            }


            Log.d("check", "info: " + mName + " " + mImageUrl + " " + mKitchenName);


                if (!mImageUrl.equals("default")) {
                    Picasso.get().load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(photo);

                  //  Glide.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(photo);
                }



        }

        checkProfile();
        checkAddress();

        EnableGPSAutoMatically();

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {

            buildLocationRequest();
            buildLocationCallback();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        }

        //  getOrdinate();

        //  locationCall();

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dialog.setMessage("Getting GPS...");
                    //  dialog.setCancelable(false);
                    dialog.show();
                    locationUpdate();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {

                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                //  dialog.cancel();
                                buildAlertMessageNoGps();
                                Log.d("check method", "gps alert");

                            }
                        }
                    }, 5 * 1000);
                }
            }
        });


        if (isUpdate.equals("false")&&mType.equals("provider")) {
            check.setChecked(true);
            check.setEnabled(false);
            getFirstLocation();
        }

        //   requestPermission();

        //   client = LocationServices.getFusedLocationProviderClient(this);

        //show();

        //  getOrdinate();

        //getAddress(23.798428,90.353462);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (StaticConfig.BITMAP!=null){
            photo.setImageBitmap(StaticConfig.BITMAP);
        }
    }

    private void EnableGPSAutoMatically() {
        GoogleApiClient googleApiClient = null;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(3 * 1000);
        locationRequest.setSmallestDisplacement(10);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // **************************
        builder.setAlwaysShow(true); // this is the key ingredient
        // **************************

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //   toast("Success");
                        // All location settings are satisfied. The client can
                        // initialize location

                        gps_check = true;
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //  toast("GPS is not on");
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.

                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(ProfileUpdate.this, 1000);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //  toast("Setting change not allowed");
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.

                        break;
                }
            }
        });



    }

    public void getFirstLocation() {
        dialog.setMessage("Getting GPS...");
        //  dialog.setCancelable(false);
        dialog.show();
        locationUpdate();

        new Handler().postDelayed(new Runnable() {
            public void run() {

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //  dialog.cancel();
                    buildAlertMessageNoGps();
                    Log.d("check method", "gps alert");

                }
            }
        }, 5 * 1000);
    }

    private void locationUpdate() {
        if (ActivityCompat.checkSelfPermission(ProfileUpdate.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ProfileUpdate.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ProfileUpdate.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);


            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                dialog.cancel();
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } else {
                        locationCall();
                    }

                    Log.d("check", "location " + latitude + " " + longitude);
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    public void checkAddress() {
        if (mType.equals("provider")) {
            FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("about").child("addressParts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        mApartment = (String) hashUser.get("apartment");
                        mHouse = (String) hashUser.get("house");
                        mStreet = (String) hashUser.get("street");
                        mArea = (String) hashUser.get("area");

                        apartment.setText(mApartment);
                        house.setText(mHouse);
                        street.setText(mStreet);
                        area.setText(mArea);

                        Log.d("check", "name in provider: " + mName);
                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
        }

        if (mType.equals("customer")) {
            FirebaseDatabase.getInstance().getReference().child("customers").child(StaticConfig.UID).child("about").child("addressParts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        mApartment = (String) hashUser.get("apartment");
                        mHouse = (String) hashUser.get("house");
                        mStreet = (String) hashUser.get("street");
                        mArea = (String) hashUser.get("area");

                        apartment.setText(mApartment);
                        house.setText(mHouse);
                        street.setText(mStreet);
                        area.setText(mArea);

                        Log.d("check", "name in customer: " + mName);
                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
        }
    }

    public void checkProfile() {

        if (mType.equals("provider")) {
            FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        String name1 = (String) hashUser.get("name");
                        String phone1 = (String) hashUser.get("phone");
                        String avatar1 = (String) hashUser.get("avatar");

                        String kitchenName1 = (String) hashUser.get("kitchenName");

                        if (!name1.equals("name")) {
                            mName = name1;
                            name.setText(name1);
                        }
                        if (!phone1.equals("phone")) {
                            mPhone = phone1;
                            phone.setText(phone1);
                        }

                        if (!kitchenName1.equals("kitchen")) {
                            mKitchenName = kitchenName1;
                            kitchen.setText(kitchenName1);
                        }

                //       if (!avatar1.equals("default")){
                //            Glide.with(getApplicationContext()).load(avatar1).placeholder(R.drawable.kitchen_icon_colour).into(photo);
                 //       }

                        Log.d("check", "name in provider: " + name1 + " " + kitchenName1);


                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
        }
        if (mType.equals("customer")) {
            FirebaseDatabase.getInstance().getReference().child("customers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        String name1 = (String) hashUser.get("name");
                        String phone1 = (String) hashUser.get("phone");
                        String avatar1 = (String) hashUser.get("avatar");

                        if (!name1.equals("name")) {
                            mName = name1;
                            name.setText(name1);
                        }
                        if (!phone1.equals("phone")) {
                            mPhone = phone1;
                            phone.setText(phone1);
                        }

                  //     if (!avatar1.equals("default")){
                  //          Glide.with(getApplicationContext()).load(avatar1).placeholder(R.drawable.kitchen_icon_colour).into(photo);
                  //      }

                        Log.d("check", "name in provider: " + name1);


                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
        }
    }

    public void done(View view) {

        if (isUpdate.equals("false") && (latitude == 0 && longitude == 0)&&mType.equals("provider")) {

            Toast.makeText(this, "Sorry, we couldn't find your location", Toast.LENGTH_SHORT).show();
        } else if ((isUpdate.equals("false") && (latitude != 0 && longitude != 0)) || isUpdate.equals("true")||mType.equals("customer")) {

            if (!name.getText().toString().isEmpty()&&!phone.getText().toString().isEmpty()) {
                mName = name.getText().toString();
                mPhone= phone.getText().toString();

                if (mType.equals("provider")) {
                    databaseReference.child("providers/" + userID).child("about").child("name").setValue(name.getText().toString());
                    databaseReference.child("providers/" + userID).child("about").child("phone").setValue(phone.getText().toString());

                    if (kitchen.getText().toString().isEmpty())
                        mKitchenName = mName + "'s Kitchen";
                    else
                        mKitchenName = kitchen.getText().toString();
                    databaseReference.child("providers/" + userID).child("about").child("kitchenName").setValue(mKitchenName);

                }
                if (mType.equals("customer")) {
                    databaseReference.child("customers/" + userID).child("about").child("name").setValue(name.getText().toString());
                    databaseReference.child("customers/" + userID).child("about").child("phone").setValue(phone.getText().toString());
                }


            } else {
                Toast.makeText(ProfileUpdate.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
            }

            mApartment = apartment.getText().toString().trim();
            mHouse = house.getText().toString().trim();
            mStreet = street.getText().toString().trim();
            mArea = area.getText().toString().trim();

            if (!mApartment.isEmpty() && !mHouse.isEmpty() && !mStreet.isEmpty() && !mArea.isEmpty()) {
                String address = mApartment + "/" + mHouse + "," + mStreet + "," + mArea;
                mAddress = address;
                StaticConfig.ADDRESS = address;
                xyz.foodhut.app.model.Address add = new xyz.foodhut.app.model.Address();
                add.apartment = mApartment;
                add.house = mHouse;
                add.street = mStreet;
                add.area = mArea;

                if (mType.equals("provider")) {
                    databaseReference.child("providers/" + userID).child("about").child("address").setValue(address);
                    databaseReference.child("providers/" + userID).child("about").child("addressParts").setValue(add);
                }
                if (mType.equals("customer")) {
                    databaseReference.child("customers/" + userID).child("about").child("address").setValue(address);
                    databaseReference.child("customers/" + userID).child("about").child("addressParts").setValue(add);
                }


                Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(ProfileUpdate.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
            }

            if ((check.isChecked() || isUpdate.equals("false")) && (latitude != 0 && longitude != 0)) {
                Log.d("check", "onCreate: checking location");
                if (mType.equals("provider")) {
                    databaseReference.child("providers/" + userID).child("about").child("latitude").setValue("" + latitude + "");
                    databaseReference.child("providers/" + userID).child("about").child("longitude").setValue("" + longitude + "");
                }
                if (mType.equals("customer")) {
                    databaseReference.child("customers/" + userID).child("about").child("latitude").setValue("" + latitude + "");
                    databaseReference.child("customers/" + userID).child("about").child("longitude").setValue("" + longitude + "");
                }
            }

            if (mType.equals("provider")) {
                startActivity(new Intent(this,HomeProvider.class));
                finishAffinity();  }
            if (mType.equals("customer")) {
                startActivity(new Intent(this,HomeCustomer.class));
                finishAffinity();  }
        }




    }

    public void goBack(View view) {

        if (mType.equals("provider")) {
            if ((mName.equals("") || mName.equals("name")) || (mAddress.equals("") || mAddress.equals("address")) || (mKitchenName.equals("") || mKitchenName.equals("kitchen"))) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUpdate.this);
                builder.setMessage("Please update all information before you leave.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
                Log.d("check method", "from alert");
            } else {
                //  startActivity(new Intent(ProfileProvider.this, HomeProvider.class));
                finish();
                //  finishAffinity();
            }
        }
        if (mType.equals("customer")) {
            if ((mName.equals("") || mName.equals("name")) || (mAddress.equals("") || mAddress.equals("address"))) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUpdate.this);
                builder.setMessage("Please update all information before you leave.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
                Log.d("check method", "from alert");
            } else {
                //  startActivity(new Intent(ProfileProvider.this, HomeProvider.class));
                finish();
                //  finishAffinity();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (mType.equals("provider")) {
            if ((mName.equals("") || mName.equals("name")) || (mAddress.equals("") || mAddress.equals("address")) || (mKitchenName.equals("") || mKitchenName.equals("kitchen"))) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUpdate.this);
                builder.setMessage("Please update all information before you leave.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
                Log.d("check method", "from alert");
            } else {
                //  startActivity(new Intent(ProfileProvider.this, HomeProvider.class));
                finish();
                //  finishAffinity();
            }
        }
        if (mType.equals("customer")) {
            if ((mName.equals("") || mName.equals("name")) || (mAddress.equals("") || mAddress.equals("address"))) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUpdate.this);
                builder.setMessage("Please update all information before you leave.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
                Log.d("check method", "from alert");
            } else {
                //  startActivity(new Intent(ProfileProvider.this, HomeProvider.class));
                finish();
                //  finishAffinity();
            }
        }
    }


    public void browsPhoto(View view) {

        checkFilePermissions();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", "image/*");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Select File");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Select File");
        }
        startActivityForResult(chooserIntent, IMAGE_REQUEST);
    }

    public void updatePhoto() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        final String[] imageURL = {null};

        int number = (new Random().nextInt(100));

        if (filePath != null && byteArray != null) {

            progressDialog.show();
            StorageReference riversRef = mStorageRef.child("images/" + userID + "/" + mName);
            UploadTask uploadTask = riversRef.putBytes(byteArray);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //if the upload is successfull
                    //hiding the progress dialog
                    progressDialog.dismiss();
                    imageURL[0] = String.valueOf(taskSnapshot.getDownloadUrl());

                    //and displaying a success toast
                    if (imageURL[0] != null) {
                        Toast.makeText(getApplicationContext(), "Photo Uploaded ", Toast.LENGTH_LONG).show();

                        if (mType.equals("provider"))
                            databaseReference.child("providers/" + userID).child("about").child("avatar").setValue(imageURL[0]);
                        if (mType.equals("customer"))
                            databaseReference.child("customers/" + userID).child("about").child("avatar").setValue(imageURL[0]);


                    } else {
                        Toast.makeText(ProfileUpdate.this, "Something Wrong! Please try again", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("state", "onSuccess::  imageURL: " + imageURL[0]);
                    //save image info in database

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploading..");
                        }
                    });

        } else {
            Toast.makeText(ProfileUpdate.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkFilePermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int permissionCheck = ProfileUpdate.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += ProfileUpdate.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
            }
        } else {
            Log.d("Check", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                StaticConfig.BITMAP=bitmap;

                photo.setImageBitmap(bitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byteArray = stream.toByteArray();



                stream.close();

                updatePhoto();

            /*    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
                // bitmap.recycle();
                stream.close();
                Log.d("check", "onActivityResult: " + photo);
                */


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getOrdinate() {
        if (ActivityCompat.checkSelfPermission(ProfileUpdate.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ProfileUpdate.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ProfileUpdate.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            return;
        } else {
            client.getLastLocation().addOnSuccessListener(ProfileUpdate.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Log.d("check", "location fused " + latitude + " " + longitude);

                        // getAddress(latitude, longitude);
                        // GetReverseGeoCoding geoCoding=new GetReverseGeoCoding();
                        // geoCoding.getAddress(latitude,longitude);

                        //  getAddress(latitude, longitude);
                        //   getArea(latitude, longitude);

                    } else {
                        show();
                    }

                }
            });
        }
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(ProfileUpdate.this, Locale.getDefault());
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


            Log.v("check", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    /*    try {
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
                latitude = latti;
                longitude = longi;


                Log.d("check method", "provider:" + provider);

                Log.d("check", "location " + latitude + " " + longitude);

                //   getAddress(latitude, longitude);


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
                latitude = latti;
                longitude = longi;


                Log.d("check method", "provider:" + provider);
                Log.d("check", "location " + latitude + " " + longitude);

                //    getAddress(latitude, longitude);


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
                latitude = latti;
                longitude = longi;

                Log.d("check method", "provider:" + provider);
                Log.d("check", "location " + latitude + " " + longitude);

                //   getAddress(latitude, longitude);


            } else {

                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();

            }
            Log.d("check method", "gps");

        }

    }

    public void locationCall() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getCoOrdinate();
            //  getLocation();
        }
    }

    private void getCoOrdinate() {
        if (ActivityCompat.checkSelfPermission(ProfileUpdate.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ProfileUpdate.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ProfileUpdate.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Log.d("check", "location network " + latitude + " " + longitude);
            } else if (location1 != null) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                Log.d("check", "location gps " + latitude + " " + longitude);

            } else if (location2 != null) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                Log.d("check", "location passive " + latitude + " " + longitude);
            } else {

                Toast.makeText(this, "Unable to Trace your location", Toast.LENGTH_SHORT).show();

            }
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


 /*   public void getLocation(double lati, double longi, final TextView tvLocate) {

        //  longi=90.353655;
        //   lati=23.795604;

        final String[] subLocality = new String[1];
        final String[] area_level_3 = new String[1];

        String reqUrl = "https://barikoi.xyz/v1/api/search/reverse/geocode/NjcwOjRPTUQ1OVRDV1A=/place?longitude=" + longi + "&latitude=" + lati;
        StringRequest sr = new StringRequest(Request.Method.GET, reqUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("tagconvertstr getData", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray Results = jsonObject.getJSONArray("Place");
                            Log.d("check", "onResponse: Place" + Results.toString());
                            JSONObject zero = Results.getJSONObject(0);
                            Log.d("check", "onResponse: zero" + zero.toString());

                            String area = zero.getString("area");

                            if (!area.equals("") && !area.isEmpty()) {
                                mLocation = area;
                                mLocationTemp = area;
                                tvLocate.setText(area);
                            } else {
                                Toast.makeText(HomeCustomer.this, "Sorry we can't detect your location.", Toast.LENGTH_SHORT).show();
                            }

                            Log.d("check", "onResponse address: " + area);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        if (mLocation.equals("")) {
                            Toast.makeText(HomeCustomer.this, "Sorry we can't detect your location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        RequestHandler.getmInstance(this).addToRequestQueue(sr);
    }

      public void getArea(double lati, double longi, final TextView tvLocate) {

        tvLocation.setText("Your location");
        getDistance(lati, longi);

        // longi=90.353655;
        // lati=23.795604;

        final String[] subLocality = new String[1];
        final String[] area_level_3 = new String[1];

        String reqUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lati + ","
                + longi + "&sensor=true";
        StringRequest sr = new StringRequest(Request.Method.GET, reqUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("tagconvertstr getData", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String Status = jsonObject.getString("status");
                            if (Status.equalsIgnoreCase("OK")) {
                                JSONArray Results = jsonObject.getJSONArray("results");
                                Log.d("check", "onResponse: results" + Results.toString());
                                JSONObject zero = Results.getJSONObject(0);
                                Log.d("check", "onResponse: zero" + zero.toString());

                                String formatted_address = zero.getString("formatted_address");

                                if (!formatted_address.equals("")) {
                                    //  tvLocate.setText(formatted_address);
                                    tvLocation.setText(formatted_address);
                                } else {
                                    // tvLocate.setText("Your location");
                                    tvLocation.setText("Your location");
                                }

                                String parts[] = formatted_address.split(",");

                               if (parts[2] != null) {
                                    mLocation = parts[2];
                                    mLocationTemp = parts[2];
                                    tvLocate.setText(parts[2]);
                                }

}
                        } catch (JSONException e) {
                                e.printStackTrace();
                                }

                                }
                                },
                                new Response.ErrorListener() {
@Override
public void onErrorResponse(VolleyError error) {
        //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }
        });

        RequestHandler.getmInstance(this).addToRequestQueue(sr);


        }

    */

}