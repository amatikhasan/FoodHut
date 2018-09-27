package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.MenuCustomer;
import xyz.foodhut.app.classes.RequestHandler;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.MainActivity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeCustomer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;

    RecyclerView recyclerView;
    ArrayList<xyz.foodhut.app.model.MenuCustomer> arrayList = new ArrayList<>();
    ArrayList<xyz.foodhut.app.model.MenuCustomer> dummy = new ArrayList<>();
    String userID = null;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    MenuCustomer menuAdapter;
    private ProgressDialog dialog;
    String name, address, avatar, mLocation = "", mLocationTemp = "", mType = "";
    int lunch = 0, bf = 0, dinner = 0, snacks = 0, search = 0;
    int minPrice, maxPrice;
    LocationManager locationManager;
    private FusedLocationProviderClient client;
    double lattitude, longitude;
    boolean gps_check;
    int provider;

    Spinner spLocation, spType;
    TextView tvLocation, tvLunch, tvSnacks, tvDinner, tvBF, nName;
    LinearLayout llLunch, llSnacks, llDinner, llBF, emptyOrder;
    CardView cvSearch;
    EditText etSearch;
    ImageView ivSearch, nImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_customer);

        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // drawer.addDrawerListener(toggle);
        // toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nView = navigationView.getHeaderView(0);
        nImage = nView.findViewById(R.id.circlePhoto);
        nName = nView.findViewById(R.id.navName);


        //   getSupportActionBar().setTitle("Menus");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        client = LocationServices.getFusedLocationProviderClient(this);

        arrayList = new ArrayList<>();
        dummy = new ArrayList<>();
        recyclerView = findViewById(R.id.rvCustomerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuCustomer(this, arrayList);
        recyclerView.setAdapter(menuAdapter);
        emptyOrder = findViewById(R.id.emptyOrder);

        tvLocation = findViewById(R.id.txtCLocation);
        tvBF = findViewById(R.id.tvBF);
        tvDinner = findViewById(R.id.tvDinner);
        tvLunch = findViewById(R.id.tvLunch);
        tvSnacks = findViewById(R.id.tvSnacks);

        llBF = findViewById(R.id.llBF);
        llDinner = findViewById(R.id.llDinner);
        llLunch = findViewById(R.id.llLunch);
        llSnacks = findViewById(R.id.llSnacks);

        cvSearch = findViewById(R.id.chCvSearch);
        etSearch = findViewById(R.id.chEtSearch);
        ivSearch = findViewById(R.id.chIvSearch);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        checkProfile();

        if (StaticConfig.UID != null) {
            FirebaseDatabase.getInstance().getReference("schedule")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            if (dataSnapshot.getValue() != null) {
                                //fetch files from firebase database and push in arraylist
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    xyz.foodhut.app.model.MenuCustomer customer = snapshot.getValue(xyz.foodhut.app.model.MenuCustomer.class);
                                    arrayList.add(customer);
                                    Log.d("Check list", "onDataChange: " + arrayList.size());
                                }
                                for (int i = 0; i < arrayList.size(); i++) {
                                    if (arrayList.get(i).imageUrl == null || arrayList.get(i).imageUrl == null) {
                                        arrayList.remove(i);
                                    }
                                }
                                Collections.reverse(arrayList);
                                //bind the data in adapter
                                Log.d("Check list", "out datachange: " + arrayList.size());
                                menuAdapter.notifyDataSetChanged();
                            } else {
                                emptyOrder.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }

      /*  final String[] selected = new String[1];
        final List<String> location = new ArrayList<String>();
        location.add("All");
        location.add("Gazipur");
        location.add("Mirpur");
        location.add("Savar");

        spLocation = findViewById(R.id.spLocation);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, location);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocation.setAdapter(adapter);

        spLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLocation = spLocation.getSelectedItem().toString();

                filterLocation(mLocation);
                // menuAdapter.filter("tvLocation",selected[0]);
                //dummy.addAll(obj);

                Log.d("check", "onItemSelected: tvLocation " + mLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final List<String> type = new ArrayList<String>();
        type.add("All");
        type.add("Lunch");
        type.add("Snacks");

        spType = findViewById(R.id.spType);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, type);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter2);

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mType = spType.getSelectedItem().toString();

                filterType(mType);

                //menuAdapter.filter("type",selected[0]);

                Log.d("check", "onItemSelected: type " + mLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        */
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
                    StaticConfig.ADDRESS = address;
                    avatar = (String) hashUser.get("avatar");

                    Log.d("check", "name in customer: " + name);

                    if (name.equals("name") || address.equals("address")) {
                        Intent y = new Intent(HomeCustomer.this, ProfileCustomer.class);

                        startActivity(y);
                    } else {
                        nName.setText(name);
                        if (!avatar.equals("default"))
                            Picasso.get().load(avatar).placeholder(R.drawable.default_avatar).into(nImage);
                    }
                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void selectLocation(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_select_location, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflate);

        final String[] selected = new String[1];
        List<String> locations = new ArrayList<String>();
        locations.add("All");
        locations.add("Mirpur");
        locations.add("Savar");
        locations.add("Gazipur");
        locations.add("Gulshan");

        LinearLayout locate = inflate.findViewById(R.id.llLocateMe);
        final TextView tvLocate = inflate.findViewById(R.id.tvLocateMe);
        final Spinner location = inflate.findViewById(R.id.spMyLocation);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location.setAdapter(adapter);

        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLocation = location.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
                getOrdinate(tvLocate);

                //  if(!mLocationTemp.equals(""))
                //  tvLocate.setText(mLocationTemp);
            }
        });

        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DBHelper dbHelper=new DBHelper(contex);
                        tvLocation.setText(mLocation);
                        filterLocation(mLocation);
                        cvSearch.setVisibility(View.GONE);
                        etSearch.setText("");
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

    public void setFilters(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_filter_price, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflate);

        final EditText min = inflate.findViewById(R.id.etMinValue);
        final EditText max = inflate.findViewById(R.id.etMaxValue);


        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DBHelper dbHelper=new DBHelper(contex);

                        minPrice = Integer.parseInt(min.getText().toString().trim());
                        maxPrice = Integer.parseInt(max.getText().toString().trim());

                        Log.d("check", "price: " + minPrice + " " + maxPrice);

                        if (minPrice == 0 && maxPrice == 0)
                            filterPrice(minPrice, maxPrice);
                        else
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

            mType = "lunch";
            filterType(mType);
            cvSearch.setVisibility(View.GONE);
            etSearch.setText("");

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.white));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.llSnacks) {

            mType = "snacks";
            filterType(mType);
            cvSearch.setVisibility(View.GONE);
            etSearch.setText("");

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.white));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.llDinner) {

            mType = "dinner";
            filterType(mType);
            cvSearch.setVisibility(View.GONE);
            etSearch.setText("");

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.white));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.llBF) {

            mType = "breakfast";
            filterType(mType);
            cvSearch.setVisibility(View.GONE);
            etSearch.setText("");

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.white));
        }
    }

    public void filterPrice(int min, int max) {
        ArrayList<xyz.foodhut.app.model.MenuCustomer> dummy2 = new ArrayList<>();
        if (dummy.size() > 0) {
            // for (MenuCustomer wp : obj) {
            dummy2.clear();
            for (int i = 0; i < dummy.size(); i++) {
                xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                mc = dummy.get(i);
                // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                if (Integer.parseInt(mc.price) >= min && Integer.parseInt(mc.price) <= max) {

                    Log.d("check", "in filter: iffff");
                    dummy2.add(mc);
                }

            }
            dummy.clear();
            dummy = dummy2;
            menuAdapter = new MenuCustomer(this, dummy);
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        } else {
            dummy2.clear();
            for (int i = 0; i < arrayList.size(); i++) {
                xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                mc = arrayList.get(i);
                // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                if (Integer.parseInt(mc.price) >= min && Integer.parseInt(mc.price) <= max) {

                    Log.d("check", "in filter: iffff");
                    dummy2.add(mc);
                }

            }
            dummy.clear();
            dummy = dummy2;
            menuAdapter = new MenuCustomer(this, dummy);
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        }
    }

    public void filterLocation(String value) {
        Log.d("check", "in filter: tvLocation" + " " + value + " " + mLocation + " " + mType + dummy.size());

        value = value.toLowerCase(Locale.getDefault());
        //mType=mType.toLowerCase(Locale.getDefault());
        //mLocation=mLocation.toLowerCase(Locale.getDefault());
        if (value.length() == 0) {
            menuAdapter = new MenuCustomer(this, arrayList);
        } else {
            dummy.clear();

            if (value.equals("all") && (mType.equals("All") || mType.equals(""))) {
                menuAdapter = new MenuCustomer(this, arrayList);
            } else {

                if (mType.equals("All") || mType.equals("")) {

                    // for (MenuCustomer wp : obj) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = arrayList.get(i);
                        Log.d("check", "in filter:tvLocation Type a/e " + arrayList.size() + " " + mc.providerAddress);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)) {

                            Log.d("check", "in filter: iffff");
                            dummy.add(mc);
                        }
                    }
                } else if (value.equals("all") && !mType.equals("All")) {
                    // for (MenuCustomer wp : obj) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = arrayList.get(i);
                        Log.d("check", "in filter:tvLocation Type n/e " + arrayList.size() + " " + mc.type);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if (mc.type.toLowerCase(Locale.getDefault()).contains(mType.toLowerCase())) {

                            Log.d("check", "in filter: if");
                            dummy.add(mc);
                        }
                    }
                } else {
                    // for (MenuCustomer wp : obj) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = arrayList.get(i);
                        Log.d("check", "in filter:tvLocation type n/e " + arrayList.size() + " " + mc.providerAddress);
                        // if ((mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value) || mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) && mc.type.toLowerCase(Locale.getDefault()).contains(mType.toLowerCase(Locale.getDefault()))) {
                        if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value) && mc.type.toLowerCase(Locale.getDefault()).contains(mType.toLowerCase(Locale.getDefault()))) {

                            Log.d("check", "in filter: iffff");
                            dummy.add(mc);
                        }
                    }
                }
                menuAdapter = new MenuCustomer(this, dummy);
            }

            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        }
    }

    public void filterType(String value) {
        Log.d("check", "in filter: type" + " " + value + " " + mLocation + " " + mType + dummy.size());

        value = value.toLowerCase(Locale.getDefault());
        //mType=mType.toLowerCase(Locale.getDefault());
        //mLocation=mLocation.toLowerCase(Locale.getDefault());

        if (value.length() == 0) {
            menuAdapter = new MenuCustomer(this, arrayList);
        } else {
            dummy.clear();

            if (value.equals("all") && (mLocation.equals("All") || mLocation.equals(""))) {
                menuAdapter = new MenuCustomer(this, arrayList);
            } else {
                if (mLocation.equals("All") || mLocation.equals("")) {

                    // for (MenuCustomer wp : obj) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = arrayList.get(i);
                        Log.d("check", "in filter: " + arrayList.size() + " " + mc.type);
                        if (mc.type.toLowerCase(Locale.getDefault()).contains(value)) {
                            Log.d("check", "in filter: iffff");
                            dummy.add(mc);
                        }
                    }
                } else if (value.equals("all") && !mLocation.equals("All")) {
                    // for (MenuCustomer wp : obj) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = arrayList.get(i);
                        Log.d("check", "in filter:type tvLocation n/e " + arrayList.size() + " " + mc.providerAddress);
                        // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                        if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(mLocation.toLowerCase())) {

                            Log.d("check", "in filter: if");
                            dummy.add(mc);
                        }
                    }
                } else {
                    // for (MenuCustomer wp : obj) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                        mc = arrayList.get(i);
                        Log.d("check", "in filter: " + arrayList.size() + " " + mc.providerAddress);
                        //if (mc.type.toLowerCase(Locale.getDefault()).contains(value) && (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(mLocation.toLowerCase(Locale.getDefault())) || mc.tvLocation.toLowerCase(Locale.getDefault()).contains(mLocation.toLowerCase(Locale.getDefault())))) {
                        if (mc.type.toLowerCase(Locale.getDefault()).contains(value) && mc.providerAddress.toLowerCase(Locale.getDefault()).contains(mLocation.toLowerCase(Locale.getDefault()))) {

                            Log.d("check", "in filter: iffff");
                            dummy.add(mc);
                        }
                    }
                }
                menuAdapter = new MenuCustomer(this, dummy);
            }
        }
        recyclerView.setAdapter(menuAdapter);
        menuAdapter.notifyDataSetChanged();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    public void getOrdinate(final TextView tvLocation) {
        if (ActivityCompat.checkSelfPermission(HomeCustomer.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(HomeCustomer.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    lattitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Log.d("check", "location " + lattitude + " " + longitude);

                    // getAddress(lattitude, longitude);
                    // GetReverseGeoCoding geoCoding=new GetReverseGeoCoding();
                    // geoCoding.getAddress(lattitude,longitude);

                    // getAddress(lattitude, longitude);
                    getArea(lattitude, longitude, tvLocation);

                } else {
                    show(tvLocation);
                }

            }
        });
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(HomeCustomer.this, Locale.getDefault());
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
                tvLocation.setText(obj.getSubLocality());
            } else if (obj.getLocality() != null) {
                tvLocation.setText(obj.getLocality());
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

    public void getArea(double lati, double longi, final TextView tvLocate) {

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
                                String parts[] = formatted_address.split(",");

                                if (parts[2] != null) {
                                    mLocation = parts[2];
                                    mLocationTemp = parts[2];
                                    tvLocate.setText(parts[2]);
                                }


                                Log.d("check", "onResponse address: " + formatted_address);
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


    public void show(TextView tvLocate) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            provider = 1;
            getLocation(tvLocate);
            Log.d("check method", "network condition");
        } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {

            provider = 2;
            getLocation(tvLocate);
            Log.d("check method", "passive condition");

        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            Log.d("check method", "gps alert");

        } else if (gps_check && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            provider = 3;
            getLocation(tvLocate);
            Log.d("check method", "gps condition");
        }
        Log.d("check provider", String.valueOf(provider));
    }

    private void getLocation(TextView tvLocate) {
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

                // getAddress(lattitude, longitude);
                getArea(lattitude, longitude, tvLocate);


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

                //  getAddress(lattitude, longitude);
                getArea(lattitude, longitude, tvLocate);


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

                //   getAddress(lattitude, longitude);
                getArea(lattitude, longitude, tvLocate);


            } else {

                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();

            }
            Log.d("check method", "gps");

        }

    }

    protected void buildAlertMessageNoGps() {

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
        final android.app.AlertDialog alert = builder.create();
        alert.show();
        Log.d("check method", "from alert");
    }

    public void footerAction(View view) {
        if (view.getId() == R.id.chExplore) {
            //reset home page and filters

            etSearch.setText("");
            cvSearch.setVisibility(View.GONE);

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));

            menuAdapter = new MenuCustomer(this, arrayList);
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        }
        if (view.getId() == R.id.chSearch) {

            if (search == 0) {
                search = 1;
                cvSearch.setVisibility(View.VISIBLE);
            } else if (search == 1) {
                search = 0;
                cvSearch.setVisibility(View.GONE);

                if (dummy.size() == 0)
                    menuAdapter = new MenuCustomer(this, arrayList);
                else
                    menuAdapter = new MenuCustomer(this, dummy);

                recyclerView.setAdapter(menuAdapter);
                menuAdapter.notifyDataSetChanged();
            }
            //etSearch.requestFocus();
            dummy.clear();
            ivSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String value = etSearch.getText().toString().trim();
                    if (!value.equals("")) {
                        // for (MenuCustomer wp : obj) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            xyz.foodhut.app.model.MenuCustomer mc = new xyz.foodhut.app.model.MenuCustomer();
                            mc = arrayList.get(i);
                            Log.d("check", "in search:tvName " + arrayList.size() + " " + mc.name);
                            // if (mc.providerAddress.toLowerCase(Locale.getDefault()).contains(value)||mc.tvLocation.toLowerCase(Locale.getDefault()).contains(value)) {
                            if (mc.name.toLowerCase(Locale.getDefault()).contains(value)) {

                                Log.d("check", "in filter: iffff");
                                dummy.add(mc);

                                cvSearch.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
            menuAdapter = new MenuCustomer(this, dummy);
            recyclerView.setAdapter(menuAdapter);
            menuAdapter.notifyDataSetChanged();
        }
        if (view.getId() == R.id.chNoti) {

            Intent intent = new Intent(this, NotificationCustomer.class);
            intent.putExtra("user", "customer");
            startActivity(intent);
        }
        if (view.getId() == R.id.chProfile) {
            Intent intent = new Intent(this, ProfileCustomer.class);
            intent.putExtra("name", name);
            intent.putExtra("address", address);
            intent.putExtra("avatar", avatar);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.cmProfile) {
            // Handle the camera action
            Intent intent = new Intent(this, ProfileCustomer.class);
            intent.putExtra("name", name);
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

        } else if (id == R.id.cmLogout) {
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
}
