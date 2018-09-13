package xyz.foodhut.app.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.CustomerHome;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.provider.HomeProvider;
import xyz.foodhut.app.ui.provider.ProviderHome;


public class Splash extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    String userType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        int secondsDelayed = 2;
        new Handler().postDelayed(new Runnable() {
            public void run() {

                if (!SharedPreferenceHelper.getInstance(getApplicationContext()).isLoggedIn()) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
                else {
                    initFirebase();
                    isLoggedIn();
                    //startActivity(new Intent(Splash.this, MainActivity.class));
                    //finish();
                }
            }
        }, secondsDelayed * 1000);


    }

    private void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky
        //mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user != null) {
            StaticConfig.UID = user.getUid();

            Log.d("check", "ID " + StaticConfig.UID);

           // getUser();

        } else {
            this.finish();
            // User is signed in
            startActivity(new Intent(Splash.this, MainActivity.class));
            Log.d("check", "onAuthStateChanged:signed_out");
        }
        // ...
    }


    public void getUser() {
        FirebaseDatabase.getInstance().getReference().child("users/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    HashMap hashUser = (HashMap) dataSnapshot.getValue();
                    userType = (String) hashUser.get("type");

                    Log.d("check", "type: " + userType);

                    if (userType.equals("provider")) {
                        startActivity(new Intent(Splash.this, ProviderHome.class));
                        finish();
                    }
                    if (userType.equals("customer")) {
                        startActivity(new Intent(Splash.this, CustomerHome.class));
                        finish();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void isLoggedIn() {
        if (SharedPreferenceHelper.getInstance(getApplicationContext()).isLoggedIn()) {

            String type=SharedPreferenceHelper.getInstance(getApplicationContext()).getType();
            Log.d("check", "isLoggedIn: "+type);

            if (type.equals("provider")) {
                startActivity(new Intent(this, HomeProvider.class));
                finish();
            }
            if (type.equals("customer")) {
                startActivity(new Intent(this, HomeCustomer.class));
                finish();
            }
        }
    }
}