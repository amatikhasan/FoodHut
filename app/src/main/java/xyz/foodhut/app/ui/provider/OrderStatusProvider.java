package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;

public class OrderStatusProvider extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView menuName;
    ArrayList<OrderDetails> obj;
    ArrayList<String> orderList;
    ArrayList<String> status;
    String mDate, mName = "",mStatus,mMenuId;
    xyz.foodhut.app.adapter.OrderStatusProvider adapter;
    private ProgressDialog dialog;
    Bundle extras;
    int check=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status_provider);
        menuName = findViewById(R.id.ospName);

        obj = new ArrayList<>();
        orderList = new ArrayList<>();
        status=new ArrayList<>();
        recyclerView = findViewById(R.id.rvOrderList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new xyz.foodhut.app.adapter.OrderStatusProvider(this, obj,status);
        recyclerView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        extras = getIntent().getExtras();
        if (extras != null) {
            mDate = extras.getString("date");
            mName = extras.getString("name");
            mMenuId = extras.getString("menuId");

            menuName.setText(mName);
            Log.d("check", "order customer: " + mDate);
        }

        getOrderList();

    }



    public void getOrderList(){
        //orderList.clear();
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("orders").child(mDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // dialog.dismiss();
                //fetch files from firebase database and push in arraylist

                //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    orderList.add(String.valueOf(dsp.getKey())); //add result into array list

                    Log.d("check", "onDataChange: orderId " + dsp.getKey());
                }

                //getOrderDetails();
                checkStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //  dialog.dismiss();
            }
        });
    }

    public void checkStatus() {
        for (int i = 0; i < orderList.size(); i++) {
            final String orderNo = orderList.get(i);

            FirebaseDatabase.getInstance().getReference().child("admin/orders/" + mDate).child(mMenuId).child("orders").child(orderNo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        mStatus = (String) hashUser.get("status");
                        //StaticConfig.STATUS.add(mStatus);
                        //status.add(mStatus);
                        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(mDate).child(orderNo).child("orderDetails").child("status").setValue(mStatus);

                        //check=1;

                        Log.d("check", "status: " + mStatus);

                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
        }

        getOrderDetails();
    }

    public void getOrderDetails() {

        for (int i = 0; i < orderList.size(); i++) {
            String orderId = orderList.get(i);
            //StaticConfig.ORDERID.add(orderId);

            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("orders").child(mDate).child(orderId).child("orderDetails")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            //fetch files from firebase database and push in arraylist
                            OrderDetails details = dataSnapshot.getValue(OrderDetails.class);


                            obj.add(details);

                            Log.d("Check list", "onDataChange: " + obj.size());

                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }
       // recyclerView.setAdapter(adapter);
        //adapter.notifyDataSetChanged();

    }
}
