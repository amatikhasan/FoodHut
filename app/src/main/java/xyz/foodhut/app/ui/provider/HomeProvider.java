package xyz.foodhut.app.ui.provider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.MenuCustomer;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.FeedBack;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.Profile;
import xyz.foodhut.app.ui.customer.NotificationCustomer;
import xyz.foodhut.app.ui.customer.ProfileCustomer;

public class HomeProvider extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth firebaseAuth;
    DrawerLayout drawer;
    TextView tvName,tvKitchen,tvAddress,tvStatus;
    ImageView image;
    String name, address,avatar,kitchenName,status;
    long mCurrent=0, mPendingWithdraw=0, mPaid=0;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_provider);
        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //  setSupportActionBar(toolbar);

        dialog=new ProgressDialog(this);
        dialog.setMessage("please wait");
        dialog.show();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // drawer.addDrawerListener(toggle);
        //  toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvName=findViewById(R.id.tvName);
        tvKitchen=findViewById(R.id.tvKitchen);
        tvAddress=findViewById(R.id.tvAddress);
        tvStatus=findViewById(R.id.tvStatus);
        image=findViewById(R.id.ivProfilePic);

        firebaseAuth = FirebaseAuth.getInstance();

        checkProfile();

       // checkBalance();
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
                    kitchenName = (String) hashUser.get("kitchenName");
                    status = (String) hashUser.get("status");

                    StaticConfig.NAME = name;
                    StaticConfig.ADDRESS = address;

                    Log.d("check", "name in provider: " + name+" "+kitchenName);

                    if (status.equals("Active")) {

                        if (name.equals("name") || address.equals("address") || kitchenName.equals("kitchen")) {
                            Log.d("check", "name in provider2: " + name + " " + address);

                            Intent y = new Intent(HomeProvider.this, ProfileProvider.class);
                            startActivity(y);
                        }

                        else {
                            String manager = "Manager: " + name;
                            tvName.setText(manager);
                            tvKitchen.setText(kitchenName);
                            tvAddress.setText(address);
                            String status1 = "Status: " + status;
                            tvStatus.setText(status1);

                            if (!avatar.equals("default"))
                                Picasso.get().load(avatar).placeholder(R.drawable.kitchen_icon_colour).into(image);

                        }

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

                dialog.dismiss();
            }

            public void onCancelled(DatabaseError arg0) {
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

    public void ordersToday(View view) {
        startActivity(new Intent(this, OrdersToday.class));
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
        Intent intent = new Intent(this, FeedBack.class);
        intent.putExtra("user", "provider");
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
            Intent intent = new Intent(this, ProfileProvider.class);
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
