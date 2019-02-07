package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
import xyz.foodhut.app.adapter.MenuCustomer;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.Profile;


public class CustomerHome extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<xyz.foodhut.app.model.MenuCustomer> arrayList = new ArrayList<>();
    String userID = null;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    MenuCustomer menuAdapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

     //   getSupportActionBar().setTitle("Menus");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvCustomerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
      //  menuAdapter = new MenuCustomer(this, arrayList);
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
                                xyz.foodhut.app.model.MenuCustomer customer = snapshot.getValue(xyz.foodhut.app.model.MenuCustomer.class);
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
                        Intent y = new Intent(CustomerHome.this, ProfileCustomer.class);

                        startActivity(y);

                    }

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

}
