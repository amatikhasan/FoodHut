package xyz.foodhut.app.ui.provider;

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

import xyz.foodhut.app.R;
import xyz.foodhut.app.classes.MenuAdapterProvider;
import xyz.foodhut.app.classes.OrdersAdapterProvider;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuProvider;
import xyz.foodhut.app.model.OrderDetailsProvider;

public class Orders extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<OrderDetailsProvider> arrayList = new ArrayList<>();
    String userID = null;
    String mdate;
    Bundle extras;
    ArrayList<String> menuList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    OrdersAdapterProvider adapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        getSupportActionBar().setTitle("Orders");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        menuList = new ArrayList<>();
        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvOrdersProvider);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapterProvider(this, arrayList);
        recyclerView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        userID = user.getUid();

        extras = getIntent().getExtras();
        if (extras != null) {
            mdate = extras.getString("date");

            getSupportActionBar().setTitle(mdate);
        }

        if (StaticConfig.UID != null) {

            databaseReference.child("providers/" + userID).child("orders").child(mdate).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        menuList.add(String.valueOf(dsp.getKey())); //add result into array list

                        Log.d("check", "onDataChange: date " + dsp.getKey());
                    }

                    getMenu();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //  dialog.dismiss();
                }
            });

        }




    }

    public void getMenu() {
        for (int i = 0; i < menuList.size(); i++) {
            String menuId=menuList.get(i);
            databaseReference.child("providers/" + userID).child("orders").child(mdate).child(menuId).child("menuDetails")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            //fetch files from firebase database and push in arraylist
                           // for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                              //  Log.d("Check list", "onDataChange: " + snapshot.getValue());
                                OrderDetailsProvider orderDetailsProvider = dataSnapshot.getValue(OrderDetailsProvider.class);
                                arrayList.add(orderDetailsProvider);
                                //menuAdapter.notifyDataSetChanged();
                                Log.d("Check list", "onDataChange: " + arrayList.size());
                         //   }

                            //    for(int i=0;i<arrayList.size();i++){
                            //      if(arrayList.get(i).imageUrl==null||arrayList.get(i).imageUrl==null){
                            //          arrayList.remove(i);
                            //      }
                            //   }

                            //  Collections.reverse(arrayList);

                            //bind the data in adapter
                            Log.d("Check list", "out datachange: " + arrayList.size());
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    public void fabBtn(View view) {

        startActivity(new Intent(this, AddMenu.class
        ));
    }
}
