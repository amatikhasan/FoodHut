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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.OrderDateProvider;
import xyz.foodhut.app.data.StaticConfig;

public class OrdersDate extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> arrayList;
    String userID=null;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    OrderDateProvider orderAdapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_date);

        getSupportActionBar().setTitle("OrdersProvider");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        arrayList=new ArrayList<>();
        recyclerView=findViewById(R.id.rvOrderDateProvider);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter=new OrderDateProvider(this,arrayList,"provider");
        recyclerView.setAdapter(orderAdapter);

        dialog=new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }

     /*   if(user!=null) {
            userID = user.getUid();
        }
        else {
            //
            Toast.makeText(this, "Error authenticating", Toast.LENGTH_SHORT).show();
        }  */


        if(StaticConfig.UID!=null) {
            FirebaseDatabase.getInstance().getReference("providers/" + userID+"/orders").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            //fetch files from firebase database and push in arraylist

                            //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                            for(DataSnapshot dsp : dataSnapshot.getChildren()){
                                arrayList.add(String.valueOf(dsp.getKey())); //add result into array list

                                Log.d("check", "onDataChange: date "+dsp.getKey());
                            }



                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

                            for (int i = 0; i < arrayList.size() - 1; i++) {
                                for (int j = 0; j < arrayList.size() - i - 1; j++) {

                                    Date date1 = null;
                                    Date date2 = null;
                                    try {
                                        date1 = dateFormat.parse(arrayList.get(j));
                                        date2 = dateFormat.parse(arrayList.get(j + 1));
                                        Log.d("Check", "dates : " + date1 + " " + date2);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    if (date1.before(date2)) {

                                        String temp;
                                        temp = arrayList.get(j);
                                        arrayList.set(j, arrayList.get(j + 1));
                                        arrayList.set(j + 1, temp);

                                        Log.d("Check", "dates comparing: " + i + " " + j);
                                    }

                                }
                            }

                            //bind the data in adapter
                            Log.d("Check list", "out datachange: " + arrayList.size());
                            orderAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });

        }

    }
}
