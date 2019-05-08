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
    xyz.foodhut.app.adapter.OrdersProviderRunning adapter;
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
        adapter = new xyz.foodhut.app.adapter.OrdersProviderRunning(this,orderDetails);
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
      //  startActivity(new Intent(this,HomeProvider.class));
        finish();
     //   finishAffinity();
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

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        final String today = sdf.format(calendar.getTime());

        databaseReference.child("providers/" + StaticConfig.UID + "/orders").child(date).child(menuId).child("orders").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        OrderDetails details = dataSnapshot.getValue(OrderDetails.class);

                        if (details != null &&details.date.equals(today) && !details.status.equals("Cancelled") && !details.status.equals("Rejected")&&!details.status.equals("Pending")&&!details.status.equals("Delivered"))
                            orderDetails.add(details);
                        //  Collections.reverse(obj);

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
                        Log.d("Check list", "onDataChange: details " + orderDetails.size() );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });

        //  }


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
