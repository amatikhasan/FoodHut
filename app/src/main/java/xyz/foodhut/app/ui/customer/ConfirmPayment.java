package xyz.foodhut.app.ui.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.MenuCustomer;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderMenuDetails;

import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class ConfirmPayment extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    Bundle extras;

    String cName, cAddress, cPhone, mCoupon, mScheduleId, mPayment, time, note,promoType="";
    String mName, mExtraItem, mNote, mTime, mAddress, mProviderAddress, mProviderPhone,
            mId, orderNo, mType, mImageUrl, mDate, mLastTime, mProviderId, mProviderName, orderType, singleOrderDate, singleMenuId;
    int mQuantity = 1, mExtraQuantity = 0, mSubTotal = 0, mTotal = 0,mAvailable=0, mFinalAmount = 0,mSellerPrice=0, mSellerAmount = 0, mCouponValue = 0, mPrice = 0, mExtraItemPrice = 0, mExtraSubTotal = 0, deliveryCharge = 0;


    EditText etTrxId;
    TextView step1, step2;

    ArrayList<MenuCustomer> arrayList;
    ArrayList<Integer> itemQtyList;
    ArrayList<Integer> extraQtyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_payment);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        etTrxId = findViewById(R.id.etTrxId);
        step1 = findViewById(R.id.txtStep4);
        step2 = findViewById(R.id.txtStep5);

        arrayList = new ArrayList<>();
        itemQtyList = new ArrayList<>();
        extraQtyList = new ArrayList<>();
        arrayList.clear();
        itemQtyList.clear();

        for (int i = 0; i < StaticConfig.ITEMQTYLIST.size(); i++) {
            if (StaticConfig.ITEMQTYLIST.get(i) == 0) {
                StaticConfig.ITEMQTYLIST.remove(i);
                StaticConfig.ORDERITEMLIST.remove(i);
            }
        }

        arrayList = StaticConfig.ORDERITEMLIST;
        itemQtyList = StaticConfig.ITEMQTYLIST;

        Log.d("check", "onCreate: " + arrayList.size() + " " + itemQtyList.size());


        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            orderType = extras.getString("type");
            promoType = extras.getString("promoType");
            mId = extras.getString("menuId");
            mName = extras.getString("menuName");
            mScheduleId = extras.getString("scheduleId");
            mDate = extras.getString("date");
            cName = extras.getString("cName");
            cAddress = extras.getString("cAddress");
            cPhone = extras.getString("cPhone");

            mTime = extras.getString("time");
            mLastTime = extras.getString("lastTime");
            note = extras.getString("note");
            orderNo = extras.getString("orderNo");
            mProviderId = extras.getString("providerId");
            mProviderName = extras.getString("providerName");
            mProviderAddress = extras.getString("providerAddress");
            mCoupon = extras.getString("coupon");
            mCouponValue = extras.getInt("couponValue");
            deliveryCharge = extras.getInt("dc");
            mFinalAmount = extras.getInt("amount");
            mSellerAmount= extras.getInt("sellerAmount");

            mQuantity=extras.getInt("qty");
            mAvailable=extras.getInt("available");
            mExtraQuantity=extras.getInt("extraQty");
            mExtraItem=extras.getString("extraItem");

            mSellerPrice=extras.getInt("sellerPrice");
            mExtraItemPrice=extras.getInt("extraItemPrice");

            String mStep4 = "Enter amount of ৳ " + mFinalAmount ;
            String mStep5 = "Enter order no. O-" + orderNo + " as reference";

            step1.setText(mStep4);
            step2.setText(mStep5);

            Log.d("check", "extraItem: " + orderNo);
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

    public void goBack(View view) {
        //  startActivity(new Intent(this,HomeCustomer.class));
        finish();
        // finishAffinity();
    }

    public boolean isConnected(){
        ConnectivityManager cm= (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network= null;
        if (cm != null) {
            network = cm.getActiveNetworkInfo();
        }

        return   (network!=null && network.isConnected());
    }

    public void confirm(View view) {

        if (isConnected()) {

            if (!etTrxId.getText().toString().isEmpty()) {

                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("Please wait..");
                dialog.show();

                // Get the calander
                Calendar c = Calendar.getInstance();

                String month = new SimpleDateFormat("MMM", Locale.US).format(c.getTime());
                int day = c.get(Calendar.DAY_OF_MONTH);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                String time = day + " " + month + " " + formatTime(hour + ":" + minute);

                if (orderType.equals("multiple")) {


                    //get the signed in user
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        userID = user.getUid();
                    }

                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                    final String date = dateFormat.format(c.getTime());

                    final String key = databaseReference.push().getKey();

                    for (int i = 0; i < arrayList.size(); i++) {

                        addOrder(i, key, orderNo, date, time, note);

                    }

                /*
                final int[] sellCountProvider = {0};
                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {

                            sellCountProvider[0] = Integer.parseInt(dataSnapshot.getValue().toString());
                            sellCountProvider[0]+=1;
                            databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellCount").setValue(sellCountProvider[0]);
                        }
                        else
                            databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellCount").setValue(1);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                */

                    final Notification notification = new Notification();
                    notification.orderId = orderNo;
                    notification.title = "Your order O-" + orderNo + " is on Pending";
                    notification.status = "P";
                    notification.time = date;

                    databaseReference.child("customers/" + userID + "/notifications/new").child(key).setValue(notification);
                    databaseReference.child("customers/" + userID + "/notifications/old").child(key).setValue(notification);

                    final Notification notification2 = new Notification();
                    notification2.orderId = orderNo;
                    notification2.title = "Received new order O-" + orderNo;
                    notification2.status = "O";
                    notification2.time = date;

                    databaseReference.child("providers/" + mProviderId + "/notifications/old").child(key).setValue(notification2);
                    databaseReference.child("providers/" + mProviderId + "/notifications/new").child(key).setValue(notification2);

                    databaseReference.child("providers/" + mProviderId + "/notifications/old").child(key).setValue(notification2);
                    databaseReference.child("providers/" + mProviderId + "/notifications/new").child(key).setValue(notification2);




                    StaticConfig.QTY = 0;
                    StaticConfig.SUBTOTAL = 0;
                    StaticConfig.TOTAL = 0;
                    StaticConfig.ORDERITEMLIST.clear();
                    StaticConfig.ITEMQTYLIST.clear();
                    StaticConfig.INDEXLIST.clear();
                    StaticConfig.EXTRAQTYLIST.clear();

                    StaticConfig.ISORDERED = 1;

                    startActivity(new Intent(this,HomeCustomer.class));
                    finish();
                    finishAffinity();
                }
                if (orderType.equals("single")) {

                    mSellerAmount = (mSellerPrice * mQuantity) + (mExtraItemPrice * mExtraQuantity);
                    Log.d("check", "confirm: " + mPrice + " " + mQuantity + " " + mSellerPrice + " " + mExtraQuantity + " " + mFinalAmount + " " + mSellerAmount);


                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                    final String date = dateFormat.format(c.getTime());

                    final String key = databaseReference.push().getKey();

                    final OrderMenuDetails menuDetails = new OrderMenuDetails(mId, mName, mType, mPrice, mImageUrl, mProviderId, mProviderName, mProviderAddress);
                    final OrderDetails orderDetails1 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, mName, mQuantity, mExtraItem, mExtraQuantity, note, mFinalAmount,mSellerAmount, mCouponValue, "bKash", "Pending", mDate, mLastTime, mTime, "Pending", time);



                    Log.d("check", "confirm: " + mPrice + " " + mQuantity + " " + mSellerPrice + " " + mExtraQuantity + " " + mFinalAmount + " " + mSellerAmount);



                    final OrderDetails orderDetails2 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, mName, mQuantity, mExtraItem, mExtraQuantity, note, mSellerAmount, "bKash", mDate, mLastTime, mTime, "Pending", time);


                    databaseReference.child("admin/orders/" + mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {
                                databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);

                                databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).child("trxId").setValue(etTrxId.getText().toString());

                            }
                            if (dataSnapshot.getValue() == null) {
                                databaseReference.child("admin/orders/" + mDate).child(mId).child("menuDetails").setValue(menuDetails);
                                databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);

                                databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).child("trxId").setValue(etTrxId.getText().toString());

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //  mSellerAmount = mFinalAmount + mCouponValue;

                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final Notification notification2 = new Notification();
                            notification2.orderId = orderNo;
                            notification2.title = "Received new order O-" + orderNo;
                            notification2.status = "O";
                            notification2.time = date;

                            if (dataSnapshot.getValue() != null) {
                                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails2);
                                databaseReference.child("providers/" + mProviderId + "/notifications/old").child(key).setValue(notification2);
                                databaseReference.child("providers/" + mProviderId + "/notifications/new").child(key).setValue(notification2);


                            }
                            if (dataSnapshot.getValue() == null) {
                                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("menuDetails").setValue(menuDetails);

                                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails2);
                                databaseReference.child("providers/" + mProviderId + "/notifications/new").child(key).setValue(notification2);
                                databaseReference.child("providers/" + mProviderId + "/notifications/old").child(key).setValue(notification2);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Notification notification = new Notification();
                            notification.orderId = orderNo;
                            notification.title = "Your order O-" + orderNo + " is on Pending";
                            notification.status = "P";
                            notification.time = date;

                            if (dataSnapshot.getValue() != null) {
                                databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);

                                String key = databaseReference.push().getKey();
                                databaseReference.child("customers/" + userID + "/notifications/new").child(key).setValue(notification);
                                databaseReference.child("customers/" + userID + "/notifications/old").child(key).setValue(notification);

                            }
                            if (dataSnapshot.getValue() == null) {
                                databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("menuDetails").setValue(menuDetails);

                                databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);
                                databaseReference.child("customers/" + userID + "/notifications/new").child(key).setValue(notification);
                                databaseReference.child("customers/" + userID + "/notifications/old").child(key).setValue(notification);
                            }

                            dialog.cancel();
                            Toast.makeText(ConfirmPayment.this, "Your order is placed and will arrive very soon.", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final int[] sellCountProvider = {0};
                    final int[] sellAmountProvider = {0};


                /*
                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {

                            sellCountProvider[0] = Integer.parseInt(dataSnapshot.getValue().toString());
                            sellCountProvider[0]+=1;
                            databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellCount").setValue(sellCountProvider[0]);
                        }
                        else
                            databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellCount").setValue(1);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });



                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {

                                sellAmountProvider[0] = Integer.parseInt(dataSnapshot.getValue().toString());
                                sellAmountProvider[0] += mSellerAmount;
                                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellAmount").setValue(sellAmountProvider[0]);

                            } else
                                databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellAmount").setValue(1);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

*/

                    int available = mAvailable - mQuantity;
                    databaseReference.child("schedule").child(mScheduleId).child("foodQty").setValue(available);


                    if (mCoupon != null) {
                        FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("coupon").child(mCoupon).child("usedBy")
                                .child(userID).setValue(userID);
                    }


                    StaticConfig.QTY = 0;
                    StaticConfig.SUBTOTAL = 0;
                    StaticConfig.TOTAL = 0;
                    StaticConfig.ORDERITEMLIST.clear();
                    StaticConfig.ITEMQTYLIST.clear();
                    StaticConfig.INDEXLIST.clear();

                    StaticConfig.ISORDERED = 1;

                    startActivity(new Intent(this,HomeCustomer.class));
                    finish();
                    finishAffinity();
                }
            } else {
                Toast.makeText(this, "Please Give TrxId of your payment.", Toast.LENGTH_SHORT).show();
            }

        }
        else
            Toast.makeText(this, "Sorry, you don't have an active internet connection", Toast.LENGTH_SHORT).show();


    }

    public void addOrder(int i, String key, String orderNo, String date, String time, String note) {

        mId = arrayList.get(i).id;
        mName = arrayList.get(i).name;
        mType = arrayList.get(i).type;
        mPrice = Integer.parseInt(arrayList.get(i).price);
        mImageUrl = arrayList.get(i).imageUrl;
        mProviderId = arrayList.get(i).providerId;
        mProviderName = arrayList.get(i).providerName;
        mProviderAddress = arrayList.get(i).providerAddress;
        mQuantity = StaticConfig.ITEMQTYLIST.get(i);
        mExtraItem = arrayList.get(i).extraItem;
        mExtraQuantity = StaticConfig.EXTRAQTYLIST.get(i);

        if (!arrayList.get(i).extraItemPrice.isEmpty())
        mExtraItemPrice= Integer.parseInt(arrayList.get(i).extraItemPrice);
        else
            mExtraItemPrice=0;

        mLastTime = arrayList.get(i).lastOrderTime;
        mSellerPrice= Integer.parseInt(arrayList.get(i).sellerPrice);

        mDate = arrayList.get(i).schedule;

      //  mFinalAmount = StaticConfig.TOTAL;

        int extra;

        if (promoType.equals("promo")) {
            // extra = (deliveryCharge) / arrayList.size();
            if (i == 0) {
                if (mQuantity>1)
                    mFinalAmount = (mPrice * (mQuantity - 1)) + mCouponValue + (mExtraQuantity * mExtraItemPrice) ;
                else
                    mFinalAmount =  mCouponValue + (mExtraQuantity * mExtraItemPrice) ;
            } else
                mFinalAmount = (mPrice * mQuantity) + (mExtraQuantity * mExtraItemPrice);
        } else {
            extra = (deliveryCharge - mCouponValue) / arrayList.size();
            mFinalAmount = (mPrice * mQuantity) + (mExtraQuantity * mExtraItemPrice) + extra;
        }

        //  mSellerAmount = mFinalAmount + mCouponValue;

        mSellerAmount=(mSellerPrice*mQuantity)+(mExtraQuantity*mExtraItemPrice);

        Log.d("check", "confirm seller: "+mPrice+" "+mQuantity+" "+mSellerPrice+" "+mExtraQuantity+" "+mFinalAmount+" "+mSellerAmount);


        Log.d("check", "confirm customer: "+mPrice+" "+mQuantity+" "+mExtraItemPrice+" "+mExtraQuantity+" "+arrayList.size());


        int available = arrayList.get(i).foodQty - mQuantity;
        String scheduleId = arrayList.get(i).scheduleId;

        Log.d("check", "confirm: " + arrayList.size() + " " + i + " " + orderNo + " " + mId);
        final OrderDetails orderDetails1;
        final OrderMenuDetails menuDetails = new OrderMenuDetails(mId, mName, mType, mPrice, mImageUrl, mProviderId, mProviderName, mProviderAddress);

        if (arrayList.size() > 1)
            orderDetails1 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, i + 1, mName, mQuantity, mExtraItem, mExtraQuantity, note, mFinalAmount,mSellerAmount, mCouponValue, "bKash", "Pending", mDate, mLastTime, mTime, "Pending", time);
        else
            orderDetails1 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId,0, mName, mQuantity, mExtraItem, mExtraQuantity, note, mFinalAmount,mSellerAmount, mCouponValue, "bKash", "Pending", mDate, mLastTime, mTime, "Pending", time);



        OrderDetails orderDetails2;

        if (arrayList.size() > 1)
            orderDetails2 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, i + 1, mName, mQuantity, mExtraItem, mExtraQuantity, note, mSellerAmount, "bKash", mDate, mLastTime, mTime, "Pending", time);
        else
            orderDetails2 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, 0, mName, mQuantity, mExtraItem, mExtraQuantity, note, mSellerAmount, "bKash", mDate, mLastTime, mTime, "Pending", time);

        updateAdmin(i, mId, mDate, orderNo, orderDetails1, menuDetails);
        updateCustomer(i, mId, mDate, orderNo, orderDetails1, menuDetails, date, key);
        updateProvider(i, mId, mDate, orderNo, orderDetails2, menuDetails, date, key, available, scheduleId);

        if (mCoupon != null) {
            FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("coupon").child(mCoupon).child("usedBy")
                    .child(StaticConfig.UID).setValue(StaticConfig.UID);
        }

        databaseReference.child("schedule").child(scheduleId).child("foodQty").setValue(available);

/*
        final int[] sellAmountProvider= {0};
        databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellAmount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    sellAmountProvider[0] = Integer.parseInt(dataSnapshot.getValue().toString());
                    sellAmountProvider[0]+=mSellerAmount;
                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellAmount").setValue(sellAmountProvider[0]);

                }
                else
                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child("sellAmount").setValue(1);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        */
    }

    public void updateAdmin(final int i, final String mId, final String mDate, final String orderNo, final OrderDetails orderDetails1, final OrderMenuDetails menuDetails) {
        databaseReference.child("admin/orders/" + mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("check", "confirm:loop admin " + arrayList.size() + " " + i + " " + orderNo + " " + mId);

                if (dataSnapshot.getValue() != null) {
                    databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);

                    databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).child("trxId").setValue(etTrxId.getText().toString());

                }
                if (dataSnapshot.getValue() == null) {
                    databaseReference.child("admin/orders/" + mDate).child(mId).child("menuDetails").setValue(menuDetails);
                    databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);

                    databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).child("trxId").setValue(etTrxId.getText().toString());

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateCustomer(final int i, final String mId, final String mDate, final String orderNo, final OrderDetails orderDetails1, final OrderMenuDetails menuDetails, final String date, final String key) {
        databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Log.d("check", "confirm:loop customer " + arrayList.size() + " " + i + " " + orderNo + " " + mId);

                if (dataSnapshot.getValue() != null) {
                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);
                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("orders").child(orderNo).child("trxId").setValue(etTrxId.getText().toString());

                }
                if (dataSnapshot.getValue() == null) {
                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("menuDetails").setValue(menuDetails);
                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("orders").child(orderNo).child("trxId").setValue(etTrxId.getText().toString());

                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);
                }

                Toast.makeText(ConfirmPayment.this, "Your order is placed and will arrive very soon.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateProvider(final int i, final String mId, final String mDate, final String orderNo, final OrderDetails orderDetails2, final OrderMenuDetails menuDetails, final String date, final String key, int available, String scheduleId) {
        databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String key = databaseReference.push().getKey();

                Log.d("check", "confirm:loop provider " + arrayList.size() + " " + i + " " + orderNo + " " + mId);

                if (dataSnapshot.getValue() != null) {
                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails2);

                }
                if (dataSnapshot.getValue() == null) {
                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("menuDetails").setValue(menuDetails);

                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
