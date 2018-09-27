package xyz.foodhut.app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.model.Review;

public class OrdersCustomer extends RecyclerView.Adapter<OrdersCustomer.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetailsProvider> data;
    private ArrayList<OrderDetails> orderData;
    private float menuRating, tempRate;
    private int ratingCount;
    private String newRating;
    private String newCount;


    public OrdersCustomer(Context contex, ArrayList<OrderDetailsProvider> data, ArrayList<OrderDetails> orderData) {
        this.contex = contex;
        this.data = data;
        this.orderData = orderData;

        Log.d("check", "OrdersCustomer: size " + data.size() + " " + orderData.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(contex);
        View view = inflater.inflate(R.layout.order_status_customer, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final OrderDetailsProvider obj = data.get(position);
        final OrderDetails obj2 = orderData.get(position);
        Log.d("check", "onBindViewHolder: " + obj.name + " " + obj.menuId + " " + obj2.date);

     /*   SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        try {
            date1 = dateFormat.parse(obj2.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newFormate = new SimpleDateFormat("dd MMM yyyy");
        String date2 = newFormate.format(date1);
        */

        holder.foodName.setText(obj.name);
        String price = String.valueOf(obj.price);
      //  holder.price.setText(price);

        holder.quantity.setText(String.valueOf(obj2.quantity));
        if (obj2.extraQuantity>0){
            holder.pkt.setText("pkt.+Extra");
        }
        holder.amount.setText(String.valueOf(obj2.amount));
        String date=obj2.date+" "+obj2.time;
        holder.date.setText(date);
        holder.time.setText(obj2.statusTime);
        String orderNo = "O-" + obj2.orderId;
        holder.orderNo.setText(orderNo);
        // holder.payment.setText(obj2.payment);
        holder.providerName.setText(obj.provider);
        holder.address.setText(obj2.cAddress);

        if (obj2.status.equals("Pending")){
            holder.status.setText("Pending");
        }
        if (obj2.status.equals("Cancelled")||obj2.status.equals("Rejected")){
            holder.status.setText("Cancelled");
            holder.status.setBackgroundColor(contex.getResources().getColor(R.color.colorPrimary));
        }
        if (obj2.status.equals("Accepted")||obj2.status.equals("Ready")){
            holder.status.setBackgroundColor(contex.getResources().getColor(R.color.green));
            holder.status.setText("Processing");
        }
        if (obj2.status.equals("Shipped")){
            holder.status.setText("Shipped");
        }
        if (obj2.status.equals("Delivered")) {
            holder.status.setText("Delivered");
            holder.status.setBackgroundColor(contex.getResources().getColor(R.color.greenDark));
            holder.review.setVisibility(View.VISIBLE);
        }

        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID)
                .child("orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("rating")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {
                            float rating = Float.parseFloat(dataSnapshot.getValue().toString());

                            if (rating>0){
                                holder.review.setText("My Review");
                            }

                            Log.d("check", "rating: " + rating);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        checkRating(obj,holder);


        holder.review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater layoutInflater = LayoutInflater.from(contex);
                View inflate = layoutInflater.inflate(R.layout.layout_add_review, null);

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(contex);
                builder.setView(inflate);


                final EditText etReview = inflate.findViewById(R.id.rlReview);
                final RatingBar ratingBar = inflate.findViewById(R.id.rlRatingBar);
                final TextView orderNo = inflate.findViewById(R.id.rlOrderNo);
                String order = "O-" + obj2.orderId;
                orderNo.setText(order);
                final String[] review = {""};

                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        ratingBar.setRating(rating);

                        tempRate = ((menuRating * ratingCount) + rating) / (ratingCount + 1);
                        newRating = String.valueOf(tempRate);
                        newCount = String.valueOf(ratingCount + 1);

                        Log.d("check", "onRatingChanged: " + newRating + " " + newCount);
                    }
                });

                builder.setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                final android.support.v7.app.AlertDialog alertDialog = builder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                        //check if reviewis given or not
                        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID).child("orders").child(obj2.date).child(obj2.orderId).child("orderDetails")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //fetch files from firebase database and push in arraylist
                                        if (dataSnapshot.getValue() != null) {
                                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                                            try {
                                                float mRating = Float.parseFloat((String) hashUser.get("rating"));
                                                String mReview = (String) hashUser.get("review");
                                                if (mRating != 0) {
                                                    ratingBar.setRating(mRating);
                                                    etReview.setText(mReview);
                                                    etReview.setEnabled(false);
                                                    ratingBar.setIsIndicator(true);

                                                    okButton.setEnabled(false);
                                                }
                                                Log.d("check", "myRating: " + mRating + " " + mReview);

                                            } catch (NullPointerException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                review[0] = etReview.getText().toString();

                                FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("rating").setValue(newRating);
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("menu").child(obj.menuId).child("rating").setValue(newRating);
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("menu").child(obj.menuId).child("ratingCount").setValue(newCount);

                                if (!review[0].equals("")) {
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj2.date).child(obj2.orderId).child("orderDetails").child("review").setValue(review[0]);
                                    //FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId).child("reviews").push().setValue(review);

                                    String rating = String.valueOf(newRating);
                                    Review mReview = new Review();
                                    mReview.menuName = obj.name;
                                    mReview.menuId = obj.menuId;
                                    mReview.customerName = obj2.customer;
                                    mReview.rating = rating;
                                    mReview.review = review[0];

                                    Log.d("check", "onClick: review " + mReview.review);
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("reviews").push().setValue(mReview);

                                }

                                ratingBar.setIsIndicator(true);
                                Toast.makeText(contex, "Review is submitted!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });
    }

    public void checkRating(OrderDetailsProvider obj, final ViewHolder holder){
        FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("menu").child(obj.menuId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            menuRating = Float.parseFloat((String) hashUser.get("rating"));
                            ratingCount = Integer.parseInt((String) hashUser.get("ratingCount"));

                            Log.d("check", "rating: " + menuRating + " " + ratingCount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, price, date, time, orderNo, providerName, status, payment, amount, quantity, address, review,pkt;
        CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.oscFood);
            pkt = itemView.findViewById(R.id.pkt);
            providerName = itemView.findViewById(R.id.oscKitchen);
            date = itemView.findViewById(R.id.oscDate);
            time = itemView.findViewById(R.id.oscTime);
            orderNo = itemView.findViewById(R.id.oscOderNo);
            status = itemView.findViewById(R.id.oscStatus);
            //  payment = itemView.findViewById(R.id.oscPayment);
            amount = itemView.findViewById(R.id.oscAmount);
            quantity = itemView.findViewById(R.id.oscQty);
            address = itemView.findViewById(R.id.oscAddress);
            review = itemView.findViewById(R.id.oscReview);

            //card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
