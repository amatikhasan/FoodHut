package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.NotificationAdapter;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;

public class NotificationProvider extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Notification> arrayList = new ArrayList<>();
    NotificationAdapter adapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_provider);


        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvNotification);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, arrayList,"provider");
        recyclerView.setAdapter(adapter);



        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();


        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/notifications/old")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            arrayList.add(notification);
                            Log.d("Check", "list noti: " + arrayList.size());
                        }

                        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/notifications/new").removeValue();

                        Collections.reverse(arrayList);
                        //bind the data in adapter
                        Log.d("Check", "list noti: " + arrayList.size());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });
    }

    public void goBack(View view){
      //  startActivity(new Intent(this,HomeProvider.class));
        finish();
       // finishAffinity();
    }
}
