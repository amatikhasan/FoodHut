package xyz.foodhut.app.ui.provider;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.MainActivity;
import xyz.foodhut.app.ui.ProfileUpdate;
import xyz.foodhut.app.ui.customer.HomeCustomer;

public class HomeProvider extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth firebaseAuth;
    String name,address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_provider);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        checkProfile();
    }

    public void checkProfile() {
        FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                     name = (String) hashUser.get("name");
                     address = (String) hashUser.get("address");

                    Log.d("check", "name in provider: "+name);

                    if (name.equals("name")) {
                        Log.d("check", "name in provider2: "+name+" "+address);

                        Intent y = new Intent(HomeProvider.this, ProfileUpdate.class);
                        y.putExtra("type","provider");

                        startActivity(y);

                    }

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void myMenu(View view){
        startActivity(new Intent(this,Menus.class));
    }

    public void mySchedule(View view){
        startActivity(new Intent(this,Schedule.class));
    }
    public void orders(View view){
        startActivity(new Intent(this,OrdersDate.class));
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
            Intent intent=new Intent(this,ProfileUpdate.class);
            intent.putExtra("type","provider");
            intent.putExtra("name",name);
            intent.putExtra("address",address);
            startActivity(intent);
        } else if (id == R.id.pmOrders) {
            Intent intent=new Intent(this,OrdersDate.class);
            startActivity(intent);

        } else if (id == R.id.pmSale) {

        } else if (id == R.id.pmReviews) {

        }
        else if (id == R.id.pmLogout) {
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
