package xyz.foodhut.app.ui.customer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.Review;

public class OrderStatusCustomer extends AppCompatActivity {

    ImageView image;
    TextView orderNo, menuName, providerName, quantity, extraItem, extraQty, amount,status;
    Button rateNow;
    Bundle extras;
    String mCustomerName,mOrderNo, mName, mProviderName,mProviderId, mExtraItem, mStatus,mReview, mDate, mMenuId, mImageUrl, mType;
    long mQty,mExtraQty,mAmount;
    float mRating=0,menuRating,tempRate,newRating;
    String ratings,count,review;
    int ratingCount;
    ArrayList<OrderDetails> obj;
    RatingBar ratingBar;
    LinearLayout llRating;
    CardView cvRating;
    EditText etReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status_customer);

        obj = new ArrayList<>();
        orderNo = findViewById(R.id.osNumber);
        menuName = findViewById(R.id.osName);
        providerName = findViewById(R.id.osProvider);
        quantity = findViewById(R.id.osQty);
        extraItem = findViewById(R.id.txtExtraQty);
        extraQty = findViewById(R.id.osExtraQty);
        amount = findViewById(R.id.osAmount);
        image = findViewById(R.id.osImage);
        status = findViewById(R.id.osBtnStatus);
        rateNow = findViewById(R.id.osBtnRate);
        ratingBar=findViewById(R.id.ratingBar);
        cvRating=findViewById(R.id.cvRating);
        etReview=findViewById(R.id.osReview);

        extras = getIntent().getExtras();
        if (extras != null) {
            mDate = extras.getString("date");
            mMenuId = extras.getString("menuId");
            mName = extras.getString("tvName");
            mProviderName = extras.getString("provider");
            mOrderNo = extras.getString("orderId");
            mType = extras.getString("type");
            mImageUrl = extras.getString("url");
            mProviderId=extras.getString("providerId");

            Log.d("check", "order customer: " + mDate + " " + mOrderNo);
        }


        checkStatus();

        getDetails();

        checkRating();


    }

    public void checkStatus() {
        FirebaseDatabase.getInstance().getReference().child("orders/" + mDate).child(mMenuId).child("orders").child(mOrderNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    mStatus = (String) hashUser.get("status");
                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(mDate).child(mOrderNo).child("orderDetails").child("status").setValue(mStatus);

                    Log.d("check", "status: " + mStatus);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void getDetails() {
        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("orders").child(mDate).child(mOrderNo).child("orderDetails")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            mStatus = (String) hashUser.get("status");
                            mQty= (long) hashUser.get("quantity");
                            mExtraItem= (String) hashUser.get("extraItem");
                            mExtraQty= (long) hashUser.get("extraQuantity");
                            mAmount= (long) hashUser.get("amount");
                            mCustomerName=(String) hashUser.get("customer");

                            try {
                                mRating = Float.parseFloat((String) hashUser.get("rating"));
                                mReview=(String) hashUser.get("review");
                                if(mRating!=0){
                                    ratingBar.setIsIndicator(true);
                                }

                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }



                            updateUI();
                            Log.d("check", "status: " + mStatus+" "+mQty+" "+mExtraQty+" "+mAmount);

                        }
                        //OrderDetails orderDetails = dataSnapshot.getValue(OrderDetails.class);
                       // obj.add(orderDetails);

                        Log.d("Check list", "onDataChange: " + obj.size());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void updateUI() {

        Picasso.get().load(mImageUrl).placeholder(R.drawable.image).into(image);

        orderNo.setText(mOrderNo);
        menuName.setText(mName);
        providerName.setText(mProviderName);
        quantity.setText(String.valueOf(mQty));
        String extra = "Extra " + mExtraItem;
        extraItem.setText(extra);
        extraQty.setText(String.valueOf(mExtraQty));
        amount.setText(String.valueOf(mAmount));

        if(mStatus.equals("Delivered")){
            cvRating.setVisibility(View.VISIBLE);

            if(mRating!=0) {
                ratingBar.setRating(mRating);
                ratingBar.setIsIndicator(true);

                etReview.setEnabled(false);
                rateNow.setVisibility(View.INVISIBLE);

            }

            if(mReview!=null){
                etReview.setText(mReview);
            }
        }

        status.setText(mStatus);
    }

    public void checkRating(){
        FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            menuRating = Float.parseFloat((String) hashUser.get("rating"));
                            ratingCount= Integer.parseInt((String) hashUser.get("ratingCount"));

                            Log.d("check", "rating: " + menuRating+" "+ratingCount);

                            if(mRating==0&&!ratingBar.isIndicator()) {

                                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                    @Override
                                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                                        ratingBar.setRating(rating);

                                        newRating=rating;

                                        tempRate=((menuRating*ratingCount)+rating)/(ratingCount+1);
                                        ratings=String.valueOf(tempRate);
                                        count=String.valueOf(ratingCount+1);

                                        Log.d("check", "onRatingChanged: "+ratingCount+" "+count);


                                    }
                                });
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void rateNow(View view){
        review=etReview.getText().toString();

        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(mDate).child(mOrderNo).child("orderDetails").child("rating").setValue(ratings);
        FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId).child("rating").setValue(ratings);
        FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId).child("ratingCount").setValue(count);

        if(review!=null) {
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(mDate).child(mOrderNo).child("orderDetails").child("review").setValue(review);
            //FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId).child("reviews").push().setValue(review);

            String rating=String.valueOf(newRating);
            Review mReview=new Review();
            mReview.menuName=mName;
            mReview.menuId=mMenuId;
            mReview.customerName=mCustomerName;
            mReview.rating=rating;
            mReview.review=review;

            FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("reviews").push().setValue(mReview);

        }

        ratingBar.setIsIndicator(true);
        etReview.setEnabled(false);
        rateNow.setVisibility(View.INVISIBLE);
    }
}
