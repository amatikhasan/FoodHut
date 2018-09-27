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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;

public class OrdersToday extends AppCompatActivity {

    ImageView emptyOrder;
    RecyclerView recyclerView;
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
        setContentView(R.layout.activity_orders_today);

     //   getSupportActionBar().setTitle("Today");

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
        adapter = new xyz.foodhut.app.adapter.OrdersProvider(this, arrayList,orderDetails);
        recyclerView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        userID = user.getUid();

        extras = getIntent().getExtras();
        if (extras != null) {
            mdate = extras.getString("date");
            StaticConfig.DATE=mdate;

        }

        getOrderList(checkDate());
    }

    public void goBack(View view){
        startActivity(new Intent(this,HomeProvider.class));
        finish();
        finishAffinity();
    }

    public void getOrderList(final String date) {
            databaseReference.child("providers/" + StaticConfig.UID).child("orders").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    if (dataSnapshot.getValue() != null) {
                        //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            orderList.add(String.valueOf(dsp.getKey())); //add result into array list

                            Log.d("check", "onDataChange: orderId " + dsp.getKey());
                        }

                        getMenu(date);
                        Log.d("check", "orderList size: " + orderList.size());

                        if (dataSnapshot.getValue() == null) {
                            dialog.dismiss();
                        }
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

    public void getMenu(String date) {

        for (int j = 0; j < orderList.size(); j++) {
            String orderId = orderList.get(j);
            StaticConfig.ORDERID.add(orderId);

            databaseReference.child("providers/" + StaticConfig.UID).child("orders").child(date).child(orderId).child("orderDetails")
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
                            dialog.dismiss();
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

    public String checkDate() {
        // Get the calander
        int day, month, year;
        String date = null;
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        c.set(year, month, day);
        date = day + "-" + (month + 1) + "-" + year;

        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");
        Date now = c.getTime();
        formattedDate = sdtf.format(now);

        Log.d("check", "checkDate: " + date + " " + formattedDate);

        return formattedDate;
    }
}
