package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;

public class Favourites extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<MenuCustomer> arrayList = new ArrayList<>();
    ArrayList<String> menuList;
    String userID = null;
    xyz.foodhut.app.adapter.MenuCustomer menuAdapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        arrayList = new ArrayList<>();
        menuList =new ArrayList<>();
        recyclerView = findViewById(R.id.rvFavourites);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new xyz.foodhut.app.adapter.MenuCustomer(this, arrayList,true);
        recyclerView.setAdapter(menuAdapter);



        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.show();

        if (!isConnected()){
            dialog.cancel();
            Toast.makeText(this, "Sorry, you don't have an active internet connection", Toast.LENGTH_SHORT).show();
        }

        FirebaseDatabase.getInstance().getReference("customers/" + StaticConfig.UID + "/favourites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.dismiss();
                //fetch files from firebase database and push in arraylist

                //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    menuList.add(String.valueOf(snapshot.getKey())); //add result into array list
                //    getMenu(snapshot.getKey());
                    getFavourites(snapshot.getKey());
                    Log.d("check", "onDataChange: id " + snapshot.getKey()+" "+snapshot.getValue());
                }

                Log.d("check", "menuList size: " + menuList.size());
               // getOrderList();
                //  Collections.reverse(obj);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });
    }

    public boolean isConnected(){
        ConnectivityManager cm= (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network= null;
        if (cm != null) {
            network = cm.getActiveNetworkInfo();
        }

        return   (network!=null && network.isConnected());
    }

    public void goBack(View view){
       // startActivity(new Intent(this,HomeCustomer.class));
        finish();
     //   finishAffinity();
    }

    public void getMenu(final String id){
        FirebaseDatabase.getInstance().getReference("schedule").child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue()!=null) {
                            xyz.foodhut.app.model.MenuCustomer customer = dataSnapshot.getValue(xyz.foodhut.app.model.MenuCustomer.class);

                            String menuId=customer.id;
                            String providerId=customer.providerId;
                            getFavourites(menuId);
                          //  arrayList.add(customer);
                            Log.d("Check", "menu fav: " + menuId);
                        }
                     //   Log.d("Check", "out list fav: " + arrayList.size());
                      //  menuAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });
    }

    public void getFavourites(final String menuId){
        FirebaseDatabase.getInstance().getReference("schedule")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue()!=null) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                xyz.foodhut.app.model.MenuCustomer customer = snapshot.getValue(xyz.foodhut.app.model.MenuCustomer.class);
                                if (customer != null && customer.id.equals(menuId))
                                    arrayList.add(customer);
                                Log.d("Check", "list fav: " + arrayList.size());
                            }
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
