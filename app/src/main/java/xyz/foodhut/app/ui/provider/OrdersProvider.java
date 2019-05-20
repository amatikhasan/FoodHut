package xyz.foodhut.app.ui.provider;

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
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;

public class OrdersProvider extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView  emptyOrder;
    ArrayList<OrderDetailsProvider> arrayList = new ArrayList<>();
    ArrayList<OrderDetails> orderDetails;
    String userID = null;
    String mdate;
    Bundle extras;
    ArrayList<String> orderList;
    ArrayList<String> dateList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    xyz.foodhut.app.adapter.OrdersProvider adapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_provider);

      //  getSupportActionBar().setTitle("Orders");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        orderList = new ArrayList<>();
        dateList = new ArrayList<>();
        arrayList = new ArrayList<>();
        orderDetails = new ArrayList<>();

        emptyOrder = findViewById(R.id.emptyOrder);
        recyclerView = findViewById(R.id.rvOrdersProvider);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new xyz.foodhut.app.adapter.OrdersProvider(this,orderDetails);
        recyclerView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        userID = user.getUid();

        extras = getIntent().getExtras();
        if (extras != null) {
            mdate = extras.getString("date");
            StaticConfig.DATE=mdate;

        }

        getDate();
    }

    public void goBack(View view){
       // startActivity(new Intent(this,HomeProvider.class));
        finish();
       // finishAffinity();
    }

    public void getDate() {
        if (StaticConfig.UID != null) {
            FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/orders").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() != null) {
                        //fetch files from firebase database and push in arraylist

                        //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            dateList.add(String.valueOf(dsp.getKey())); //add result into array list

                            getMenuList(dsp.getKey());
                            Log.d("check", "onDataChange: date " + dsp.getKey());
                        }
                        Log.d("check", "dateList size: " + dateList.size());
                       // getOrderList();

                        //  Collections.reverse(obj);
                    }
                    else {
                        dialog.dismiss();
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
            databaseReference.child("providers/" + StaticConfig.UID).child("orders").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
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

            databaseReference.child("providers/" + StaticConfig.UID).child("orders").child(date).child(orderId).child("orderDetails")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                           // dialog.dismiss();
                            OrderDetails details = dataSnapshot.getValue(OrderDetails.class);
                            orderDetails.add(details);
                            //  Collections.reverse(obj);

                            Log.d("Check", "orderDetails: " + orderDetails.size());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            databaseReference.child("providers/" + userID).child("orders").child(date).child(orderId).child("menuDetails")
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

    public void getMenuList(final String date) {

        databaseReference.child("providers/" + StaticConfig.UID + "/orders").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // dialog.dismiss();
                //fetch files from firebase database and push in arraylist
                if (dataSnapshot.getValue() != null) {

                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        //   getMenu(String.valueOf(dsp.getKey()),date);
                        getOrderList(String.valueOf(dsp.getKey()),date);
                        Log.d("check", "onDataChange: menuId " + dsp.getKey());

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //  dialog.dismiss();
            }
        });
    }

    public void getMenu(final String menuId, final String date) {
        arrayList.clear();

        databaseReference.child("providers/" + StaticConfig.UID + "/orders").child(date).child(menuId).child("menuDetails")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //  dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {

                            OrderDetailsProvider orderDetailsProvider = dataSnapshot.getValue(OrderDetailsProvider.class);

                            getOrderList(menuId,date);

                            Log.d("Check", "menuDetails: " + arrayList.size());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });
    }


    public void getOrderList(final String menuId, final String date) {
        //menuList.clear();
        databaseReference.child("providers/" + StaticConfig.UID + "/orders").child(date).child(menuId).child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    getOrderDetails(menuId,date, String.valueOf(dsp.getKey()));

                    Log.d("check", "onDataChange: orderId from list " + dsp.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //  dialog.dismiss();
            }
        });
    }

    public void getOrderDetails(final String menuId, String date, String orderId) {


        databaseReference.child("providers/" + StaticConfig.UID + "/orders").child(date).child(menuId).child("orders").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        OrderDetails details = dataSnapshot.getValue(OrderDetails.class);

                        orderDetails.add(details);

                        for (int i = 0; i < orderDetails.size()-1; i++) {
                            for (int j = 0; j < orderDetails.size() - i - 1; j++) {

                                if (Long.parseLong(orderDetails.get(j).orderId)<Long.parseLong(orderDetails.get(j+1).orderId)) {

                                    OrderDetails od=new OrderDetails();
                                    od=orderDetails.get(j);

                                    orderDetails.set(j, orderDetails.get(j + 1));
                                    orderDetails.set(j + 1, od);

                                    Log.d("Check", "dates comparing: "+i+" " + j);
                                }

                            }
                        }

                        adapter.notifyDataSetChanged();
                        Log.d("Check list", "onDataChange: details " + orderDetails.size() + " " + orderDetails.get(0).orderId);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });

        //  }


    }
}
