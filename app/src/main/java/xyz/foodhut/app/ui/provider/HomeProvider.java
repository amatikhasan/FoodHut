package xyz.foodhut.app.ui.provider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.MenuCustomer;
import xyz.foodhut.app.classes.NotificationService;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.FeedBack;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.Profile;
import xyz.foodhut.app.ui.ProfileUpdate;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.customer.NotificationCustomer;
import xyz.foodhut.app.ui.customer.ProfileCustomer;

public class HomeProvider extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth firebaseAuth;
    DrawerLayout drawer;
  private  TextView tvName,tvKitchen,tvAddress,tvStatus,noRating,notificationCount;
  private  RatingBar ratingBar;
  private    ImageView image;
 private    String name, address,avatar,kitchenName,status;
 private   float rating;
 private   long mCurrent=0, mPendingWithdraw=0, mPaid=0;
  private   ProgressDialog dialog;

    int appVersion, currentVersion;
    String forceUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_provider);
        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //  setSupportActionBar(toolbar);

        dialog=new ProgressDialog(this);
        dialog.setMessage("please wait..");
        dialog.show();

     //   drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // drawer.addDrawerListener(toggle);
        //  toggle.syncState();

      //  NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
     //   navigationView.setNavigationItemSelectedListener(this);

        tvName=findViewById(R.id.tvName);
        tvKitchen=findViewById(R.id.tvKitchen);
        tvAddress=findViewById(R.id.tvAddress);
        tvStatus=findViewById(R.id.tvStatus);
        noRating=findViewById(R.id.noRating);
        image=findViewById(R.id.ivProfilePic);
        ratingBar=findViewById(R.id.mRatingBar);

        notificationCount = findViewById(R.id.chNotiCount);

        firebaseAuth = FirebaseAuth.getInstance();

        if (!isConnected()){
            dialog.cancel();
            Toast.makeText(this, "Sorry, you don't have an active internet connection", Toast.LENGTH_SHORT).show();
        }

        notificationCount();

        checkUpdate();

        checkProfile();

        startService(new Intent(this, NotificationService.class));

       // checkBalance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkProfile();

        notificationCount();
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
                                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeProvider.this);
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
                                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeProvider.this);
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

    public void checkProfile() {
        FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    name = (String) hashUser.get("name");
                    address = (String) hashUser.get("address");
                    avatar = (String) hashUser.get("avatar");
                    rating = Float.parseFloat((String) hashUser.get("rating"));
                    kitchenName = (String) hashUser.get("kitchenName");
                    status = (String) hashUser.get("status");
                    StaticConfig.LATITUDE = (String) hashUser.get("latitude");
                    StaticConfig.LONGITUDE = (String) hashUser.get("longitude");
                    StaticConfig.KITCHENNAME = kitchenName;
                    StaticConfig.PHONE = (String) hashUser.get("phone");

                    StaticConfig.NAME = name;
                    StaticConfig.ADDRESS = address;

                    Log.d("check", "name in provider: " + name + " " + kitchenName);

                    if (status.equals("Active")) {

                        if (name.equals("name") || address.equals("address") || kitchenName.equals("kitchen")) {
                            Log.d("check", "name in provider2: " + name + " " + address);

                            Intent y = new Intent(HomeProvider.this, ProfileUpdate.class);
                            y.putExtra("type", "provider");
                            y.putExtra("avatar", "default");
                            y.putExtra("isUpdate", "false");
                            startActivity(y);
                            finish();
                        } else {
                            String manager = "Manager: " + name;
                            tvName.setText(manager);
                            tvKitchen.setText(kitchenName);
                            tvAddress.setText(address);
                            String status1 = "Status: " + status;
                            tvStatus.setText(status1);

                            if (rating > 0)
                                ratingBar.setRating(rating);
                            else {
                                ratingBar.setVisibility(View.GONE);
                                noRating.setVisibility(View.VISIBLE);
                            }


                            if (!avatar.equals("default")) {
                                Picasso.get().load(avatar).placeholder(R.drawable.kitchen_icon_colour).into(image);

                                // Glide.with(getApplicationContext()).load(avatar).placeholder(R.drawable.kitchen_icon_colour).into(image);
                            }

                        }

                    } else {

                        if (name.equals("name") || address.equals("address") || kitchenName.equals("kitchen")) {
                            Log.d("check", "name in provider2: " + name + " " + address);

                            Intent y = new Intent(HomeProvider.this, ProfileUpdate.class);
                            y.putExtra("type", "provider");
                            y.putExtra("avatar", "default");
                            y.putExtra("isUpdate", "false");
                            startActivity(y);
                            finish();
                        }
                        else{

                            final AlertDialog.Builder builder = new AlertDialog.Builder(HomeProvider.this);
                            builder.setMessage("Foodhut approval is required to get access as a kitchen manager." +
                                    " We will shortly review your kitchen til then stay with us." +
                                    " Thank you for your patience.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            finish();
                                        }
                                    });
                            final AlertDialog alert = builder.create();
                            alert.show();
                            Log.d("check method", "from alert");
                        }
                    }
                }

                dialog.cancel();
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });


    }

    public void notificationCount() {
        final int[] count = new int[1];
        count[0] = 0;
        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/notifications/new")
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


        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/notifications")
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

    public void checkBalance() {

        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("payments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    mCurrent = (long) hashUser.get("currentBalance");
                    mPendingWithdraw = (long) hashUser.get("withdrawRequest");
                    mPaid = (long) hashUser.get("totalWithdrawn");

                    Log.d("check", "balance: " + mCurrent);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void myMenu(View view) {
        startActivity(new Intent(this, Menus.class));
    }

    public void mySchedule(View view) {
        startActivity(new Intent(this, Schedule.class));
    }

    public void orders(View view) {
        startActivity(new Intent(this, OrdersProvider.class));
    }

    public void myKitchen(View view) {
        startActivity(new Intent(this, Kitchen.class));
    }

    public void viewPayments(View view) {
        Intent intent = new Intent(this, Payments.class);
      //  intent.putExtra("current", mCurrent);
      //  intent.putExtra("pending", mPendingWithdraw);
      //  intent.putExtra("paid", mPaid);
        startActivity(intent);
    }

    public void mySales(View view) {
        startActivity(new Intent(this, Sales.class));
    }

    public void feedBack(View view) {
        Intent intent = new Intent(this, Complains.class);
        startActivity(intent);
    }

    public void myReviews(View view) {
        startActivity(new Intent(this, Reviews.class));
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

    public void footerAction(View view) {
        if (view.getId() == R.id.phHome) {
            //reset home page and filters

            Intent intent = new Intent(this, HomeProvider.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.phRunning) {

            Intent intent = new Intent(this, OrdersToday.class);
            intent.putExtra("type","running");
            startActivity(intent);
        }
        if (view.getId() == R.id.phNoti) {

            Intent intent = new Intent(this, NotificationProvider.class);
            intent.putExtra("user","provider");
            startActivity(intent);
        }
        if (view.getId() == R.id.phProfile) {
           // Intent intent = new Intent(this, ProfileProvider.class);
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("type", "provider");
            intent.putExtra("isUpdate", "true");
            intent.putExtra("name", name);
            intent.putExtra("address", address);
            intent.putExtra("avatar", avatar);
            intent.putExtra("kitchen", kitchenName);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //   if (id == R.id.action_settings) {
        //        return true;
        //   }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.pmProfile) {
            // Handle the camera action
            Intent intent = new Intent(this, ProfileProvider.class);
            intent.putExtra("type", "provider");
            intent.putExtra("name", name);
            intent.putExtra("address", address);
            startActivity(intent);
        } else if (id == R.id.pmOrders) {
            Intent intent = new Intent(this, OrdersDate.class);
            startActivity(intent);

        } else if (id == R.id.pmSale) {

        } else if (id == R.id.pmReviews) {

        } else if (id == R.id.pmLogout) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            SharedPreferenceHelper.getInstance(this).logout();
            firebaseAuth.signOut();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
