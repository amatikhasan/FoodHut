package xyz.foodhut.app.ui.customer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
//import com.github.piasy.biv.view.BigImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;

public class MenuDetails extends AppCompatActivity {

    TextView name, type, price, desc, date, providerName, providerAddress, ratingCount,order,pkgSize,available,category,
            lastTime;
    ImageView image;
    //Button order;
    RatingBar rating;
    String mName, mType, mPrice,mSellerPrice, mDesc, mDate, mImageUrl, mId, mExtraItem, mExtraItemPrice,
            mProviderId, mProviderName, mProviderAddress,mProviderPhone,mScheduleId, mPkgSize,mCategory,mLastTime;
    int id, mRatingCount,deliveryCharge=0, mAvailable;
    float mRating;
    Bundle extras;
   // BigImageView biv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_details);

        name = findViewById(R.id.osName);
        type = findViewById(R.id.osType);
        price = findViewById(R.id.mdPrice);
        desc = findViewById(R.id.mdDetails);
        date = findViewById(R.id.osDate);
        providerName = findViewById(R.id.osProvider);
        providerAddress = findViewById(R.id.osProviderLocation);
        //ratingCount = findViewById(R.id.mdName);
        //rating
        order=findViewById(R.id.mdOrder);

        pkgSize = findViewById(R.id.mPkgSize);
        available = findViewById(R.id.mAvailable);
        category = findViewById(R.id.mCategory);
        lastTime = findViewById(R.id.osLastTime);

        image=findViewById(R.id.mdImage);


        getDC();

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString("menuId");
            mScheduleId = extras.getString("scheduleId");
            mName = extras.getString("name");
            mType = extras.getString("type");
            mPrice = extras.getString("price");
            mSellerPrice = extras.getString("sellerPrice");
            mDesc = extras.getString("desc");
            mExtraItem = extras.getString("extraItem");
            mExtraItemPrice = extras.getString("extraItemPrice");
            mImageUrl = extras.getString("imageUrl");
            mDate=extras.getString("date");
            mProviderId=extras.getString("providerId");
            mProviderName=extras.getString("providerName");
            mProviderAddress=extras.getString("providerAddress");
            mProviderPhone=extras.getString("providerPhone");

            mCategory=extras.getString("category");
            mPkgSize=extras.getString("pkgSize");
            mLastTime=extras.getString("lastTime");
            mAvailable=extras.getInt("available");
            Log.d("check", "extra MD: "+mExtraItem);


            getAvailable();
            // Log.d("check", "onCreate: "+byteArray);


         //   getSupportActionBar().setTitle("Menu Details");

      /*      SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MM-yyyy");
            Date date1= null;
            try {
                date1 = dateFormat.parse(mDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat newFormate=new SimpleDateFormat("dd MMM, yyyy");
            String date2=newFormate.format(date1);
            */

            String total="৳ "+mPrice;
            String pkg="1:"+mPkgSize;
            String time="(Order before "+mLastTime+")";
            String qty=mAvailable+" Available";

            name.setText(mName);
           // type.setText(mType);
            price.setText(total);
            desc.setText(mDesc);
            date.setText(mDate);
            providerName.setText(mProviderName);

            String parts[] = mProviderAddress.split(",");
            String newAddress = parts[1] + ", " + parts[2];

            providerAddress.setText(newAddress);

            pkgSize.setText(pkg);
            available.setText(String.valueOf(qty));
            category.setText(mCategory);
            lastTime.setText(time);

            Picasso.get().load(mImageUrl).placeholder(R.drawable.loading).into(image);
          //  Glide.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.loading).into(image);

            /*
            if (byteArray != null) {
                //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                image.setImageBitmap(bitmap);
                // btnDelete.setVisibility(View.VISIBLE);
            }
            */
        }


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           /*     AlertDialog.Builder mBuilder = new AlertDialog.Builder(MenuDetails.this);
                View mView = getLayoutInflater().inflate(R.layout.layout_image_view, null);
              //  ImageView image = mView.findViewById(R.id.pvImage);
                PhotoView photoView = mView.findViewById(R.id.pvImage);
              //  BigImageView biv=mView.findViewById(R.id.bImage);
                Glide.with(getApplicationContext()).load(mImageUrl).into(photoView);

              //  PhotoView photoView = mView.findViewById(R.id.pvImage);
             //   photoView.setImageBitmap(bitmap);
              //  mBuilder.setView(mView);

                mBuilder.setView(mView);
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
                */

                Intent intent = new Intent(MenuDetails.this, xyz.foodhut.app.ui.customer.ImageView.class);
                intent.putExtra("imageUrl", mImageUrl);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        getAvailable();
    }


    public boolean isConnected(){
        ConnectivityManager cm= (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network= null;
        if (cm != null) {
            network = cm.getActiveNetworkInfo();
        }

        return   (network!=null && network.isConnected());
    }


    public void goBack(View view){
      //  startActivity(new Intent(this,HomeCustomer.class));
        finish();
      //  finishAffinity();
    }

    public void getDC() {
        FirebaseDatabase.getInstance().getReference().child("admin/appControl").child("deliveryCharge").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    deliveryCharge=Integer.parseInt(snapshot.getValue().toString());

                    Log.d("check", "dc in addorder: " + deliveryCharge);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void getAvailable() {
        FirebaseDatabase.getInstance().getReference().child("schedule").child(mScheduleId).child("foodQty").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    mAvailable=Integer.parseInt(snapshot.getValue().toString());
                    String qty=mAvailable+" Available";
                    available.setText(String.valueOf(qty));
                    Log.d("check", "dc in addorder: " + mAvailable);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void order(View view){

        if (isConnected()) {

            if (mAvailable > 0) {

                String mTime;
                Calendar calendar = Calendar.getInstance();
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);
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

                try {
                    date2 = stf.parse(mLastTime);
                    date1 = stf.parse(mTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.d("check", "order: " + date + " " + date1 + " " + date2);

                if (date.equals(mDate)) {
                    if (date1.before(date2)) {


                        Intent intent = new Intent(this, AddOrder.class);
                        intent.putExtra("name", mName);
                        intent.putExtra("price", Integer.parseInt(mPrice));
                        intent.putExtra("sellerPrice", Integer.parseInt(mSellerPrice));
                        intent.putExtra("menuId", mId);
                        intent.putExtra("scheduleId", mScheduleId);
                        intent.putExtra("desc", mDesc);
                        intent.putExtra("extraItem", mExtraItem);
                        intent.putExtra("extraItemPrice", mExtraItemPrice);
                        intent.putExtra("type", mType);
                        intent.putExtra("imageUrl", mImageUrl);
                        intent.putExtra("lastTime", mLastTime);
                        intent.putExtra("date", mDate);
                        intent.putExtra("providerName", mProviderName);
                        intent.putExtra("providerAddress", mProviderAddress);
                        intent.putExtra("providerPhone", mProviderPhone);
                        intent.putExtra("providerId", mProviderId);
                        intent.putExtra("dc", deliveryCharge);
                        intent.putExtra("available", mAvailable);

                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Sorry, You're late to order this food", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Intent intent = new Intent(this, AddOrder.class);
                    intent.putExtra("name", mName);
                    intent.putExtra("price", Integer.parseInt(mPrice));
                    intent.putExtra("sellerPrice", Integer.parseInt(mSellerPrice));
                    intent.putExtra("menuId", mId);
                    intent.putExtra("scheduleId", mScheduleId);
                    intent.putExtra("desc", mDesc);
                    intent.putExtra("extraItem", mExtraItem);
                    intent.putExtra("extraItemPrice", mExtraItemPrice);
                    intent.putExtra("type", mType);
                    intent.putExtra("imageUrl", mImageUrl);
                    intent.putExtra("date", mDate);
                    intent.putExtra("lastTime", mLastTime);
                    intent.putExtra("providerName", mProviderName);
                    intent.putExtra("providerAddress", mProviderAddress);
                    intent.putExtra("providerPhone", mProviderPhone);
                    intent.putExtra("providerId", mProviderId);
                    intent.putExtra("dc", deliveryCharge);
                    intent.putExtra("available", mAvailable);


                    startActivity(intent);
                }

            } else
                Toast.makeText(this, "Sorry, This food is not available for now", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Sorry, You don't have an active internet connection", Toast.LENGTH_SHORT).show();

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
}
