package xyz.foodhut.app.ui.customer;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.OrderDetails;
import xyz.foodhut.app.model.OrderMenuDetails;

import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class AddOrder extends AppCompatActivity {

    TextView quantity, subTotal, extraItem, extraItemPrice, extraItemQty, name, total,time, address, itemPrice;
    EditText etNote;
    TextView confirm;
    RadioButton cod;
    LinearLayout llExtra;

    String cName, cAddress, cPhone;
    String mName, mExtraItem, mNote, mTime, mAddress, mProviderAddress, mProviderPhone,
            mId, mType, mImageUrl, mDate, mProviderId, mProviderName;
    int mQuantity = 1, mExtraQuantity = 0, mSubTotal = 0, mTotal = 0, mPrice = 0, mExtraItemPrice = 0, mExtraSubTotal = 0;
    Bundle extras;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        getSupportActionBar().setTitle("Add Order");

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        quantity = findViewById(R.id.aoQuantity);
        // subTotal=findViewById(R.id.aoSubTotal);
        extraItem = findViewById(R.id.aoExtraItem);
        itemPrice = findViewById(R.id.aoPrice);
        extraItemPrice = findViewById(R.id.aoExtraPrice);
        extraItemQty = findViewById(R.id.aoExtraQuantity);
        name = findViewById(R.id.aoName);
        total = findViewById(R.id.aoTotalPrice);
        address = findViewById(R.id.aoAddress);

        time = findViewById(R.id.aoBtnTime);
        confirm = findViewById(R.id.aoConfirm);
        etNote = findViewById(R.id.aoNote);
        cod = findViewById(R.id.aoRdoCOD);

        llExtra = findViewById(R.id.llExtra);
        // tk2=findViewById(R.id.tk2);

        address.setText(StaticConfig.ADDRESS);
        customerInfo();

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString("menuId");
            mName = extras.getString("name");
            mType = extras.getString("type");
            mPrice = Integer.parseInt(extras.getString("price"));
            //mDesc = extras.getString("desc");
            mExtraItem = extras.getString("extraItem");

            Log.d("check", "extraItem: " + mExtraItem);

            mImageUrl = extras.getString("imageUrl");
            mDate = extras.getString("date");
            mProviderId = extras.getString("providerId");
            mProviderName = extras.getString("providerName");
            mProviderAddress = extras.getString("providerAddress");
            mProviderPhone = extras.getString("providerPhone");

            // Log.d("check", "onCreate: "+byteArray);


            cod.setChecked(true);

            mSubTotal = mPrice;
            mTotal = mPrice;
            String ei = "Extra " + mExtraItem;
            String price = String.valueOf(mPrice);

            name.setText(mName);
            total.setText(price);
            itemPrice.setText(price);


            if (mExtraItem != null) {
                mExtraItemPrice = Integer.parseInt(extras.getString("extraItemPrice"));
                extraItem.setText(ei);
            } else {
                extraItem.setVisibility(View.INVISIBLE);
                extraItemPrice.setVisibility(View.INVISIBLE);
                llExtra.setVisibility(View.INVISIBLE);
                // tk2.setVisibility(View.INVISIBLE);
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
                Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddOrder.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        if (minute < 10) {
                            mTime  = String.valueOf(selectedHour) + ":" + "0" + String.valueOf(selectedMinute);
                            //mTime = formatTime(time);
                            //mTime=time;
                        } else {
                            mTime = String.valueOf(selectedHour) + ":" + String.valueOf(selectedMinute);
                           // mTime = formatTime(time);
                            //mTime=time;
                        }
                        time.setText(formatTime(mTime));
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

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

                    Log.d("check", "name in addorder: " + cName);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
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
                itemPrice.setText(sub);
                total.setText(totals);
                quantity.setText(qty);


            }

        }
        if (view.getId() == R.id.aoQtyPlus) {

            mSubTotal = mSubTotal + mPrice;
            mTotal = mTotal + mPrice;

            mQuantity++;
            String qty = String.valueOf(mQuantity);
            quantity.setText(qty);
            String sub = String.valueOf(mSubTotal);
            String totals = String.valueOf(mTotal);
            itemPrice.setText(sub);
            total.setText(totals);

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
                extraItemPrice.setText(sub);
                total.setText(totals);


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
            extraItemPrice.setText(sub);
            total.setText(totals);
        }
    }

    public void confirm(View view) {
        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }
        int random = (new Random().nextInt(1000));

        // Get the calander
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour=c.get(Calendar.HOUR_OF_DAY);
        int minute=c.get(Calendar.MINUTE);
        int second=c.get(Calendar.SECOND);
        String time=formatTime(hour+":"+minute);

        final String orderNo = year + "" + (month + 1) + "" + day + "" + random;

        DateFormat dateFormat= SimpleDateFormat.getDateTimeInstance();
        final String date=dateFormat.format(c.getTime());
        final Notification notification=new Notification();
        notification.orderId=orderNo;
        notification.status="Processing";
        notification.time=date;


        final String key = databaseReference.push().getKey();

        final OrderMenuDetails menuDetails = new OrderMenuDetails(mId, mName, mType, mPrice, mImageUrl, mProviderId, mProviderName, mProviderAddress);
        final OrderDetails orderDetails = new OrderDetails(orderNo, cName, cAddress, cPhone, mQuantity, mExtraItem, mExtraQuantity, mTotal, "COD",mDate, mTime, "Processing",time);

        databaseReference.child("orders/" + mDate).child(mId).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.getValue() != null) {
                    databaseReference.child("orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails);
                    Toast.makeText(AddOrder.this, "Order Confirmed", Toast.LENGTH_SHORT).show();
                }
                if (dataSnapshot.getValue() == null) {
                    databaseReference.child("orders/" + mDate).child(mId).child("menuDetails").setValue(menuDetails);
                    databaseReference.child("orders/" + mDate).child(mId).child("orders").child(orderNo).setValue(orderDetails);

                    Toast.makeText(AddOrder.this, "Order Confirmed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(orderNo).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Notification notification2=new Notification();
                notification2.orderId=orderNo;
                notification2.status="placed";
                notification2.time=date;

                if (dataSnapshot.getValue() != null) {
                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(orderNo).child("orderDetails").setValue(orderDetails);
                    databaseReference.child("providers/" + userID + "/notifications").child(key).setValue(notification2);

                }
                if (dataSnapshot.getValue() == null) {
                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(orderNo).child("menuDetails").setValue(menuDetails);

                    databaseReference.child("providers/" + mProviderId + "/orders").child(mDate).child(orderNo).child("orderDetails").setValue(orderDetails);
                    databaseReference.child("providers/" + userID + "/notifications").child(key).setValue(notification2);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("customers/" + userID + "/orders").child(mDate).child(orderNo).child("menuDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(orderNo).child("orderDetails").setValue(orderDetails);

                   String key= databaseReference.push().getKey();
                    databaseReference.child("customers/" + userID + "/notifications").child(key).setValue(notification);

                }
                if (dataSnapshot.getValue() == null) {
                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(orderNo).child("menuDetails").setValue(menuDetails);

                    databaseReference.child("customers/" + userID + "/orders").child(mDate).child(orderNo).child("orderDetails").setValue(orderDetails);
                    databaseReference.child("customers/" + userID + "/notifications").child(key).setValue(notification);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        startActivity(new Intent(this, HomeCustomer.class));
        finish();

    }
}
