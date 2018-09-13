package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;

public class Favourites extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<MenuCustomer> arrayList = new ArrayList<>();
    ArrayList<String> idList ;
    String userID = null;
    xyz.foodhut.app.adapter.MenuCustomer menuAdapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        arrayList = new ArrayList<>();
        idList=new ArrayList<>();
        recyclerView = findViewById(R.id.rvFavourites);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new xyz.foodhut.app.adapter.MenuCustomer(this, arrayList);
        recyclerView.setAdapter(menuAdapter);



        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Favourite Menus...");
        dialog.show();

        FirebaseDatabase.getInstance().getReference("customers/" + StaticConfig.UID + "/favourites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.dismiss();
                //fetch files from firebase database and push in arraylist

                //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(String.valueOf(snapshot.getKey())); //add result into array list
                    getMenu(snapshot.getKey());
                    Log.d("check", "onDataChange: id " + snapshot.getKey()+" "+snapshot.getValue());
                }

                Log.d("check", "idList size: " + idList.size());
               // getOrderList();
                //  Collections.reverse(obj);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });



    }

    public void getMenu(String id){
        FirebaseDatabase.getInstance().getReference("schedule").child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue()!=null) {
                            xyz.foodhut.app.model.MenuCustomer customer = dataSnapshot.getValue(xyz.foodhut.app.model.MenuCustomer.class);
                            arrayList.add(customer);
                            Log.d("Check", "list fav: " + arrayList.size());
                        }
                        Log.d("Check", "out list fav: " + arrayList.size());
                        menuAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });
    }
}
