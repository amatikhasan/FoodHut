package xyz.foodhut.app.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import xyz.foodhut.app.model.User;
import xyz.foodhut.app.ui.customer.CustomerHome;
import xyz.foodhut.app.ui.provider.ProviderHome;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        // mFirebaseDatabase = FirebaseDatabase.getInstance();
        // myRef = mFirebaseDatabase.getReference();

        //initFirebase();
        // isLoggedIn();
    }

    private void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky
        //mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user != null) {
            StaticConfig.UID = user.getUid();

            Log.d("check", "onAuthStateChanged: ID " + StaticConfig.UID);

            //getUser();

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
                        startActivity(new Intent(MainActivity.this, ProviderHome.class));
                        finish();
                    }
                    if (userType.equals("customer")) {
                        startActivity(new Intent(MainActivity.this, CustomerHome.class));
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

            if (SharedPreferenceHelper.getInstance(getApplicationContext()).getType().equals("provider")) {
                startActivity(new Intent(this, ProviderHome.class));
                finish();
            }
            if (SharedPreferenceHelper.getInstance(getApplicationContext()).getType().equals("customer")) {
                startActivity(new Intent(this, CustomerHome.class));
                finish();
            }
        }
    }

    public void toCustomer(View view) {
        Intent intent = new Intent(this, PhoneAuthActivity.class);
        intent.putExtra("type", "customer");
        startActivity(intent);
        finish();
    }

    public void toProvider(View view) {
        Intent intent = new Intent(this, PhoneAuthActivity.class);
        intent.putExtra("type", "provider");
        startActivity(intent);
        finish();
    }
}
