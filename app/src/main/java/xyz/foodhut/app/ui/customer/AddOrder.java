package xyz.foodhut.app.ui.customer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderMenuDetails;
import xyz.foodhut.app.ui.provider.HomeProvider;

import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class AddOrder extends AppCompatActivity {

    TextView quantity, total, extraItem, extraItemPrice, txtExtraPrice, extraItemQty, extraX, name, finalAmount, time, address, itemPrice, coupon, dc;
    EditText etNote;
    TextView confirm;
    RadioButton cod, bKash, card, corporate;
    LinearLayout llExtra;

    String cName, cAddress, cPhone, mCoupon, mScheduleId, mPayment;
    String mName, mExtraItem, mNote, mTime, mAddress, mProviderAddress, mProviderPhone,
            mId, mType, mImageUrl, mDate, mLastTime, mProviderId, mProviderName;
    int mQuantity = 1, mExtraQuantity = 0, mSubTotal = 0, mTotal = 0, mAvailable = 0, mFinalAmount = 0, mSellerPrice = 0, mSellerAmount = 0, mCouponValue = 0, mPrice = 0, mExtraItemPrice = 0, mExtraSubTotal = 0, deliveryCharge = 0;
    Bundle extras;

    private long promoPrice;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        //   getSupportActionBar().setTitle("Add Order");

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        quantity = findViewById(R.id.aoQuantity);
        // total=findViewById(R.id.aoSubTotal);
        extraItem = findViewById(R.id.aoExtraItem);
        itemPrice = findViewById(R.id.aoPrice);
        extraItemPrice = findViewById(R.id.aoExtraPrice);
        txtExtraPrice = findViewById(R.id.txtExtraPrice);
        extraX = findViewById(R.id.X2);
        extraItemQty = findViewById(R.id.aoExtraQuantity);
        name = findViewById(R.id.aoName);
        total = findViewById(R.id.aoSubTotalPrice);
        finalAmount = findViewById(R.id.aoTotalPrice);
        dc = findViewById(R.id.aoDeliveryFee);
        coupon = findViewById(R.id.aoCoupon);
        address = findViewById(R.id.aoAddress);

        time = findViewById(R.id.aoBtnTime);
        confirm = findViewById(R.id.aoConfirm);
        etNote = findViewById(R.id.aoNote);
        cod = findViewById(R.id.aoRdoCOD);
        bKash = findViewById(R.id.aoRdoBKash);
        corporate = findViewById(R.id.aoRdoCorporate);

        llExtra = findViewById(R.id.llExtra);
        // tk2=findViewById(R.id.tk2);

        address.setText(StaticConfig.ADDRESS);
        customerInfo();

        getDC();

        cod.setChecked(true);

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString("menuId");
            mScheduleId = extras.getString("scheduleId");
            mName = extras.getString("name");
            mType = extras.getString("type");
            mPrice = extras.getInt("price");
            mSellerPrice = extras.getInt("sellerPrice");
            deliveryCharge = extras.getInt("dc");
            mAvailable = extras.getInt("available");
            //mDesc = extras.getString("desc");
            mExtraItem = extras.getString("extraItem");
            mExtraItemPrice = extras.getInt("extraItemPrice");
            Log.d("check", "extraItem: " + mExtraItem);

            mImageUrl = extras.getString("imageUrl");
            mDate = extras.getString("date");
            mLastTime = extras.getString("lastTime");
            mProviderId = extras.getString("providerId");
            mProviderName = extras.getString("providerName");
            mProviderAddress = extras.getString("providerAddress");
            mProviderPhone = extras.getString("providerPhone");

            // Log.d("check", "onCreate: "+byteArray);

            mSubTotal = mPrice;
            mTotal = mPrice;
            mFinalAmount = mPrice + deliveryCharge;
            String ei = "Extra " + mExtraItem;
            String price = String.valueOf(mPrice);



            dc.setText(String.valueOf(deliveryCharge));
            name.setText(mName);
            total.setText(price);
            itemPrice.setText(price);
            extraItemPrice.setText(String.valueOf(mExtraItemPrice));
            finalAmount.setText(String.valueOf(mFinalAmount));


            if (!mExtraItem.equals("")) {
                mExtraItemPrice = Integer.parseInt(extras.getString("extraItemPrice"));
                extraItem.setText(ei);
            } else {
                extraItem.setVisibility(View.GONE);
                extraItemPrice.setVisibility(View.GONE);
                llExtra.setVisibility(View.GONE);
                txtExtraPrice.setVisibility(View.GONE);
                extraX.setVisibility(View.GONE);
            }

            /*
            if (byteArray != null) {
                //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                image.setImageBitmap(bitmap);
                // btnDelete.setVisibility(View.VISIBLE);
            }
            */
        }


        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] timeSelected = new String[1];
                final String[] timeNow = new String[1];
                Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddOrder.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        if (minute < 10) {
                            timeSelected[0] = formatTime(String.valueOf(selectedHour) + ":" + "0" + String.valueOf(selectedMinute));
                            //mTime = formatTime(time);
                            //mTime=time;
                        } else {
                            timeSelected[0] = formatTime(String.valueOf(selectedHour) + ":" + String.valueOf(selectedMinute));
                            // mTime = formatTime(time);
                            //mTime=time;
                        }


                        Calendar calendar = Calendar.getInstance();
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        final int minute = calendar.get(Calendar.MINUTE);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        calendar.set(year, month, day);

                        if (minute < 10) {
                            timeNow[0] = formatTime(String.valueOf(hour) + ":" + "0" + String.valueOf(minute));
                            //mTime = formatTime(time);
                            //mTime=time;
                        } else {
                            timeNow[0] = formatTime(String.valueOf(hour) + ":" + String.valueOf(minute));
                            // mTime = formatTime(time);
                            //mTime=time;
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        final String date = sdf.format(calendar.getTime());

                        SimpleDateFormat stf = new SimpleDateFormat("hh:mm a");
                        Date time1 = null;
                        Date time2 = null;
                        Date today3 = null;
                        Date schedule4 = null;

                        try {
                            time2 = stf.parse(timeSelected[0]);
                            time1 = stf.parse(timeNow[0]);

                            today3 = sdf.parse(date);
                            schedule4 = sdf.parse(mDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Log.d("check", "order: " + date + " " + time1 + " " + time2 + " " + today3 + " " + schedule4);

                        if (today3.equals(schedule4)) {
                            if (time2.before(time1)) {
                                Toast.makeText(AddOrder.this, "Select Valid Time", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                mTime=timeSelected[0];
                                time.setText(mTime);
                            }
                        }
                        else
                        {
                            mTime=timeSelected[0];
                            time.setText(mTime);
                        }


                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });


        cod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cod.isChecked()) {
                    bKash.setChecked(false);
                    corporate.setChecked(false);
                }
            }
        });

        bKash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (bKash.isChecked()) {
                    cod.setChecked(false);
                    corporate.setChecked(false);
                }
            }
        });
        corporate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (corporate.isChecked()) {
                    cod.setChecked(false);
                    bKash.setChecked(false);
                }
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

    public void customerInfo() {
        FirebaseDatabase.getInstance().getReference().child("customers").child(StaticConfig.UID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    cName = (String) hashUser.get("name");
                    cAddress = (String) hashUser.get("address");
                    cPhone = (String) hashUser.get("phone");

                    address.setText(cAddress);

                    Log.d("check", "name in addorder: " + cName);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void goBack(View view) {
        //  startActivity(new Intent(this,HomeCustomer.class));
        finish();
        // finishAffinity();
    }

    public void getDC() {
        FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("dc").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    deliveryCharge = Integer.parseInt(snapshot.getValue().toString());
                    mFinalAmount = mFinalAmount + deliveryCharge;

                    dc.setText(String.valueOf(deliveryCharge));
                    finalAmount.setText(String.valueOf(mFinalAmount));
                    Log.d("check", "dc in addorder: " + deliveryCharge);
                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void addCoupon(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_add_coupon, null);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(inflate);

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Applying Coupon..");

        final EditText etCoupon = inflate.findViewById(R.id.etCoupon);

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
                Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCoupon = etCoupon.getText().toString().trim();
                        if (!mCoupon.isEmpty()) {

                            pDialog.show();

                            FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("coupon").child(mCoupon).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {

                                        HashMap hashUser = (HashMap) snapshot.getValue();
                                        final long value = (long) hashUser.get("value");
                                        final String type = (String) hashUser.get("type");


                                        if (value > 0) {
                                            if (type.equals("flat")) {
                                                FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("coupon").child(mCoupon).child("usedBy")
                                                        .child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot snapshot) {

                                                        if (snapshot.getValue() == null) {

                                                            pDialog.cancel();

                                                            mFinalAmount -= value;
                                                            String couponValue = "৳ -" + value;
                                                            coupon.setText(couponValue);
                                                            coupon.setEnabled(false);
                                                            mCouponValue = (int) value;

                                                            finalAmount.setText(String.valueOf(mFinalAmount));

                                                            dialog.dismiss();
                                                            Toast.makeText(AddOrder.this, "Coupon is applied!", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            pDialog.cancel();
                                                            dialog.dismiss();
                                                            Toast.makeText(AddOrder.this, "Sorry coupon is not valid!", Toast.LENGTH_SHORT).show();
                                                        }

                                                        pDialog.cancel();
                                                    }

                                                    public void onCancelled(DatabaseError arg0) {
                                                        pDialog.cancel();
                                                    }
                                                });
                                            }


                                            if (type.equals("promo")) {
                                                FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("coupon").child(mCoupon).child("usedBy")
                                                        .child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot snapshot) {

                                                        if (snapshot.getValue() == null) {

                                                            pDialog.cancel();

                                                            if (mQuantity==1){
                                                                mFinalAmount= (int) (value+(mExtraItemPrice*mExtraQuantity));
                                                            }
                                                            else {
                                                                mFinalAmount= (int) (((mQuantity-1)*mPrice)+value+(mExtraItemPrice*mExtraQuantity));
                                                            }
                                                            String couponValue = mCoupon;
                                                            coupon.setText(couponValue);
                                                            coupon.setEnabled(false);
                                                            mCouponValue = (int) value;

                                                            finalAmount.setText(String.valueOf(mFinalAmount));

                                                            dialog.dismiss();
                                                            Toast.makeText(AddOrder.this, "Coupon is applied!", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            pDialog.cancel();
                                                            dialog.dismiss();
                                                            Toast.makeText(AddOrder.this, "Sorry coupon is not valid!", Toast.LENGTH_SHORT).show();
                                                        }

                                                        pDialog.cancel();
                                                    }

                                                    public void onCancelled(DatabaseError arg0) {
                                                        pDialog.cancel();
                                                    }
                                                });
                                            }
                                        }

                                    } else {
                                        pDialog.cancel();
                                        dialog.dismiss();
                                        Toast.makeText(AddOrder.this, "Sorry coupon is not valid!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                public void onCancelled(DatabaseError arg0) {
                                    pDialog.cancel();
                                }
                            });

                            //    databaseReference.child("customers/" + userID).child("about").child("name").setValue(etName.getText().toString());

                        } else {
                            Toast.makeText(AddOrder.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void quantityOp(View view) {

        mQuantity = Integer.parseInt(quantity.getText().toString().trim());
        mExtraQuantity = Integer.parseInt(extraItemQty.getText().toString());

        if (view.getId() == R.id.aoQtyMinus) {

            if (mQuantity > 1) {

                mSubTotal = mSubTotal - mPrice;
                mTotal = mTotal - mPrice;

                mQuantity--;
                String qty = String.valueOf(mQuantity);
                String sub = String.valueOf(mSubTotal);
                String totals = String.valueOf(mTotal);
              //  itemPrice.setText(sub);
                total.setText(totals);
                quantity.setText(qty);

                mFinalAmount -= mPrice;
                finalAmount.setText(String.valueOf(mFinalAmount));

            }

        }
        if (view.getId() == R.id.aoQtyPlus) {

            if (mQuantity < mAvailable) {

                mSubTotal = mSubTotal + mPrice;
                mTotal = mTotal + mPrice;

                mQuantity++;
                String qty = String.valueOf(mQuantity);
                quantity.setText(qty);
                String sub = String.valueOf(mSubTotal);
                String totals = String.valueOf(mTotal);
                // itemPrice.setText(sub);
                total.setText(totals);

                mFinalAmount += mPrice;
                finalAmount.setText(String.valueOf(mFinalAmount));

            }
            else
                Toast.makeText(this, "No more item", Toast.LENGTH_SHORT).show();
        }
        if (view.getId() == R.id.aoExtraQtyMinus) {

            if (mExtraQuantity > 0) {

                mExtraSubTotal = mExtraSubTotal - mExtraItemPrice;
                mTotal = mTotal - mExtraItemPrice;

                mExtraQuantity--;
                String qty = String.valueOf(mExtraQuantity);
                extraItemQty.setText(qty);

                String sub = String.valueOf(mExtraSubTotal);
                String totals = String.valueOf(mTotal);
             //   extraItemPrice.setText(sub);
                total.setText(totals);

                mFinalAmount -= mExtraItemPrice;
                finalAmount.setText(String.valueOf(mFinalAmount));
            }
        }
        if (view.getId() == R.id.aoExtraQtyPlus) {
            mExtraSubTotal = mExtraSubTotal + mExtraItemPrice;
            mTotal = mTotal + mExtraItemPrice;

            mExtraQuantity++;
            String qty = String.valueOf(mExtraQuantity);
            extraItemQty.setText(qty);

            String sub = String.valueOf(mExtraSubTotal);
            String totals = String.valueOf(mTotal);
         //   extraItemPrice.setText(sub);
            total.setText(totals);

            mFinalAmount += mExtraItemPrice;
            finalAmount.setText(String.valueOf(mFinalAmount));
        }
    }

    public void changeAddress(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View inflate = layoutInflater.inflate(R.layout.layout_update_address, null);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(inflate);


        final EditText etApartment = inflate.findViewById(R.id.etApartment);
        final EditText etHouse = inflate.findViewById(R.id.etHouse);
        final EditText etStreet = inflate.findViewById(R.id.etStreet);
        final EditText etArea = inflate.findViewById(R.id.etArea);

        final String[] mApartment = new String[1];
        final String[] mHouse = new String[1];
        final String[] mStreet = new String[1];
        final String[] mArea = new String[1];

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
                Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mApartment[0] = etApartment.getText().toString().trim();
                        mHouse[0] = etHouse.getText().toString().trim();
                        mStreet[0] = etStreet.getText().toString().trim();
                        mArea[0] = etArea.getText().toString().trim();

                        if (!mApartment[0].isEmpty() && !mHouse[0].isEmpty() && !mStreet[0].isEmpty() && !mArea[0].isEmpty()) {
                            String newAddress = mApartment[0] + "/" + mHouse[0] + "," + mStreet[0] + "," + mArea[0];
                            cAddress = newAddress;


                            address.setText(newAddress);

                            dialog.cancel();
                        } else {
                            Toast.makeText(AddOrder.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void confirm(View view) {

        final String[] code = new String[1];
        final String[] corporateCode = new String[1];
        final long[] corporateDue = new long[1];

        corporateCode[0]="code2019";

        if (cod.isChecked()) {
            mPayment = "COD";
            completeOrder();
        }
        if (bKash.isChecked()) {
            mPayment = "bKash";
            completeOrder();
        }
        if (corporate.isChecked()) {
            mPayment = "Corporate Due";

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View inflate = layoutInflater.inflate(R.layout.layout_add_corporate_code, null);

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setView(inflate);

            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage("Applying Code..");

            final EditText etCode = inflate.findViewById(R.id.etCode);

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
                    Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            code[0] = etCode.getText().toString().trim();
                            if (!code[0].isEmpty()) {

                                pDialog.show();

                                FirebaseDatabase.getInstance().getReference("customers/" + StaticConfig.UID + "/about")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                //fetch files from firebase database and push in arraylist

                                                if (dataSnapshot.getValue()!=null){

                                                    HashMap hashUser = (HashMap) dataSnapshot.getValue();

                                                    try {
                                                        corporateCode[0] = (String) hashUser.get("corporateCode");
                                                        corporateDue[0] = (long) hashUser.get("corporateDue");
                                                    }catch (NullPointerException e){
                                                        e.printStackTrace();
                                                    }


                                                    if (code[0].equals(corporateCode[0])){
                                                        Toast.makeText(AddOrder.this, "Code Applied!", Toast.LENGTH_SHORT).show();

                                                        int due= (int) (corporateDue[0]+mFinalAmount);

                                                        FirebaseDatabase.getInstance().getReference("customers/" + StaticConfig.UID + "/about/corporateDue").setValue(due);

                                                        dialog.cancel();


                                                        completeOrder();

                                                    }
                                                    else {
                                                        Toast.makeText(AddOrder.this, "Invalid Code!", Toast.LENGTH_SHORT).show();
                                                        dialog.cancel();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                //    databaseReference.child("customers/" + userID).child("about").child("name").setValue(etName.getText().toString());

                            } else {
                                Toast.makeText(AddOrder.this, "Required fields are missing", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            alertDialog.show();
        }



    }


    public void completeOrder() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait..");

        if (isConnected()) {


            dialog.show();


            //get the signed in user
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                userID = user.getUid();
            }
            int random1 = (new Random().nextInt(999));
            int random2 = (new Random().nextInt(9));

            int random = random1 + random2;

            // Get the calander
            Calendar c = Calendar.getInstance();

            // From calander get the year, month, day, hour, minute
            int year = c.get(Calendar.YEAR);
            //   int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);
            String month = new SimpleDateFormat("MMM", Locale.US).format(c.getTime());
            String time = day + " " + month + " " + formatTime(hour + ":" + minute);
            //String time = formatTime(hour + ":" + minute);

            // final String orderNo = year + "" + (month + 1) + "" + day +""+hour+ "" + minute + "" + second;
            final String orderNo = String.valueOf(c.getTimeInMillis());

            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
            final String date = dateFormat.format(c.getTime());

            if (mTime != null) {



                if (cod.isChecked() || corporate.isChecked()) {

                    String note = etNote.getText().toString().trim();

                    final String key = databaseReference.push().getKey();

                    mSellerAmount = (mSellerPrice * mQuantity) + (mExtraItemPrice * mExtraQuantity);

                    Log.d("check", "confirm: " + mPrice + " " + mQuantity + " " + mSellerPrice + " " + mExtraQuantity + " " + mFinalAmount + " " + mSellerAmount);


                    final OrderMenuDetails menuDetails = new OrderMenuDetails(mId, mName, mType, mPrice, mImageUrl, mProviderId, mProviderName, mProviderAddress);

                    //  final OrderDetails orderDetails1 = new OrderDetails(orderNo, cName, StaticConfig.UID, cAddress, cPhone, mQuantity, mExtraItem, mExtraQuantity, note, mFinalAmount, mCouponValue, mPayment, "Due", mDate, mLastTime, mTime, "Pending", time);

                    final OrderDetails orderDetails1;
                    if (corporate.isChecked())
                        orderDetails1 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, mName, mQuantity, mExtraItem, mExtraQuantity, note, mFinalAmount,mSellerAmount, mCouponValue, mPayment, "Corporate Due", mDate, mLastTime, mTime, "Pending", time);
                    else
                        orderDetails1 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, mName, mQuantity, mExtraItem, mExtraQuantity, note, mFinalAmount,mSellerAmount, mCouponValue, mPayment, "Due", mDate, mLastTime, mTime, "Pending", time);


                    StaticConfig.menuDetails = menuDetails;
                    StaticConfig.orderDetails1 = orderDetails1;


                    final OrderDetails orderDetails2 = new OrderDetails(orderNo, mId, cName, userID, cAddress, cPhone, mProviderName, mProviderId, mName, mQuantity, mExtraItem, mExtraQuantity, note, mSellerAmount, mPayment, mDate, mLastTime, mTime, "Pending", time);

                    StaticConfig.orderDetails2 = orderDetails2;

                    databaseReference.child("admin/orders/" + mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {
                                databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);

                            }
                            if (dataSnapshot.getValue() == null) {
                                databaseReference.child("admin/orders/" + mDate).child(mId).child("menuDetails").setValue(menuDetails);
                                databaseReference.child("admin/orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails1);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


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
                            Toast.makeText(AddOrder.this, "Your order is placed and will arrive very soon.", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                /*
                final int[] sellCountAdmin = {0};
                final int[] sellAmountAdmin = {0};

                databaseReference.child("admin/orders/" + mDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            sellCountAdmin[0] = Integer.parseInt((String) hashUser.get("sellCount"));
                            sellAmountAdmin[0] = Integer.parseInt((String) hashUser.get("sellAmount"));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                sellCountAdmin[0]+=1;
                sellAmountAdmin[0]+=mFinalAmount;

                databaseReference.child("admin/orders/" + mDate).child("sellCount").setValue(sellCountAdmin[0]);
                databaseReference.child("admin/orders/" + mDate).child("sellAmount").setValue(sellAmountAdmin[0]);

                final int[] sellCountCustomer = {0};
                final int[] sellAmountCustomer= {0};

                databaseReference.child("customers/" + userID + "/orders").child(mDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                            sellCountCustomer[0] = Integer.parseInt((String) hashUser.get("sellCount"));
                            sellAmountCustomer[0] = Integer.parseInt((String) hashUser.get("sellAmount"));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                sellCountCustomer[0]+=1;
                sellAmountCustomer[0]+=mFinalAmount;

                databaseReference.child("customers/" + userID + "/orders").child(mDate).child("sellCount").setValue(sellCountCustomer[0]);
                databaseReference.child("customers/" + userID + "/orders").child(mDate).child("sellAmount").setValue(sellAmountCustomer[0]);

*/

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
                                .child(StaticConfig.UID).setValue(StaticConfig.UID);
                    }


                    //   Toast.makeText(AddOrder.this, "Order Confirmed", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    //  startActivity(new Intent(this, HomeCustomer.class));

                    finish();

                    StaticConfig.QTY = 0;
                    StaticConfig.SUBTOTAL = 0;
                    StaticConfig.ORDERITEMLIST.clear();
                    StaticConfig.ITEMQTYLIST.clear();
                    StaticConfig.INDEXLIST.clear();
                    StaticConfig.ISORDERED = 1;


                } else {
                    Intent intent = new Intent(this, ConfirmPayment.class);

                    Log.d("check", "confirm: " + mFinalAmount);

                    mSellerAmount = (mSellerPrice * mQuantity) + (mExtraItemPrice * mExtraQuantity);


                    intent.putExtra("date", mDate);
                    intent.putExtra("menuId", mId);
                    intent.putExtra("menuName", mName);
                    intent.putExtra("orderNo", orderNo);
                    intent.putExtra("scheduleId", mScheduleId);
                    intent.putExtra("providerId", mProviderId);
                    intent.putExtra("providerName", mProviderName);
                    intent.putExtra("providerAddress", mProviderAddress);
                    intent.putExtra("coupon", mCoupon);
                    intent.putExtra("couponValue", mCouponValue);
                    intent.putExtra("amount", mFinalAmount);
                    intent.putExtra("sellerAmount", mSellerAmount);
                    intent.putExtra("type", "single");
                    intent.putExtra("extraItem", mExtraItem);
                    intent.putExtra("lastTime", mLastTime);

                    intent.putExtra("cName", cName);
                    intent.putExtra("cPhone", cPhone);
                    intent.putExtra("cAddress", cAddress);
                    intent.putExtra("time", mTime);
                    intent.putExtra("note", etNote.getText().toString().trim());

                    intent.putExtra("sellerPrice", mSellerPrice);
                    intent.putExtra("extraItemPrice", mExtraItemPrice);

                    intent.putExtra("extraQty", mExtraQuantity);
                    intent.putExtra("qty", mQuantity);
                    intent.putExtra("available", mAvailable);

                    startActivity(intent);
                  //  finish();
                }

            } else {
                Toast.makeText(this, "Please Select Time", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
        else
            Toast.makeText(this, "Sorry, you don't have an active internet connection", Toast.LENGTH_SHORT).show();

        dialog.cancel();
    }


}
