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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderDetailsProvider;
import xyz.foodhut.app.model.Review;

public class OrdersCustomer extends RecyclerView.Adapter<OrdersCustomer.ViewHolder> {
    private Context contex;
    private ArrayList<OrderDetails> orderData;
    private float menuRating, tempRate, kitchenRating, kitchenTempRating;
    private int ratingCount, kitchenRatingCount;
    private String newRating;
    private String newCount;
    private String kitchenNewRating, kitchenNewCount;

    public OrdersCustomer(Context contex, ArrayList<OrderDetails> orderData) {
        this.contex = contex;
        this.orderData = orderData;

        Log.d("check", "OrdersCustomer: size " + orderData.size());
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

        //  final OrderDetailsProvider obj = data.get(position);
        final OrderDetails obj = orderData.get(position);
        Log.d("check", "onBindViewHolder: " + obj.itemName + " " + obj.menuId + " " + obj.date + " " + obj.status);

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

        holder.foodName.setText(obj.itemName);
        // String price = String.valueOf(obj2.price);
        //  holder.price.setText(price);

        holder.quantity.setText(String.valueOf(obj.quantity));
        if (obj.extraQuantity > 0) {
            holder.pkt.setText("pkt.+Extra");
        }
        else
            holder.pkt.setText("pkt.");

        holder.amount.setText(String.valueOf(obj.amount));
        String date = obj.date + " " + obj.time;
        holder.date.setText(date);
        holder.time.setText(obj.statusTime);
        String orderNo;

        if (obj.menuCount > 0)
            orderNo = "O-" + obj.orderId + " (" + obj.menuCount + ")";
        else
            orderNo = "O-" + obj.orderId;

        holder.orderNo.setText(orderNo);
        // holder.payment.setText(obj2.payment);
        holder.providerName.setText(obj.provider);
        holder.address.setText(obj.cAddress);


        if (obj.status.equals("Pending")) {
            holder.status.setText("Pending");
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_yellow));
            holder.review.setVisibility(View.GONE);
        }
        if (obj.status.equals("Cancelled") || obj.status.equals("Rejected")) {
            holder.status.setText("Cancelled");
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
            holder.cancel.setVisibility(View.GONE);
            holder.review.setVisibility(View.GONE);
        }
        if (obj.status.equals("Accepted")) {
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_blue));
            holder.status.setText("Accepted");
            holder.review.setVisibility(View.GONE);
        }
        if (obj.status.equals("Shipped")) {
            holder.status.setText("Shipped");
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_orange));
            holder.review.setVisibility(View.GONE);
        }
        if (obj.status.equals("Delivered")) {
            holder.status.setText("Delivered");
            holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_green));
            holder.review.setVisibility(View.VISIBLE);
        }

        if (obj.paymentStatus.equals("Paid")) {
            holder.payment.setBackground(contex.getResources().getDrawable(R.drawable.round_button_green));
            holder.payment.setText(obj.paymentStatus);
        } else {
            holder.payment.setText(obj.paymentStatus);
            holder.payment.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
        }

        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID)
                .child("orders").child(obj.date).child(obj.orderId).child("orderDetails").child("rating")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {
                            float rating = Float.parseFloat(dataSnapshot.getValue().toString());

                            if (rating > 0) {
                                holder.review.setText("My Review");
                            }

                            Log.d("check", "rating: " + rating);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        checkRating(obj, holder);
        checkKitchenRating(obj, holder);
        checkCancelable(obj, holder);

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(contex);
                builder.setMessage("Are you sure you want to cancel this order?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                holder.cancel.setVisibility(View.GONE);
                                holder.status.setText("Cancelled");
                                //  holder.status.setBackground(contex.getResources().getDrawable(R.drawable.round_button_primary));
                                FirebaseDatabase.getInstance().getReference().child("admin/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Cancelled");
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Cancelled");
                                FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("status").setValue("Cancelled");

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final android.app.AlertDialog alert = builder.create();
                alert.show();
                Log.d("check method", "from alert");
            }
        });

        holder.review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater layoutInflater = LayoutInflater.from(contex);
                View inflate = layoutInflater.inflate(R.layout.layout_add_review, null);

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(contex);
                builder.setView(inflate);


                final EditText etReview = inflate.findViewById(R.id.rlReview);
                final EditText etComplain = inflate.findViewById(R.id.rlComplain);
                final RatingBar ratingBar = inflate.findViewById(R.id.rlRatingBar);
                final TextView orderNo = inflate.findViewById(R.id.rlOrderNo);
                String order = "O-" + obj.orderId;
                orderNo.setText(order);
                final String[] review = {""};

                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        ratingBar.setRating(rating);

                        tempRate = ((menuRating * ratingCount) + rating) / (ratingCount + 1);
                        newRating = String.valueOf(tempRate);
                        newCount = String.valueOf(ratingCount + 1);

                        kitchenTempRating = ((kitchenRating * kitchenRatingCount) + rating) / (kitchenRatingCount + 1);
                        kitchenNewRating = String.valueOf(kitchenTempRating);
                        kitchenNewCount = String.valueOf(kitchenRatingCount + 1);

                        Log.d("check", "onRatingChanged: " + newRating + " " + newCount + " " + kitchenNewRating + " " + kitchenNewCount);
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
                        FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //fetch files from firebase database and push in arraylist
                                        if (dataSnapshot.getValue() != null) {
                                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                                            try {
                                                float mRating = Float.parseFloat((String) hashUser.get("rating"));
                                                String mReview = (String) hashUser.get("review");
                                                String mComplain = (String) hashUser.get("complain");


                                                if (mRating != 0) {
                                                    ratingBar.setRating(mRating);
                                                    etReview.setText(mReview);
                                                    etReview.setEnabled(false);
                                                    ratingBar.setIsIndicator(true);

                                                    okButton.setEnabled(false);
                                                }
                                                if (!mComplain.isEmpty()){
                                                    etComplain.setText(mComplain);
                                                    etComplain.setEnabled(false);
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
                                String complain = etComplain.getText().toString();

                                FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("rating").setValue(newRating);
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("menu").child(obj.menuId).child("rating").setValue(newRating);
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("menu").child(obj.menuId).child("ratingCount").setValue(newCount);
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("about").child("rating").setValue(kitchenNewRating);
                                FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("about").child("ratingCount").setValue(kitchenNewCount);


                                if (!review[0].isEmpty()) {
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("review").setValue(review[0]);
                                    //FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId).child("reviews").push().setValue(review);

                                    Calendar c = Calendar.getInstance();
                                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                                    final String date = dateFormat.format(c.getTime());

                                    String rating = String.valueOf(newRating);
                                    Review mReview = new Review();
                                    mReview.menuName = obj.itemName;
                                    mReview.menuId = obj.menuId;
                                    mReview.customerName = obj.customer;
                                    mReview.rating = rating;
                                    mReview.review = review[0];
                                    mReview.time = date;

                                    Log.d("check", "onClick: review " + mReview.review);
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("reviews").push().setValue(mReview);

                                    Toast.makeText(contex, "Review is submitted!", Toast.LENGTH_SHORT).show();


                                    final Notification notification2 = new Notification();
                                    notification2.orderId = obj.orderId;
                                    notification2.title = "Received review on order O-" + obj.orderId;
                                    notification2.status = "R";
                                    notification2.time = date;

                                    final String key = FirebaseDatabase.getInstance().getReference().push().getKey();
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/notifications/old").child(key).setValue(notification2);
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/notifications/new").child(key).setValue(notification2);

                                }

                                if (!complain.isEmpty()) {
                                    FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/orders").child(obj.date).child(obj.menuId).child("orders").child(obj.orderId).child("complain").setValue(complain);
                                    //FirebaseDatabase.getInstance().getReference().child("providers/" + mProviderId).child("menu").child(mMenuId).child("reviews").push().setValue(review);

                                    Calendar c = Calendar.getInstance();
                                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                                    final String date = dateFormat.format(c.getTime());

                                    Review mReview = new Review();
                                    mReview.menuName = obj.itemName;
                                    mReview.menuId = obj.menuId;
                                    mReview.customerName = obj.customer;
                                    mReview.kitchenName = obj.provider;
                                    mReview.review = complain;
                                    mReview.time = date;

                                    Log.d("check", "onClick: review " + mReview.review);
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("complains").push().setValue(mReview);
                                    FirebaseDatabase.getInstance().getReference().child("admin").child("complains").push().setValue(mReview);

                                    if (review[0].isEmpty())
                                        Toast.makeText(contex, "Complain is submitted!", Toast.LENGTH_SHORT).show();

                                    final Notification notification2 = new Notification();
                                    notification2.orderId = obj.orderId;
                                    notification2.title = "Received complain on order O-" + obj.orderId;
                                    notification2.status = "C";
                                    notification2.time = date;

                                    final String key = FirebaseDatabase.getInstance().getReference().push().getKey();
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/notifications/old").child(key).setValue(notification2);
                                    FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId + "/notifications/new").child(key).setValue(notification2);

                                }

                                ratingBar.setIsIndicator(true);
                                holder.review.setText("My Review");
                                dialog.dismiss();
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void checkCancelable(OrderDetails obj, ViewHolder holder) {
        String mTime;
        Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        final String date = sdf.format(calendar.getTime());


        // From calander get the year, month, day, hour, minute

        if (minute < 10) {
            mTime = formatTime(String.valueOf(hour) + ":" + "0" + String.valueOf(minute));
            //mTime = formatTime(time);
            //mTime=time;
        } else {
            mTime = formatTime(String.valueOf(hour) + ":" + String.valueOf(minute));
            // mTime = formatTime(time);
            //mTime=time;
        }

        SimpleDateFormat stf = new SimpleDateFormat("hh:mm a");
        Date date1 = null;
        Date date2 = null;
        Date today3 = null;
        Date schedule4 = null;

        try {
            date2 = stf.parse(obj.lastTime);
            date1 = stf.parse(mTime);

            today3 = sdf.parse(date);
            schedule4 = sdf.parse(obj.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("check", "order: " + date + " " + date1 + " " + date2 + " " + today3 + " " + schedule4);

        if (today3.equals(schedule4)) {
            if (date1.before(date2)) {
                if (!obj.status.equals("Cancelled") && !obj.status.equals("Shipped") && !obj.status.equals("Delivered"))
                    holder.cancel.setVisibility(View.VISIBLE);
            }
        }
        if (today3.before(schedule4)) {
            if (!obj.status.equals("Cancelled") && !obj.status.equals("Shipped") && !obj.status.equals("Delivered"))
                holder.cancel.setVisibility(View.VISIBLE);
        }
    }

    //formate time with AM,PM for button
    public String formatTime(String time) {
        String format, formattedTime, minutes;
        String[] dateParts = time.split(":");
        int hour = Integer.parseInt(dateParts[0]);
        int minute = Integer.parseInt(dateParts[1]);
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        if (minute < 10)
            minutes = "0" + minute;
        else
            minutes = String.valueOf(minute);
        formattedTime = hour + ":" + minutes + " " + format;

        return formattedTime;
    }

    public void checkRating(OrderDetails obj, final ViewHolder holder) {
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

    public void checkKitchenRating(OrderDetails obj, final ViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("providers/" + obj.pId).child("about")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist
                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            kitchenRating = Float.parseFloat((String) hashUser.get("rating"));
                            kitchenRatingCount = Integer.parseInt((String) hashUser.get("ratingCount"));

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
        return orderData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, cancel, date, time, orderNo, providerName, status, payment, amount, quantity, address, review, pkt;
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
            payment = itemView.findViewById(R.id.oscPaymentStatus);
            cancel = itemView.findViewById(R.id.oscCancelOrder);
            amount = itemView.findViewById(R.id.oscAmount);
            quantity = itemView.findViewById(R.id.oscQty);
            address = itemView.findViewById(R.id.oscAddress);
            review = itemView.findViewById(R.id.oscReview);

            //card = itemView.findViewById(R.id.cardMenu);
        }

    }


}
