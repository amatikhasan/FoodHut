package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.classes.MenuAdapterCustomer;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.ProfileUpdate;

public class HomeCustomer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    ArrayList<MenuCustomer> arrayList = new ArrayList<>();
    String userID = null;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    MenuAdapterCustomer menuAdapter;
    private ProgressDialog dialog;
    String name,address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setTitle("Menus");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvCustomerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapterCustomer(this, arrayList);
        recyclerView.setAdapter(menuAdapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        checkProfile();


        if (StaticConfig.UID != null) {
            FirebaseDatabase.getInstance().getReference("schedule")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            //fetch files from firebase database and push in arraylist
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                MenuCustomer customer = snapshot.getValue(MenuCustomer.class);
                                arrayList.add(customer);
                                //menuAdapter.notifyDataSetChanged();
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
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });

        }
    }

    public void checkProfile() {
        FirebaseDatabase.getInstance().getReference().child("customers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                     name = (String) hashUser.get("name");
                     address = (String) hashUser.get("address");

                    Log.d("check", "name in customer: "+name);

                    if (name.equals("name")) {
                        Intent y = new Intent(HomeCustomer.this, ProfileUpdate.class);
                        y.putExtra("type","customer");
                       // y.putExtra("name",name);
                       // y.putExtra("address",address);

                        startActivity(y);

                    }

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
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
            Intent intent=new Intent(this,ProfileUpdate.class);
            intent.putExtra("type","customer");
            intent.putExtra("name",name);
            intent.putExtra("address",address);
            startActivity(intent);

        } else if (id == R.id.cmOrders) {
            Intent intent=new Intent(this,OrdersDateCustomer.class);
            intent.putExtra("type","customer");
            startActivity(intent);

        } else if (id == R.id.cmWishList) {

        }
        else if (id == R.id.cmLogout) {
            Intent intent=new Intent(this,MainActivity.class);
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
