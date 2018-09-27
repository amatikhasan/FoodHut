package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import xyz.foodhut.app.adapter.MenuProvider;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.HomeCustomer;

public class Menus extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<xyz.foodhut.app.model.MenuProvider> arrayList = new ArrayList<>();
    String userID=null;
    TextView emptyList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    MenuProvider menuAdapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

      //  getSupportActionBar().setTitle("Add Menu");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        emptyList=findViewById(R.id.emptyList);

        arrayList=new ArrayList<>();
        recyclerView=findViewById(R.id.rvMenu);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter=new MenuProvider(this,arrayList);
        recyclerView.setAdapter(menuAdapter);

        dialog=new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();

     /*   if(user!=null) {
            userID = user.getUid();
        }
        else {
            //
            Toast.makeText(this, "Error authenticating", Toast.LENGTH_SHORT).show();
        }  */


        if(StaticConfig.UID!=null) {
            FirebaseDatabase.getInstance().getReference("providers/"+ StaticConfig.UID).child("menu")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialog.dismiss();
                    if (dataSnapshot.getValue() != null) {
                        //fetch files from firebase database and push in arraylist
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            xyz.foodhut.app.model.MenuProvider menuProvider = snapshot.getValue(xyz.foodhut.app.model.MenuProvider.class);
                            arrayList.add(menuProvider);
                            //menuAdapter.notifyDataSetChanged();
                            Log.d("Check list", "onDataChange: " + arrayList.size());
                        }

                        for (int i = 0; i < arrayList.size(); i++) {
                            if (arrayList.get(i).imageUrl == null || arrayList.get(i).imageUrl == null) {
                                arrayList.remove(i);
                            }
                        }

                        Collections.reverse(arrayList);

                        //bind the data in adapter
                        Log.d("Check list", "out datachange: " + arrayList.size());
                        menuAdapter.notifyDataSetChanged();
                    }
                    else {
                        emptyList.setVisibility(View.VISIBLE);
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

    public void goBack(View view){
        startActivity(new Intent(this,HomeProvider.class));
        finish();
        finishAffinity();
    }

    public void fabBtn(View view){

        startActivity(new Intent(this,AddMenu.class
        ));
    }
}
