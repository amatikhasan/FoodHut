package xyz.foodhut.app.ui.customer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.appyvet.materialrangebar.RangeBar;
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
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.MenuCustomer;
import xyz.foodhut.app.classes.NotificationService;
import xyz.foodhut.app.classes.RequestHandler;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.Profile;
import xyz.foodhut.app.ui.ProfileUpdate;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.google.firebase.crash.FirebaseCrash.log;

public class HomeCustomer extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        // implements NavigationView.OnNavigationItemSelectedListener
{

    DrawerLayout drawer;
    SwipeRefreshLayout refreshLayout;

    List<String> locations;
    Map<String,String> ordinates;
    RecyclerView recyclerView;
    ArrayList<xyz.foodhut.app.model.MenuCustomer> arrayList = new ArrayList<>();
    ArrayList<xyz.foodhut.app.model.MenuCustomer> dummyList = new ArrayList<>();
    ArrayList<xyz.foodhut.app.model.MenuCustomer> dummy = new ArrayList<>();
    ArrayList<xyz.foodhut.app.model.MenuCustomer> dummyGlobal = new ArrayList<>();
    String userID = null;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    MenuCustomer menuAdapter;
    private ProgressDialog dialog;
    String name, address, avatar, phone, mDate = "Any Day", mLocation = "", mLocationTemp = "", mType = "all";
    int lunch = 0, bf = 0, dinner = 0, snacks = 0,frozen=0,fit=0,catering=0,vegetarian=0, search = 0;
    int minPrice = 10, maxPrice = 1000;
    int typeFilter = 0;
    int priceFilter = 0;
    int dateFilter = 0;
   private   LocationManager locationManager;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private static final int REQUEST_LOCATION = 1;
    private static int firsVisibleItem;
    ProgressDialog locationDalog;
    double latitude = 0, longitude = 0, dummyLatitude = 0, dummyLongitude = 0;
    boolean gps_check = false;
    int isGPSEnabled = 0;
    int appVersion, currentVersion;
    String forceUpdate;


    private long UPDATE_INTERVAL = 30 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 5000; /* 2 sec */

    Spinner spLocation, spType;
    TextView tvLocation, tvLunch, tvSnacks, tvDinner, tvBF,tvFrozen,tvFit,tvCatering,tvVegetarian, nName, notificationCount, filterCount;
    LinearLayout llLunch, llSnacks, llDinner, llBF,llFrozen,llFit,llCatering,llVegetarian, emptyOrder;
    RelativeLayout rlCart, rlTop;
    LinearLayout llLogout, llFooter;
    View shadowBottom;
    TextView qty, price;
    CardView cvSearch;
    EditText etSearch;
    ImageView ivSearch, nImage;

    ImageView fExplore, fSearch, fOrders, fMore;
    TextView fTvExplore, fTvSearch, fTvOrders, fTvMore, fTvCart;

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
    protected void onResume() {
        notificationCount();
        super.onResume();

        if (StaticConfig.ISORDERED == 1) {
            StaticConfig.ISORDERED = 0;
            rlCart.setVisibility(View.GONE);
            fTvCart.setVisibility(View.GONE);

            getMenu(1);
        }

    //    checkProfile();

        Log.d("Check", "onResume -_-  ");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_customer);

        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

     /*   drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // drawer.addDrawerListener(toggle);
        // toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nView = navigationView.getHeaderView(0);
        nImage = nView.findViewById(R.id.circlePhoto);
        nName = nView.findViewById(R.id.navName);
        llLogout = findViewById(R.id.llLogout);

        */

        refreshLayout = findViewById(R.id.swipeRefreshLayout);

        rlCart = findViewById(R.id.rlCart);
        qty = findViewById(R.id.qty);
        price = findViewById(R.id.price);

        StaticConfig.QTY = 0;
        StaticConfig.SUBTOTAL = 0;
        StaticConfig.ORDERITEMLIST.clear();
        StaticConfig.ITEMQTYLIST.clear();
        StaticConfig.EXTRAQTYLIST.clear();
        StaticConfig.INDEXLIST.clear();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //   getSupportActionBar().setTitle("Menus");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        locations = new ArrayList<String>();
        ordinates=new HashMap<>();

        arrayList = new ArrayList<>();
        dummyList = new ArrayList<>();
        dummy = new ArrayList<>();
        dummyGlobal = new ArrayList<>();
        recyclerView = findViewById(R.id.rvCustomerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuCustomer(this, arrayList, qty, price, rlCart,fTvCart);
        recyclerView.setAdapter(menuAdapter);
        emptyOrder = findViewById(R.id.emptyOrder);

        tvLocation = findViewById(R.id.txtCLocation);
        tvBF = findViewById(R.id.tvBF);
        tvDinner = findViewById(R.id.tvDinner);
        tvLunch = findViewById(R.id.tvLunch);
        tvSnacks = findViewById(R.id.tvSnacks);
        tvFrozen=findViewById(R.id.tvFrozen);
        tvFit=findViewById(R.id.tvFit);
        tvCatering=findViewById(R.id.tvCatering);
        tvVegetarian=findViewById(R.id.tvVegetarian);

        notificationCount = findViewById(R.id.chNotiCount);
        filterCount = findViewById(R.id.chFilterCount);

        llBF = findViewById(R.id.llBF);
        llDinner = findViewById(R.id.llDinner);
        llLunch = findViewById(R.id.llLunch);
        llSnacks = findViewById(R.id.llSnacks);
        llFrozen=findViewById(R.id.llFrozen);
        llFit=findViewById(R.id.llFit);
        llCatering=findViewById(R.id.llCatering);
        llVegetarian=findViewById(R.id.llVegetarian);

        cvSearch = findViewById(R.id.chCvSearch);
        etSearch = findViewById(R.id.chEtSearch);

        ivSearch = findViewById(R.id.chIvSearch);

        fExplore = findViewById(R.id.chExplore);
        fSearch = findViewById(R.id.chSearch);
        fOrders = findViewById(R.id.chOrders);
        fMore = findViewById(R.id.chMore);

        fTvExplore = findViewById(R.id.chTxtExplore);
        fTvSearch = findViewById(R.id.chTxtSearch);
        fTvOrders = findViewById(R.id.chTxtOrders);
        fTvMore = findViewById(R.id.chTxtMore);
        fTvCart = findViewById(R.id.cart);

        rlTop = findViewById(R.id.rlTop);
        llFooter = findViewById(R.id.cvFooter);
        shadowBottom = findViewById(R.id.viewShadow);

        locationDalog = new ProgressDialog(this);
        locationDalog.setMessage("Please Wait...");

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (!isConnected()){

            Toast.makeText(this, "You don't have an active internet connection.", Toast.LENGTH_SHORT).show();
        }

        checkUpdate();

        checkProfile();

        notificationCount();

        startService(new Intent(this, NotificationService.class));

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {

            //   buildLocationRequest();

            EnableGPSAutoMatically();
            buildLocationCallback();

            fusedLocationProviderClient = getFusedLocationProviderClient(this);
        }


        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

     /*   llLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                SharedPreferenceHelper.getInstance(getApplicationContext()).logout();
                firebaseAuth.signOut();
                finish();
            }
        });
        */


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkProfile();
                getMenu(1);

                minPrice = 10;
                maxPrice = 1000;
                mDate = "Any Day";
                mType = "all";

                dateFilter = 0;
                priceFilter = 0;
                typeFilter = 0;

                filterCount.setVisibility(View.GONE);

                lunch = 0;
                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvLunch.setTextColor(getResources().getColor(R.color.gray));

                snacks = 0;
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));

                dinner = 0;
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));

                bf = 0;
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvBF.setTextColor(getResources().getColor(R.color.gray));

                frozen = 0;
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));

                fit = 0;
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvFit.setTextColor(getResources().getColor(R.color.gray));

                catering = 0;
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));

                vegetarian = 0;
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));


            }
        });


        final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        firsVisibleItem = layoutManager.findFirstVisibleItemPosition();

        /*
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int currentFirstVisible=layoutManager.findFirstVisibleItemPosition();
                if (currentFirstVisible>firsVisibleItem){
                    //scrolling up
                    llFooter.setVisibility(View.VISIBLE);
                    shadowBottom.setVisibility(View.VISIBLE);
                    rlTop.setVisibility(View.INVISIBLE);
                }
                else {
                    //scrolling down
                    llFooter.setVisibility(View.INVISIBLE);
                    shadowBottom.setVisibility(View.INVISIBLE);
                    rlTop.setVisibility(View.VISIBLE);
                }

                firsVisibleItem=currentFirstVisible;
            }
        });

        */
    }

    public boolean isConnected(){
        ConnectivityManager cm= (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network= null;
        if (cm != null) {
            network = cm.getActiveNetworkInfo();
        }

        return   (network!=null && network.isConnected());
    }

    public void checkUpdate() {
        FirebaseDatabase.getInstance().getReference("admin/appControl/version")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot != null) {
                            //fetch files from firebase database and push in arraylist
                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            if (hashUser != null) {
                                currentVersion = Integer.parseInt(String.valueOf((long) hashUser.get("currentVersion")));
                                forceUpdate = (String) hashUser.get("forceUpdate");


                                try {
                                    appVersion = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionCode;

                                    if (currentVersion > appVersion && !forceUpdate.equals("yes")) {
                                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeCustomer.this);
                                        builder.setMessage("New update found, Please update FoodHut app for a better experience.")
                                                .setCancelable(true)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(final DialogInterface dialog, final int id) {

                                                        final String appPackageName = getPackageName();

                                                        try {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                        } catch (android.content.ActivityNotFoundException e) {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                        }

                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(final DialogInterface dialog, final int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        final android.app.AlertDialog alert = builder.create();
                                        alert.show();
                                        Log.d("check method", "from alert");
                                    }

                                    if (currentVersion > appVersion && forceUpdate.equals("yes")) {
                                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeCustomer.this);
                                        builder.setMessage("New update found, Update is required to access FoodHut app.")
                                                .setCancelable(false)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(final DialogInterface dialog, final int id) {

                                                        final String appPackageName = getPackageName();

                                                        try {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                        } catch (android.content.ActivityNotFoundException e) {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                        }

                                                        finish();

                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(final DialogInterface dialog, final int id) {
                                                        finish();
                                                    }
                                                });
                                        final android.app.AlertDialog alert = builder.create();
                                        alert.show();
                                        Log.d("check method", "from alert");
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.cancel();
                    }
                });
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
                            status.startResolutionForResult(HomeCustomer.this, 1000);

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //  toast("Success");

    }

    @Override
    public void onConnectionSuspended(int i) {
        //  toast("Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //  toast("Failed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                //  toast(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    /*
    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }
    */

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        getDistance(location.getLatitude(), location.getLongitude());
    }

    public void getMenu(final int check) {

        dummyList.clear();
        arrayList.clear();

        StaticConfig.QTY = 0;
        StaticConfig.SUBTOTAL = 0;
        StaticConfig.TOTAL = 0;
        StaticConfig.ORDERITEMLIST.clear();
        StaticConfig.ITEMQTYLIST.clear();
        StaticConfig.INDEXLIST.clear();
        StaticConfig.EXTRAQTYLIST.clear();
        StaticConfig.SCHEDULEFORONEITEM = "";

        rlCart.setVisibility(View.GONE);
        fTvCart.setVisibility(View.GONE);

        //  recyclerView.setAdapter(menuAdapter);
        //  menuAdapter.notifyDataSetChanged();

      /*  if (dummy.size() > 0)
            menuAdapter.filterList(dummy);
        else
            menuAdapter.filterList(dummyGlobal);
            */
        if (!isConnected()){
            dialog.cancel();
         }

        if (StaticConfig.UID != null) {
            FirebaseDatabase.getInstance().getReference("schedule")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                      //      dialog.dismiss();
                            if (dataSnapshot.getValue() != null) {
                                //fetch files from firebase database and push in arraylist
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    xyz.foodhut.app.model.MenuCustomer customer = snapshot.getValue(xyz.foodhut.app.model.MenuCustomer.class);
                                    dummyList.add(customer);
                                    Log.d("Check list", "onDataChange: " + dummyList.size());
                                }
                                Calendar c = Calendar.getInstance();
                                // From calander get the year, month, day, hour, minute
                                int year = c.get(Calendar.YEAR);
                                int month = c.get(Calendar.MONTH);
                                int day = c.get(Calendar.DAY_OF_MONTH);
                                c.set(year, month, day);
                                Date now = c.getTime();

                                String formattedDate;
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                                formattedDate = dateFormat.format(now);


                                for (int i = 0; i < dummyList.size(); i++) {
                                    try {
                                        Date date1 = dateFormat.parse(formattedDate);
                                        Date date2 = dateFormat.parse(dummyList.get(i).schedule);
                                        Log.d("Check", "date : " + date1 + " " + date2);
                                        if (date1.before(date2) || date1.equals(date2)) {
                                            Log.d("Check", "date comparing : " + dummyList.get(i).schedule);
                                            arrayList.add(dummyList.get(i));
                                        } else {
                                         //   FirebaseDatabase.getInstance().getReference("schedule").child(dummyList.get(i).scheduleId).removeValue();
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                Collections.shuffle(arrayList);

                                //   if (arrayList.get(i).imageUrl == null ) {
                                //       arrayList.remove(i);
                                //   }
                                for (int i = 0; i < arrayList.size() - 1; i++) {
                                    for (int j = 0; j < arrayList.size() - i - 1; j++) {

                                        Date date1 = null;
                                        Date date2 = null;
                                        try {
                                            date1 = dateFormat.parse(arrayList.get(j).schedule);
                                            date2 = dateFormat.parse(arrayList.get(j + 1).schedule);
                                            Log.d("Check", "dates : " + date1 + " " + date2);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        if (date1.after(date2)) {

                                            xyz.foodhut.app.model.MenuCustomer mc2 = new xyz.foodhut.app.model.MenuCustomer();
                                            mc2 = arrayList.get(j);
                                            arrayList.set(j, arrayList.get(j + 1));
                                            arrayList.set(j + 1, mc2);

                                            Log.d("Check", "dates comparing: " + i + " " + j);
                                        }

                                    }
                                }

                                //bind the data in adapter
                                Log.d("Check list", "out datachange: " + arrayList.size());
                                //menuAdapter.notifyDataSetChanged();

                       /*         if (check == 0)
                                    locationOp();
                                else {
                                    filterLocation(mLocation);
                                }

                                */




                                if (check == 0) {

                                    try {
                                        dummyLatitude = Double.parseDouble(SharedPreferenceHelper.getInstance(getApplicationContext()).getLat());
                                        dummyLongitude = Double.parseDouble(SharedPreferenceHelper.getInstance(getApplicationContext()).getLong());

                                        Log.d("check", "lat long: "+dummyLatitude+" "+dummyLongitude);
                                    } catch (NumberFormatException e) {
                                        log(e.getMessage());
                                    }

                                    if (dummyLatitude != 0 && dummyLongitude != 0) {

                                        getDistance(dummyLatitude, dummyLongitude);
                                        tvLocation.setText("Current location");
                                        getLocation(dummyLatitude,dummyLongitude,tvLocation);
                                        locationDalog.cancel();

                                    } else {

                                        if (SharedPreferenceHelper.getInstance(HomeCustomer.this).getLOCATION().isEmpty())
                                            locationOp();

                                        else {
                                            mLocation = SharedPreferenceHelper.getInstance(HomeCustomer.this).getLOCATION();

                                            Log.d("check", "ordinates: " + ordinates.size());

                                            if (ordinates.size() > 0) {
                                                String latLong = ordinates.get(mLocation);
                                                String parts[] = latLong.split(",");
                                                Double lat = Double.valueOf(parts[0]);
                                                Double longi = Double.valueOf(parts[1]);
                                                getDistance(lat, longi);
                                            } else
                                                filterLocation(mLocation);
                                            tvLocation.setText(mLocation);
                                        }
                                    }
                                }

                                if (check == 1) {

                                    try {
                                        dummyLatitude = Double.parseDouble(SharedPreferenceHelper.getInstance(getApplicationContext()).getLat());
                                        dummyLongitude = Double.parseDouble(SharedPreferenceHelper.getInstance(getApplicationContext()).getLong());

                                        Log.d("check", "lat long: "+dummyLatitude+" "+dummyLongitude);
                                    } catch (NumberFormatException e) {
                                        log(e.getMessage());
                                    }

                                     if ((dummyLatitude!= 0 && dummyLongitude != 0)) {
                                         getDistance(dummyLatitude, dummyLongitude);
                                         Log.d("check", "onDataChange: check=1,distance");
                                     }
                                    else {
                                        mLocation = SharedPreferenceHelper.getInstance(HomeCustomer.this).getLOCATION();

                                        if (ordinates.size()>0) {
                                            String latLong = ordinates.get(mLocation);
                                            String parts[] = latLong.split(",");
                                            Double lat = Double.valueOf(parts[0]);
                                            Double longi = Double.valueOf(parts[1]);
                                            getDistance(lat, longi);
                                        }
                                        else
                                            filterLocation(mLocation);

                                            tvLocation.setText(mLocation);

                                    }
                                }


                            } else {
                                emptyOrder.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.cancel();
                        }
                    });


        }

        fExplore.setImageDrawable(getResources().getDrawable(R.drawable.icon_explore_red));
        fTvExplore.setTextColor(getResources().getColor(R.color.colorPrimary));

        fSearch.setImageDrawable(getResources().getDrawable(R.drawable.search_64));
        fTvSearch.setTextColor(getResources().getColor(R.color.grayDark));

        cvSearch.setVisibility(View.INVISIBLE);
        etSearch.setText("");

        refreshLayout.setRefreshing(false);

        dialog.cancel();
    }

    private void filter(String text) {
        ArrayList<xyz.foodhut.app.model.MenuCustomer> filteredList = new ArrayList<>();


        if (dummy.size() == 0) {

            if (text.length() > 0) {

                for (xyz.foodhut.app.model.MenuCustomer item : dummyGlobal) {
                    if (item.name.toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                menuAdapter.filterList(filteredList);
            } else
                menuAdapter.filterList(dummyGlobal);

        } else {

            if (text.length() > 0) {
                for (xyz.foodhut.app.model.MenuCustomer item : dummy) {
                    if (item.name.toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                menuAdapter.filterList(filteredList);
            } else
                menuAdapter.filterList(dummy);
        }

    }


    public void notificationCount() {
        final int[] count = new int[1];
        count[0] = 0;
        FirebaseDatabase.getInstance().getReference("customers/" + StaticConfig.UID + "/notifications/new")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot != null) {
                            //fetch files from firebase database and push in arraylist
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                count[0]++;
                                Log.d("Check", "list noti: " + count[0]);
                            }

                            if (count[0] > 0) {
                                notificationCount.setVisibility(View.VISIBLE);
                                notificationCount.setText(String.valueOf(count[0]));
                                Log.d("Check", "list noti: " + count[0]);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.cancel();
                    }
                });


        FirebaseDatabase.getInstance().getReference("customers/" + StaticConfig.UID + "/notifications")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        notificationCount.setVisibility(View.GONE);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private void locationUpdate() {
        if (ActivityCompat.checkSelfPermission(HomeCustomer.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (HomeCustomer.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomeCustomer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            return;
        }


        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                locationDalog.cancel();
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();


                        if (isGPSEnabled == 1) {

                            SharedPreferenceHelper.getInstance(getApplicationContext()).setLatLong(String.valueOf(latitude),String.valueOf(longitude));

                            tvLocation.setText("Current location");
                            getLocation(latitude,longitude,tvLocation);
                            getDistance(latitude, longitude);
                        }
                    } else {
                        // locationCall();
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

    public void getDistance(Double lat1, Double lon1) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        dummyGlobal.clear();

        // for (MenuCustomer wp : obj) {
        for (int i = 0; i < arrayList.size(); i++) {
            xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
            mc = arrayList.get(i);
            Log.d("check", "in typeFilter:tvLocation type n/e " + arrayList.size() + " " + mc.name);

            Location loc2 = new Location("");
            loc2.setLatitude(Double.parseDouble(mc.latitude));
            loc2.setLongitude(Double.parseDouble(mc.longitude));
            float distanceInMeters = loc1.distanceTo(loc2);
            Log.d("check", "getDistance: " + distanceInMeters);

            if (distanceInMeters < 5000) {

                Log.d("check", "in typeFilter: inside Distance");
                dummyGlobal.add(mc);
            }
        }


        if (dummyGlobal.size() > 0) {
            emptyOrder.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            menuAdapter = new MenuCustomer(this, dummyGlobal, qty, price, rlCart,fTvCart);
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        } else {
            emptyOrder.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }


    public void goToCart(View view) {
        if (StaticConfig.QTY > 0) {
            startActivity(new Intent(this, AddOrderMultiple.class));
        } else {
            Toast.makeText(this, "Your Cart Is Empty!", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearCart(View view) {
        StaticConfig.QTY = 0;
        StaticConfig.SUBTOTAL = 0;
        StaticConfig.TOTAL = 0;
        StaticConfig.ORDERITEMLIST.clear();
        StaticConfig.ITEMQTYLIST.clear();
        StaticConfig.INDEXLIST.clear();
        StaticConfig.SCHEDULEFORONEITEM = "";

        rlCart.setVisibility(View.GONE);
        fTvCart.setVisibility(View.GONE);

        if (dummy.size() > 0) {
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
            menuAdapter.filterList(dummy);
        } else {
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
            menuAdapter.filterList(dummyGlobal);
        }
    }

    public void openDrawer(View view) {
        drawer.openDrawer(Gravity.END);
    }

    public void checkProfile() {
        FirebaseDatabase.getInstance().getReference().child("customers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    name = (String) hashUser.get("name");
                    address = (String) hashUser.get("address");
                    phone = (String) hashUser.get("phone");
                    StaticConfig.LATITUDE = (String) hashUser.get("latitude");
                    StaticConfig.LONGITUDE = (String) hashUser.get("longitude");
                    StaticConfig.ADDRESS = address;
                    avatar = (String) hashUser.get("avatar");

                    Log.d("check", "name in customer: " + name + " " + StaticConfig.LONGITUDE);

                    if (name.equals("name") || address.equals("address")) {
                        Intent y = new Intent(HomeCustomer.this, ProfileUpdate.class);
                        y.putExtra("type", "customer");
                        y.putExtra("avatar", "default");
                        y.putExtra("isUpdate", "false");
                        startActivity(y);
                        finish();
                    }
                    else {
                        getAreaList();
                    }
                  /*  else {
                        nName.setText(name);
                        if (!avatar.equals("default"))
                            Picasso.get().load(avatar).placeholder(R.drawable.default_avatar).into(nImage);
                    }
                    */
                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void selectLocation(View view) {

        locationOp();
    }

    public void getAreaList() {
        FirebaseDatabase.getInstance().getReference("admin/area").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //fetch files from firebase database and push in arraylist

                if (dataSnapshot.getValue() != null) {
                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        ordinates.put(dsp.getKey(), String.valueOf(dsp.getValue()));

                        Log.d("check", "getAreaList: ordinate " + dsp.getKey()+" "+dsp.getValue());
                    }

                    getMenu(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void locationOp() {


        //  getArea();
        dummyLatitude = 0;
        dummyLongitude = 0;

        //    getAreaList();

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_select_location, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflate);

        final String[] selected = new String[1];
        //  List<String> locations = new ArrayList<String>();
        // locations.add("All");

        //get these location from providers area

        //   locations.add("Mirpur");
        //  locations.add("Savar");
        //  locations.add("Gazipur");
        //  locations.add("Gulshan");

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locations);

        FirebaseDatabase.getInstance().getReference("admin/area").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //fetch files from firebase database and push in arraylist

                if (dataSnapshot.getValue() != null) {
                    locations.clear();
                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        locations.add(String.valueOf(dsp.getKey())); //add result into array list
                      //  ordinates.put(dsp.getKey(), String.valueOf(dsp.getValue()));

                        adapter.notifyDataSetChanged();
                        Log.d("check", "onDataChange: date " + dsp.getKey()+" "+dsp.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        RelativeLayout locate = inflate.findViewById(R.id.llLocateMe);
        final TextView tvLocate = inflate.findViewById(R.id.tvLocateMe);
        final Spinner location = inflate.findViewById(R.id.spMyLocation);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLocation = location.getSelectedItem().toString();

                location.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DBHelper dbHelper=new DBHelper(contex);
                        tvLocation.setText(mLocation);

                        SharedPreferenceHelper.getInstance(HomeCustomer.this).setLocation(mLocation);

                        String latLong=ordinates.get(mLocation);
                        String parts[]=latLong.split(",");
                        Double lat= Double.valueOf(parts[0]);
                        Double longi=Double.valueOf(parts[1]);
                        getDistance(lat,longi);

                        SharedPreferenceHelper.getInstance(getApplicationContext()).setLatLong("0","0");


                        //  filterLocation(mLocation);
                        // typeFilter+=1;
                        cvSearch.setVisibility(View.GONE);
                        etSearch.setText("");
                        isGPSEnabled = 0;
                        // Toast.makeText(contex, "Menu Added in Schedule", Toast.LENGTH_SHORT).show();
                    }
                })
             /*   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })*/;
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.cancel();

                //  dialog.setCancelable(false);
                locationDalog.show();
                try {
                    dummyLatitude = Double.parseDouble(SharedPreferenceHelper.getInstance(getApplicationContext()).getLat());
                    dummyLongitude = Double.parseDouble(SharedPreferenceHelper.getInstance(getApplicationContext()).getLong());

                    Log.d("check", "lat long: "+dummyLatitude+" "+dummyLongitude);
                }
                catch (NumberFormatException e){
                    log(e.getMessage());
                }
                if ((latitude != 0 && longitude != 0)) {
                    SharedPreferenceHelper.getInstance(getApplicationContext()).setLatLong(String.valueOf(latitude),String.valueOf(longitude));

                    getDistance(latitude, longitude);
                    tvLocation.setText("Current location");
                    getLocation(latitude,longitude,tvLocation);
                    locationDalog.cancel();
                } else {
                    EnableGPSAutoMatically();
                    //  buildLocationCallback();
                    isGPSEnabled = 1;

                    locationUpdate();
                    //  locationDalog.cancel();
                }

                //  startLocationUpdates();
/*
                new Handler().postDelayed(new Runnable() {
                    public void run() {

                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            // locationDalog.cancel();
                            //  buildAlertMessageNoGps();
                            EnableGPSAutoMatically();
                            Log.d("check method", "gps alert");
                        }
                    }
                }, 5 * 1000);
                */

                //  if(!mLocationTemp.equals(""))
                //  tvLocate.setText(mLocationTemp);
            }
        });
    }

    public void getArea() {

        locations.clear();

        FirebaseDatabase.getInstance().getReference("admin/area").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //fetch files from firebase database and push in arraylist

                if (dataSnapshot.getValue() != null) {
                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        locations.add(String.valueOf(dsp.getKey())); //add result into array list

                        Log.d("check", "onDataChange: date " + dsp.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setFilters(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_filter_price, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflate);


        final String[] minValue = new String[1];
        final String[] maxValue = new String[1];
        final TextView min = inflate.findViewById(R.id.tvMinValue);
        final TextView max = inflate.findViewById(R.id.tvMaxValue);
        final RangeBar rangeBar = inflate.findViewById(R.id.rangeBar);

        rangeBar.setRangePinsByValue(minPrice, maxPrice);

        minValue[0] = "৳ " + minPrice;
        maxValue[0] = "৳ " + maxPrice;

        min.setText(minValue[0]);
        max.setText(maxValue[0]);

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex, String leftPinValue, String rightPinValue) {
                minValue[0] = "৳ " + leftPinValue;
                maxValue[0] = "৳ " + rightPinValue;

                min.setText(minValue[0]);
                max.setText(maxValue[0]);

                minPrice = Integer.parseInt(leftPinValue);
                maxPrice = Integer.parseInt(rightPinValue);

                Log.d("check", "price: " + minPrice + " " + maxPrice);
            }

        });

        final String[] selected = new String[1];
        List<String> dates = new ArrayList<String>();
        // locations.add("All");

        //get these location from providers area

        dates.add("Any Day");
        dates.add("Today");
        dates.add("Tomorrow");
        dates.add("Day After Tomorrow");

        final Spinner date = inflate.findViewById(R.id.spDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date.setAdapter(adapter);

        if (!mDate.equals("Any Day")) {
            if (mDate.equals("Today")) {
                date.setSelection(1);
            }
            if (mDate.equals("Tomorrow")) {
                date.setSelection(2);
            }
            if (mDate.equals("Day After Tomorrow")) {
                date.setSelection(3);
            }
        }

        date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDate = date.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (maxPrice > minPrice) {

                            if (mDate.equals("Any Day")) {
                                filterPrice(minPrice, maxPrice);
                                dateFilter = 0;
                            } else {
                                filterPrice(minPrice, maxPrice, mDate);
                                dateFilter = 1;
                            }


                            if (minPrice > 10 || maxPrice < 1000) {
                                priceFilter = 1;
                            } else {
                                priceFilter = 0;
                            }

                            if (priceFilter + typeFilter + dateFilter > 0) {
                                filterCount.setVisibility(View.VISIBLE);
                                filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                            } else
                                filterCount.setVisibility(View.GONE);
                        } else
                            Toast.makeText(HomeCustomer.this, "Please select Price Range", Toast.LENGTH_SHORT).show();

                        // Toast.makeText(contex, "Menu Added in Schedule", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void getType(View view) {
        if (view.getId() == R.id.llLunch) {

            if (lunch == 1) {
                lunch = 0;
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);
                lunch = 1;
                bf = 0;
                dinner = 0;
                snacks = 0;
                frozen=0;
                fit=0;
                catering=0;
                vegetarian=0;

                mType = "lunch";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.white));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));
            }

        }
        if (view.getId() == R.id.llSnacks) {

            if (snacks == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                snacks = 0;
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 0;
                dinner = 0;
                snacks = 1;
                frozen=0;
                fit=0;
                catering=0;
                vegetarian=0;

                mType = "snacks";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.white));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));

            }
        }
        if (view.getId() == R.id.llDinner) {

            if (dinner == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                dinner = 0;
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 0;
                dinner = 1;
                snacks = 0;
                frozen=0;
                fit=0;
                catering=0;
                vegetarian=0;

                mType = "dinner";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.white));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));

            }


        }
        if (view.getId() == R.id.llBF) {

            if (bf == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                bf = 0;
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 1;
                dinner = 0;
                snacks = 0;
                frozen=0;
                fit=0;
                catering=0;
                vegetarian=0;

                mType = "breakfast";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.white));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));

            }
        }

        if (view.getId() == R.id.llFrozen) {

            if (frozen == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                frozen = 0;
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 0;
                dinner = 0;
                snacks = 0;
                frozen=1;
                fit=0;
                catering=0;
                vegetarian=0;

                mType = "frozen food";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.white));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));

            }
        }

        if (view.getId() == R.id.llFit) {

            if (fit == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                fit = 0;
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 0;
                dinner = 0;
                snacks = 0;
                frozen=0;
                fit=1;
                catering=0;
                vegetarian=0;

                mType = "fit food";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.white));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));

            }
        }

        if (view.getId() == R.id.llCatering) {

            if (catering == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                catering = 0;
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 0;
                dinner = 0;
                snacks = 0;
                frozen=0;
                fit=0;
                catering=1;
                vegetarian=0;

                mType = "catering";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.white));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));

            }
        }

        if (view.getId() == R.id.llVegetarian) {

            if (vegetarian == 1) {
                typeFilter = 0;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                vegetarian = 0;
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                tvVegetarian.setTextColor(getResources().getColor(R.color.gray));
                mType = "all";
                filterType("all");
            } else {
                typeFilter = 1;

                if (priceFilter + typeFilter + dateFilter > 0) {
                    filterCount.setVisibility(View.VISIBLE);
                    filterCount.setText(String.valueOf(priceFilter + typeFilter + dateFilter));
                } else
                    filterCount.setVisibility(View.GONE);

                lunch = 0;
                bf = 0;
                dinner = 0;
                snacks = 0;
                frozen=0;
                fit=0;
                catering=0;
                vegetarian=1;

                mType = "vegetarian";
                filterType(mType);
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");

                llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFrozen.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llFit.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llCatering.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
                llVegetarian.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));

                tvLunch.setTextColor(getResources().getColor(R.color.gray));
                tvSnacks.setTextColor(getResources().getColor(R.color.gray));
                tvDinner.setTextColor(getResources().getColor(R.color.gray));
                tvBF.setTextColor(getResources().getColor(R.color.gray));
                tvFrozen.setTextColor(getResources().getColor(R.color.gray));
                tvFit.setTextColor(getResources().getColor(R.color.gray));
                tvCatering.setTextColor(getResources().getColor(R.color.gray));
                tvVegetarian.setTextColor(getResources().getColor(R.color.white));

            }
        }

    }

    public void filterPrice(int min, int max) {
        ArrayList<xyz.foodhut.app.model.MenuCustomer> dummy2 = new ArrayList<>();

        Log.d("check", "price:any " + min + " " + max + " " + dummyGlobal.size());

        if (dummyGlobal.size() > 0) {
            // for (MenuCustomer wp : obj) {
            dummy2.clear();
            dummy2 = dummyGlobal;
            dummy.clear();

            if (!mType.equals("all")) {
                for (int i = 0; i < dummy2.size(); i++) {
                    xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                    mc = dummy2.get(i);
                    // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                    if ((Integer.parseInt(mc.price) >= min && Integer.parseInt(mc.price) <= max) && mc.type.toLowerCase(Locale.getDefault()).equals(mType)) {

                        Log.d("check", "in typeFilter price dummy: iffff " + min + " " + max);
                        dummy.add(mc);
                    }

                }
                Log.d("check", "in price Filter dummy: size " + dummy.size());
                if (dummy.size() > 0) {
                    emptyOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    menuAdapter.filterList(dummy);
                } else {
                    emptyOrder.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                for (int i = 0; i < dummy2.size(); i++) {
                    xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                    mc = dummy2.get(i);
                    // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                    if (Integer.parseInt(mc.price) >= min && Integer.parseInt(mc.price) <= max) {

                        Log.d("check", "in typeFilter price dummy: iffff " + min + " " + max);
                        dummy.add(mc);
                    }
                }
                Log.d("check", "in price Filter dummy: size " + dummy.size());
                if (dummy.size() > 0) {
                    emptyOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    menuAdapter.filterList(dummy);
                } else {
                    emptyOrder.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            //  typeFilter+=1;

        }
    }

    public void filterPrice(int min, int max, String date) {
        ArrayList<xyz.foodhut.app.model.MenuCustomer> dummy2 = new ArrayList<>();
        Log.d("check", "price:date " + min + " " + max + " " + date + " " + dummyGlobal.size());

        // Get the calander
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = 0;
        if (date.equals("Today")) {
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        if (date.equals("Tomorrow")) {
            day = c.get(Calendar.DAY_OF_MONTH) + 1;
        }
        if (date.equals("Day After Tomorrow")) {
            day = c.get(Calendar.DAY_OF_MONTH) + 2;
        }
        c.set(year, month, day);
        String date2 = day + "-" + (month + 1) + "-" + year;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        try {
            date1 = dateFormat.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");

        formattedDate = sdtf.format(date1);
        Log.d("check", "onClick: date " + date2 + " " + formattedDate);


        if (dummyGlobal.size() > 0) {
            // for (MenuCustomer wp : obj) {
            dummy2.clear();
            dummy2 = dummyGlobal;
            dummy.clear();

            if (!mType.equals("all")) {
                for (int i = 0; i < dummy2.size(); i++) {
                    xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                    mc = dummy2.get(i);
                    // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                    if ((Integer.parseInt(mc.price) >= min && Integer.parseInt(mc.price) <= max) && mc.type.toLowerCase(Locale.getDefault()).equals(mType) && mc.schedule.equals(formattedDate)) {

                        Log.d("check", "in Filter price all type dummy: iffff " + min + " " + max);
                        dummy.add(mc);
                    }

                }
                Log.d("check", "in price Filter dummy: size " + dummy.size());
                if (dummy.size() > 0) {
                    emptyOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    menuAdapter.filterList(dummy);
                } else {
                    emptyOrder.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            } else {
                for (int i = 0; i < dummy2.size(); i++) {
                    xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                    mc = dummy2.get(i);
                    // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                    if ((Integer.parseInt(mc.price) >= min && Integer.parseInt(mc.price) <= max) && mc.schedule.equals(formattedDate)) {

                        Log.d("check", "in typeFilter price not all type dummy: iffff " + min + " " + max);
                        dummy.add(mc);
                    }
                }
                Log.d("check", "in price Filter dummy: size " + dummy.size());
                if (dummy.size() > 0) {
                    emptyOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    menuAdapter.filterList(dummy);
                } else {
                    emptyOrder.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }

            //  typeFilter+=1;

        }
    }

    public void filterLocation(String value) {
        Log.d("check", "in typeFilter: Location" + " " + value + " " + mLocation + " " + mType + " " + dummyGlobal.size());

        value = value.toLowerCase(Locale.getDefault());

        if (value.equals("all")) {
            dummyGlobal = arrayList;
            //  menuAdapter.filterList(arrayList);
            menuAdapter = new MenuCustomer(this, arrayList, qty, price, rlCart,fTvCart);
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        } else {
            dummyGlobal.clear();

            for (int i = 0; i < arrayList.size(); i++) {
                xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                mc = arrayList.get(i);
                // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)) {

                    Log.d("check", "in typeFilter location list: iffff " + mc.providerAddress.toLowerCase(Locale.getDefault()) + " " + value);

                    dummyGlobal.add(mc);
                }

            }

            Log.d("check", "in location Filter dummy size " + dummyGlobal.size());
            //  menuAdapter.filterList(dummyGlobal);

            if (dummyGlobal.size() > 0) {
                emptyOrder.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                menuAdapter = new MenuCustomer(this, dummyGlobal, qty, price, rlCart,fTvCart);
                recyclerView.setAdapter(menuAdapter);
                menuAdapter.notifyDataSetChanged();
            } else {
                emptyOrder.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    public void filterType(String value) {
        Log.d("check", "in typeFilter: type" + " " + value + " " + mLocation + " " + mType + " " + dummyGlobal.size());
        Log.d("check", "in typeFilter type  " + minPrice + " " + maxPrice);

        // Get the calander
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = 0;
        if (mDate.equals("Today")) {
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        if (mDate.equals("Tomorrow")) {
            day = c.get(Calendar.DAY_OF_MONTH) + 1;
        }
        if (mDate.equals("Day After Tomorrow")) {
            day = c.get(Calendar.DAY_OF_MONTH) + 2;
        }
        c.set(year, month, day);
        String date2 = day + "-" + (month + 1) + "-" + year;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        try {
            date1 = dateFormat.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");

        formattedDate = sdtf.format(date1);
        Log.d("check", "onClick: date " + date2 + " " + formattedDate);

        value = value.toLowerCase(Locale.getDefault());
        ArrayList<xyz.foodhut.app.model.MenuCustomer> dummy2 = new ArrayList<>();
        if (dummyGlobal.size() > 0) {

            dummy2.clear();
            dummy2 = dummyGlobal;
            dummy.clear();

            if (value.equals("all")) {

                if (mDate.equals("Any Day")) {

                    for (int i = 0; i < dummy2.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = dummy2.get(i);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if (Integer.parseInt(mc.price) >= minPrice && Integer.parseInt(mc.price) <= maxPrice) {

                            Log.d("check", "in typeFilter price dummy: all any " + minPrice + " " + maxPrice);
                            dummy.add(mc);
                        }
                    }
                    //  menuAdapter.filterList(dummy);
                    Log.d("check", "in type Filter all any dummy: size " + dummy.size());

                } else {

                    for (int i = 0; i < dummy2.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = dummy2.get(i);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if (Integer.parseInt(mc.price) >= minPrice && Integer.parseInt(mc.price) <= maxPrice && mc.schedule.equals(formattedDate)) {

                            Log.d("check", "in typeFilter price dummy: all not any " + minPrice + " " + maxPrice);
                            dummy.add(mc);
                        }
                    }

                    Log.d("check", "in type Filter all not any dummy: size " + dummy.size());
                }

                if (dummy.size() > 0) {
                    emptyOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    menuAdapter.filterList(dummy);
                } else {
                    emptyOrder.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }


            } else {

                if (mDate.equals("Any Day")) {

                    for (int i = 0; i < dummy2.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = dummy2.get(i);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if ((Integer.parseInt(mc.price) >= minPrice && Integer.parseInt(mc.price) <= maxPrice) && mc.type.toLowerCase(Locale.getDefault()).equals(value)) {

                            Log.d("check", "in typeFilter price dummy: type any " + minPrice + " " + maxPrice);
                            dummy.add(mc);
                        }

                    }

                    Log.d("check", "in type Filter not all any dummy: size " + dummy.size());
                    //  menuAdapter.filterList(dummy);

                } else {

                    for (int i = 0; i < dummy2.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = dummy2.get(i);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if ((Integer.parseInt(mc.price) >= minPrice && Integer.parseInt(mc.price) <= maxPrice) && mc.type.toLowerCase(Locale.getDefault()).equals(value) && mc.schedule.equals(formattedDate)) {

                            Log.d("check", "in typeFilter price dummy: type date " + minPrice + " " + maxPrice);
                            dummy.add(mc);
                        }

                    }

                    Log.d("check", "in type Filter not all not any dummy: size " + dummy.size());
                    //    menuAdapter.filterList(dummy);

                }

                if (dummy.size() > 0) {
                    emptyOrder.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    menuAdapter.filterList(dummy);
                } else {
                    emptyOrder.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }
        }
    }

    public void footerAction(View view) {
        if (view.getId() == R.id.chExplore || view.getId() == R.id.chTxtExplore) {
            //reset home page and filters

            etSearch.setText("");
            cvSearch.setVisibility(View.GONE);
            filterCount.setVisibility(View.GONE);
            minPrice = 10;
            maxPrice = 1000;
            priceFilter = 0;
            typeFilter = 0;

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));

            menuAdapter.filterList(dummyGlobal);

            fExplore.setImageDrawable(getResources().getDrawable(R.drawable.icon_explore_red));
            fTvExplore.setTextColor(getResources().getColor(R.color.colorPrimary));

            fSearch.setImageDrawable(getResources().getDrawable(R.drawable.search_64));
            fTvSearch.setTextColor(getResources().getColor(R.color.grayDark));

        }
        if (view.getId() == R.id.chSearch || view.getId() == R.id.chTxtSearch) {

            if (search == 0) {
                search = 1;
                cvSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();

                fExplore.setImageDrawable(getResources().getDrawable(R.drawable.explore_64));
                fTvExplore.setTextColor(getResources().getColor(R.color.grayDark));

                fSearch.setImageDrawable(getResources().getDrawable(R.drawable.icon_search_ight_red));
                fTvSearch.setTextColor(getResources().getColor(R.color.colorPrimary));

            } else if (search == 1) {
                search = 0;
                cvSearch.setVisibility(View.GONE);

                fExplore.setImageDrawable(getResources().getDrawable(R.drawable.icon_explore_red));
                fTvExplore.setTextColor(getResources().getColor(R.color.colorPrimary));

                fSearch.setImageDrawable(getResources().getDrawable(R.drawable.search_64));
                fTvSearch.setTextColor(getResources().getColor(R.color.gray));

              /*  if (dummy.size() == 0)
                    menuAdapter = new MenuCustomer(this, arrayList,qty,price,rlCart);
                else
                    menuAdapter = new MenuCustomer(this, dummy,qty,price,rlCart);

                recyclerView.setAdapter(menuAdapter);
                menuAdapter.notifyDataSetChanged();
                */

            }
            //etSearch.requestFocus();
            //  dummy.clear();
            ivSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    search = 0;
                    etSearch.setText("");

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    }
                    cvSearch.setVisibility(View.GONE);

                    fExplore.setImageDrawable(getResources().getDrawable(R.drawable.icon_explore_red));
                    fTvExplore.setTextColor(getResources().getColor(R.color.colorPrimary));

                    fSearch.setImageDrawable(getResources().getDrawable(R.drawable.search_64));
                    fTvSearch.setTextColor(getResources().getColor(R.color.grayDark));
                    /*
                    String value = etSearch.getText().toString().trim();
                    if (!value.equals("")) {
                        // for (MenuCustomer wp : obj) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                            mc = arrayList.get(i);
                            Log.d("check", "in search:tvName " + arrayList.size() + " " + mc.name);
                            // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                            if (mc.name.toLowerCase(Locale.getDefault()).contains(value)) {

                                Log.d("check", "in typeFilter: iffff");
                                dummy.add(mc);

                                cvSearch.setVisibility(View.GONE);
                            }
                        }
                    }
                    */
                }
            });

        }
        if (view.getId() == R.id.chNoti) {

            Intent intent = new Intent(this, NotificationCustomer.class);
            intent.putExtra("user", "customer");
            startActivity(intent);
        }

        if (view.getId() == R.id.chOrders || view.getId() == R.id.chTxtOrders) {

            // Intent intent = new Intent(this, OrdersDateCustomer.class);
            Intent intent = new Intent(this, OrdersCustomer.class);
            intent.putExtra("type", "customer");
            startActivity(intent);

        }

        if (view.getId() == R.id.chMore || view.getId() == R.id.chTxtMore) {
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("type", "customer");
            intent.putExtra("isUpdate", "true");
            intent.putExtra("name", name);
            intent.putExtra("address", address);
            intent.putExtra("phone", phone);
            intent.putExtra("avatar", avatar);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!drawer.isDrawerOpen(GravityCompat.START)) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want Exit FoodHut?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final android.app.AlertDialog alert = builder.create();
            alert.show();
            Log.d("check method", "from alert");
        } else {
            super.onBackPressed();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_customer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //  if (id == R.id.action_settings) {
        //       return true;
        //   }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.cmProfile) {
            // Handle the camera action
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("type", "customer");
            intent.putExtra("name", name);
            intent.putExtra("isUpdate", "true");
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            intent.putExtra("avatar", avatar);
            startActivity(intent);

        } else if (id == R.id.cmOrders) {
            // Intent intent = new Intent(this, OrdersDateCustomer.class);
            Intent intent = new Intent(this, OrdersCustomer.class);
            intent.putExtra("type", "customer");
            startActivity(intent);

        } else if (id == R.id.cmWishList) {
            Intent intent = new Intent(this, Favourites.class);
            startActivity(intent);

        }
      /*  else if (id == R.id.cmLogout) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            SharedPreferenceHelper.getInstance(this).logout();
            firebaseAuth.signOut();
            finish();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    */


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
        if (ActivityCompat.checkSelfPermission(HomeCustomer.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (HomeCustomer.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomeCustomer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                tvLocation.setText("Your location");
                getDistance(latitude, longitude);

                Log.d("check", "location network " + latitude + " " + longitude);
            } else if (location1 != null) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    tvLocation.setText("Your location");
                    getDistance(latitude, longitude);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                Log.d("check", "location gps " + latitude + " " + longitude);

            } else if (location2 != null) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    tvLocation.setText("Your location");
                    getDistance(latitude, longitude);

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

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Please turn on your GPS and set to 'High Accuracy'")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        //  gps_check = true;

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
        Log.d("check method", "from alert");
    }


    public void getLocation(double lati, double longi, final TextView tvLocate) {

        //  longi=90.353655;
        //   lati=23.795604;

        final String[] subLocality = new String[1];
        final String[] area_level_3 = new String[1];

        String reqUrl = "https://barikoi.xyz/v1/api/search/reverse/geocode/MTEzMjo0Nk03MEcyNjRC/place?longitude=" + longi + "&latitude=" + lati;
        StringRequest sr = new StringRequest(Request.Method.GET, reqUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("check", "barikoi: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray Results = jsonObject.getJSONArray("Place");
                            Log.d("check", "barikoi: Place" + Results.toString());
                            JSONObject zero = Results.getJSONObject(0);
                            Log.d("check", "barikoi: zero" + zero.toString());

                            String area = zero.getString("area");

                            if (!area.equals("") && !area.isEmpty()) {
                              //  mLocation = area;
                             //   mLocationTemp = area;
                                tvLocate.setText(area);
                            } else {
                               // Toast.makeText(HomeCustomer.this, "Sorry we can't detect your location.", Toast.LENGTH_SHORT).show();

                                tvLocate.setText("Current Location");
                            }

                            Log.d("check", "barikoi address: " + area);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                     //   if (mLocation.equals("")) {
                      //      Toast.makeText(HomeCustomer.this, "Sorry we can't detect your location.", Toast.LENGTH_SHORT).show();
                     //   }
                        tvLocate.setText("Current Location");
                    }
                });

        RequestHandler.getmInstance(this).addToRequestQueue(sr);
    }


}
