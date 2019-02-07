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
import xyz.foodhut.app.adapter.ReviewAdapter;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.Review;

public class Reviews extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Review> arrayList = new ArrayList<>();
    ReviewAdapter adapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);


        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvReview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(this, arrayList,"reviews");
        recyclerView.setAdapter(adapter);



        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();


        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/reviews")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Review review = snapshot.getValue(Review.class);
                            arrayList.add(review);
                            Log.d("Check", "list review: " + arrayList.size());
                        }

                        Collections.reverse(arrayList);
                        //bind the data in adapter
                        Log.d("Check", "list review: " + arrayList.size());
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
      //  finishAffinity();
    }
}
