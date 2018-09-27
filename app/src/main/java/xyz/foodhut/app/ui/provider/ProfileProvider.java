package xyz.foodhut.app.ui.provider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import xyz.foodhut.app.R;
import xyz.foodhut.app.classes.RequestHandler;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.customer.Favourites;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.customer.OrdersCustomer;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ProfileProvider extends AppCompatActivity {

    Bundle extras;
    String type;
    TextView tvName,tvKitchen, tvaddress, location;
    ImageView photo;
    String mName,mKitchenName, mAddress, mLocation, mImageUrl, mApartment, mHouse, mStreet, mArea;
    public String userID;

    byte[] byteArray;
    public static byte[] bytes;
    private static final int IMAGE_REQUEST = 1;
    private Uri filePath;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    double lattitude, longitude;
    boolean gps_check;
    int provider;

    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;
    private FusedLocationProviderClient client;

    private String address1, address2, city, state, country, county, PIN;
    // private static final String LOG_TAG = ReverseGeoCoding.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_provider);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }

        tvName = findViewById(R.id.profileName);
        tvKitchen = findViewById(R.id.kitchenName);
        tvaddress = findViewById(R.id.profileAddress);
        photo = findViewById(R.id.circlePhoto);
        // location = findViewById(R.id.etUpLocation);

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString("name");
            mAddress = extras.getString("address");
            mImageUrl = extras.getString("avatar");
            mKitchenName = extras.getString("kitchen")+"";

            Log.d("check", "info: " + mName + " " + mAddress+" "+mKitchenName);

            if (!mName.equals("")||mName!=null) {
                tvName.setText(mName);
                tvKitchen.setText(mKitchenName);
                tvaddress.setText(mAddress);
            }
            Picasso.get().load(mImageUrl).placeholder(R.drawable.default_avatar).into(photo);
        }

        checkAddress();

     //   requestPermission();

     //   client = LocationServices.getFusedLocationProviderClient(this);

        //show();

      //  getOrdinate();

        //getAddress(23.798428,90.353462);

    }

    public void checkAddress() {
        FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("about").child("addressParts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    mApartment = (String) hashUser.get("apartment");
                    mHouse = (String) hashUser.get("house");
                    mStreet = (String) hashUser.get("street");
                    mArea = (String) hashUser.get("area");

                    Log.d("check", "name in customer: " + mName);
                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void favourites(View view){
        startActivity(new Intent(this,Favourites.class));
        finish();
    }

    public void orders(View view){
        startActivity(new Intent(this,OrdersCustomer.class));
        finish();
    }

    public void logout(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        SharedPreferenceHelper.getInstance(this).logout();
        auth.signOut();
        finish();
    }

    public void updateName(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_update_name, null);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(inflate);

        final String[] newName = new String[1];

        final EditText etName = inflate.findViewById(R.id.etName);
        etName.setText(mName);

        builder.setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!etName.getText().toString().isEmpty()) {
                            tvName.setText(etName.getText().toString());
                            databaseReference.child("providers/" + userID).child("about").child("name").setValue(etName.getText().toString());

                            dialog.cancel();
                        } else {
                            Toast.makeText(ProfileProvider.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void updateKitchenName(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_update_kitchen_name, null);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(inflate);

        final String[] newName = new String[1];

        final EditText etName = inflate.findViewById(R.id.etName);
        etName.setText(mKitchenName);

        builder.setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!etName.getText().toString().isEmpty()) {
                            tvKitchen.setText(etName.getText().toString());
                            databaseReference.child("providers/" + userID).child("about").child("kitchenName").setValue(etName.getText().toString());

                            dialog.cancel();
                        } else {
                            Toast.makeText(ProfileProvider.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void goBack(View view){
            FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        String name = (String) hashUser.get("name");
                        String address = (String) hashUser.get("address");
                        String kitchen = (String) hashUser.get("kitchenName");

                        Log.d("check", "name in provider: " + name+" "+kitchen);


                        if ((name.equals("")||name.equals("name"))||(address.equals("")||address.equals("address"))||(kitchen.equals("")||kitchen.equals("kitchen"))){
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileProvider.this);
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
                        }
                        else {
                            startActivity(new Intent(ProfileProvider.this,HomeProvider.class));
                            finish();
                            finishAffinity();
                        }
                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
    }

    public void updateAddress(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_update_address, null);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(inflate);


        final EditText etApartment = inflate.findViewById(R.id.etApartment);
        final EditText etHouse = inflate.findViewById(R.id.etHouse);
        final EditText etStreet = inflate.findViewById(R.id.etStreet);
        final EditText etArea = inflate.findViewById(R.id.etArea);

        if (!mAddress.equals("address")) {
            etApartment.setText(mApartment);
            etHouse.setText(mHouse);
            etStreet.setText(mStreet);
            etArea.setText(mArea);
        }

        builder.setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mApartment = etApartment.getText().toString();
                        mHouse = etHouse.getText().toString();
                        mStreet = etStreet.getText().toString();
                        mArea = etArea.getText().toString();

                        if (!mApartment.isEmpty() && !mHouse.isEmpty() && !mStreet.isEmpty() && !mArea.isEmpty()) {
                            String address = mApartment + "/" + mHouse + "," + mStreet + "," + mArea;
                            StaticConfig.ADDRESS=address;
                            tvaddress.setText(address);
                            databaseReference.child("providers/" + userID).child("about").child("address").setValue(address);
                            xyz.foodhut.app.model.Address add = new xyz.foodhut.app.model.Address();
                            add.apartment = mApartment;
                            add.house = mHouse;
                            add.street = mStreet;
                            add.area = mArea;
                            databaseReference.child("providers/" + userID).child("about").child("addressParts").setValue(add);

                            Toast.makeText(getApplicationContext(), "Address Updated", Toast.LENGTH_SHORT).show();

                            dialog.cancel();
                        } else {
                            Toast.makeText(ProfileProvider.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
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

        final String[] imageURL = {null};

        int number = (new Random().nextInt(100));

        if (filePath != null) {

            progressDialog.show();
            StorageReference riversRef = mStorageRef.child("images/" + userID + "/" + mName + number);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            imageURL[0] = String.valueOf(taskSnapshot.getDownloadUrl());

                            //and displaying a success toast
                            if (imageURL[0] != null) {
                                Toast.makeText(getApplicationContext(), "Photo Uploaded ", Toast.LENGTH_LONG).show();
                                databaseReference.child("providers/" + userID).child("about").child("avatar").setValue(imageURL[0]);

                            } else {
                                Toast.makeText(ProfileProvider.this, "Something Wrong! Please try again", Toast.LENGTH_SHORT).show();
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
                            //progressDialog.setMessage("Uploaded " + ((int) progress) + "%");
                        }
                    });

        } else {
            Toast.makeText(ProfileProvider.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkFilePermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int permissionCheck = ProfileProvider.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += ProfileProvider.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
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

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                photo.setImageBitmap(bitmap);

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

    public void updateProfile(View view) {
        mName = tvName.getText().toString();
        mAddress = tvaddress.getText().toString();
        mLocation = location.getText().toString();

        if (type.equals("customer")) {
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("about").child("tvName").setValue(mName);
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("about").child("tvaddress").setValue(mAddress);
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("about").child("location").setValue(mLocation);

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeCustomer.class));

        }
        if (type.equals("provider")) {
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("about").child("tvName").setValue(mName);
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("about").child("tvaddress").setValue(mAddress);
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("about").child("location").setValue(mLocation);

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeProvider.class));
        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    public void getOrdinate() {
        if (ActivityCompat.checkSelfPermission(ProfileProvider.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(ProfileProvider.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    lattitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Log.d("check", "location " + lattitude + " " + longitude);

                    // getAddress(lattitude, longitude);
                    // GetReverseGeoCoding geoCoding=new GetReverseGeoCoding();
                    // geoCoding.getAddress(lattitude,longitude);

                    getArea(lattitude, longitude);

                } else {
                    show();
                }

            }
        });
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(ProfileProvider.this, Locale.getDefault());
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

            if (obj.getSubLocality() != null) {
                location.setText(obj.getSubLocality());
            } else {
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
                Log.d("check method", "provider:" + provider);

                Log.d("check", "location " + lattitude + " " + longitude);

                getAddress(lattitude, longitude);


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
                Log.d("check method", "provider:" + provider);
                Log.d("check", "location " + lattitude + " " + longitude);

                getAddress(lattitude, longitude);


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
                Log.d("check method", "provider:" + provider);
                Log.d("check", "location " + lattitude + " " + longitude);

                getAddress(lattitude, longitude);


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

    public void getArea(double lati, double longi) {

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
                                JSONArray address_components = zero.getJSONArray("address_components");
                                Log.d("check", "onResponse: components" + address_components.toString());

                                for (int i = 0; i < address_components.length(); i++) {
                                    JSONObject zero2 = address_components.getJSONObject(i);
                                    Log.d("check", "onResponse: zero2" + zero2.toString());
                                    String long_name = zero2.getString("long_name");
                                    JSONArray mtypes = zero2.getJSONArray("types");
                                    String type1 = mtypes.getString(0);
                                    String type2= mtypes.getString(0);
                                  //  String type2 =mtypes.getString(1) ;

                                    if (!long_name.isEmpty()||!long_name.equals("")) {
                                        if (type1.equals("sublocality")) {
                                            subLocality[0] = long_name;
                                           // Toast.makeText(ProfileCustomer.this, long_name, Toast.LENGTH_SHORT).show();
                                            Log.d("check", "onResponse: subLocality" + subLocality[0]);

                                        }
                                        else if (mtypes.getString(1).equals("sublocality")) {
                                            subLocality[0] = long_name;
                                           // Toast.makeText(ProfileCustomer.this, long_name, Toast.LENGTH_SHORT).show();
                                            Log.d("check", "onResponse: subLocality" + subLocality[0]);

                                        }



                                        //  Toast.makeText(ProfileCustomer.this, location[0], Toast.LENGTH_SHORT).show();
                                    }

                                    // JSONArray mtypes = zero2.getJSONArray("types");
                                    // String Type = mtypes.getString(0);
                                    // Log.e(Type,long_name);
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
}
