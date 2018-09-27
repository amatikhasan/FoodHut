package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.OrdersProvider;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;

public class OrdersCustomer extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView emptyOrder;
    ArrayList<OrderDetailsProvider> arrayList = new ArrayList<>();
    ArrayList<OrderDetails> orderDetails;
    String userID = null;
    String mdate;
    Bundle extras;
    ArrayList<String> orderList;
    ArrayList<String> dateList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    xyz.foodhut.app.adapter.OrdersCustomer adapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_customer);

      //  getSupportActionBar().setTitle("OrdersProvider");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        orderList = new ArrayList<>();
        dateList = new ArrayList<>();
        arrayList = new ArrayList<>();
        orderDetails = new ArrayList<>();

        emptyOrder=findViewById(R.id.emptyOrder);
        recyclerView = findViewById(R.id.rvOrdersCustomer);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new xyz.foodhut.app.adapter.OrdersCustomer(this, arrayList, orderDetails);
        recyclerView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }
     /*   extras = getIntent().getExtras();
        if (extras != null) {
            mdate = extras.getString("date");
            StaticConfig.DATE=mdate;

            getSupportActionBar().setTitle(mdate);
        }  */

        getDate();
    }

    public void getDate() {
        if (StaticConfig.UID != null) {
            FirebaseDatabase.getInstance().getReference("customers/" + userID + "/orders").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    if (dataSnapshot.getValue() != null) {
                        //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            dateList.add(String.valueOf(dsp.getKey())); //add result into array list

                            Log.d("check", "onDataChange: date " + dsp.getKey());
                        }
                        Log.d("check", "dateList size: " + dateList.size());
                        getOrderList();
                        //  Collections.reverse(obj);
                    }
                    else {
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
    }

    public void getOrderList() {
        for (int i = 0; i < dateList.size(); i++) {
            String date = dateList.get(i);


            final int finalI = i;
            databaseReference.child("customers/" + userID).child("orders").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        orderList.add(String.valueOf(dsp.getKey())); //add result into array list

                        Log.d("check", "onDataChange: orderId " + dsp.getKey());
                    }

                    getMenu(finalI);
                    Log.d("check", "orderList size: " + orderList.size());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //  dialog.dismiss();
                }
            });
        }


    }

    public void getMenu(int i) {
       // for (int i = 0; i < dateList.size(); i++) {
            String date = dateList.get(i);
            StaticConfig.DATE = date;

            for (int j = 0; j < orderList.size(); j++) {
                String orderId = orderList.get(j);
                StaticConfig.ORDERID.add(orderId);

                databaseReference.child("customers/" + userID).child("orders").child(date).child(orderId).child("orderDetails")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                OrderDetails details = dataSnapshot.getValue(OrderDetails.class);
                                orderDetails.add(details);
                                //  Collections.reverse(obj);

                                Log.d("Check", "orderDetails: " + orderDetails.size());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                databaseReference.child("customers/" + userID).child("orders").child(date).child(orderId).child("menuDetails")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dialog.dismiss();
                                //fetch files from firebase database and push in arraylist
                                // for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                //  Log.d("Check list", "onDataChange: " + snapshot.getValue());
                                OrderDetailsProvider orderDetailsProvider = dataSnapshot.getValue(OrderDetailsProvider.class);
                                arrayList.add(orderDetailsProvider);
                                //menuAdapter.notifyDataSetChanged();
                                //   }

                                //    for(int i=0;i<obj.size();i++){
                                //      if(obj.get(i).mImageUrl==null||obj.get(i).mImageUrl==null){
                                //          obj.remove(i);
                                //      }
                                //   }

                                //  Collections.reverse(obj);

                                //bind the data in adapter
                                adapter.notifyDataSetChanged();
                                Log.d("Check", "menuDetails: " + arrayList.size());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                dialog.dismiss();
                            }
                        });
            }

            orderList.clear();
        }
   // }

    public void goBack(View view){
        startActivity(new Intent(this,HomeCustomer.class));
        finish();
        finishAffinity();
    }

}
