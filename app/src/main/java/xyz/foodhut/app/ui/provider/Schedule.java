package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collections;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.ScheduleProvider;

public class Schedule extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<ScheduleProvider> arrayList = new ArrayList<>();
    String userID = null;
    xyz.foodhut.app.adapter.ScheduleProvider adapter;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog dialog;
    private String mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();



        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvSchedule);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new xyz.foodhut.app.adapter.ScheduleProvider(this, arrayList, 1);
        recyclerView.setAdapter(adapter);

        getScheduleList();


    }


    public void getScheduleList() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            userID = user.getUid();
        }


        if (StaticConfig.UID != null) {
            dialog.show();
            FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID).child("schedule")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                dialog.dismiss();
                                //fetch files from firebase database and push in arraylist
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                                    getSchedule(String.valueOf(snapshot.getKey()));

                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    public void getSchedule(String menuId){
        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID).child("schedule").child(menuId)
//                .child(mDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            dialog.dismiss();
                            //fetch files from firebase database and push in arraylist
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                xyz.foodhut.app.model.ScheduleProvider scheduleProvider = snapshot.getValue(xyz.foodhut.app.model.ScheduleProvider.class);

                                //   if (scheduleProvider.date.equals(mDate)) {
                                //      arrayList.add(scheduleProvider);
                                //   }
                                arrayList.add(scheduleProvider);
                                //menuAdapter.notifyDataSetChanged();
                                Log.d("Check list", "onDataChange: " + arrayList.size());
                            }

                            //  for (int i = 0; i < arrayList.size(); i++) {
                            //      if (!arrayList.get(i).date.equals(mDate)) {
                            //        arrayList.remove(i);
                            //      }
                            //  }


                            Collections.reverse(arrayList);

                            //bind the data in adapter
                            Log.d("Check list", "out datachange: " + arrayList.size());
                            adapter.notifyDataSetChanged();

                            dialog.dismiss();
                        }
                        else {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });
    }

    public void goBack(View view) {
        //   startActivity(new Intent(this,HomeProvider.class));
        finish();
        //  finishAffinity();
    }
}
