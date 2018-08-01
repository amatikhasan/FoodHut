package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
import xyz.foodhut.app.classes.MenuAdapterProvider;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;
import xyz.foodhut.app.model.MenuProvider;
import xyz.foodhut.app.model.User;
import xyz.foodhut.app.ui.PhoneAuthActivity;
import xyz.foodhut.app.ui.ProfileUpdate;
import xyz.foodhut.app.ui.provider.AddMenu;


public class CustomerHome extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<MenuCustomer> arrayList = new ArrayList<>();
    String userID = null;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    MenuAdapterCustomer menuAdapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

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

     /*   if(user!=null) {
            userID = user.getUid();
        }
        else {
            //
            Toast.makeText(this, "Error authenticating", Toast.LENGTH_SHORT).show();
        }  */

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
                    String name = (String) hashUser.get("name");

                    Log.d("check", "name in customer: "+name);

                    if (name.equals("name")) {
                        Intent y = new Intent(CustomerHome.this, ProfileUpdate.class);
                        y.putExtra("type","customer");

                        startActivity(y);

                    }

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

}
