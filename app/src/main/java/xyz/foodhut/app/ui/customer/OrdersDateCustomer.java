package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
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

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.OrderDateProvider;
import xyz.foodhut.app.data.StaticConfig;

public class OrdersDateCustomer extends AppCompatActivity {
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
        setContentView(R.layout.activity_orders_date_customer);

        getSupportActionBar().setTitle("OrdersProvider");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        arrayList=new ArrayList<>();
        recyclerView=findViewById(R.id.rvOrderDateCustomer);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter=new OrderDateProvider(this,arrayList,"customer");
        recyclerView.setAdapter(orderAdapter);

        dialog=new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }

        if(StaticConfig.UID!=null) {
            FirebaseDatabase.getInstance().getReference("customers/" + userID+"/orders").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for(DataSnapshot dsp : dataSnapshot.getChildren()){
                        arrayList.add(String.valueOf(dsp.getKey())); //add result into array list

                        Log.d("check", "onDataChange: date "+dsp.getKey());
                    }


                    // for(int i=0;i<obj.size();i++){
                    //    if(obj.get(i).mImageUrl==null||obj.get(i).mImageUrl==null){
                    //         obj.remove(i);
                    //     }
                    //  }

                    //  Collections.reverse(obj);

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
