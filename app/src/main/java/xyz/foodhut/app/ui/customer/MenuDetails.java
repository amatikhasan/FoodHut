package xyz.foodhut.app.ui.customer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import xyz.foodhut.app.R;

public class MenuDetails extends AppCompatActivity {

    TextView name, type, price, desc, date, providerName, providerAddress, ratingCount,order;
    ImageView image;
    //Button order;
    RatingBar rating;
    String mName, mType, mPrice, mDesc, mDate, mImageUrl, mId, mExtraItem, mExtraItemPrice,
            mProviderId, mProviderName, mProviderAddress,mProviderPhone;
    int id, mRatingCount;
    float mRating;
    Bundle extras;

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

        image=findViewById(R.id.mdImage);



        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString("menuId");
            mName = extras.getString("name");
            mType = extras.getString("type");
            mPrice = extras.getString("price");
            mDesc = extras.getString("desc");
            mExtraItem = extras.getString("extraItem");
            mExtraItemPrice = extras.getString("extraItemPrice");
            mImageUrl = extras.getString("imageUrl");
            mDate=extras.getString("date");
            mProviderId=extras.getString("providerId");
            mProviderName=extras.getString("providerName");
            mProviderAddress=extras.getString("providerAddress");
            mProviderPhone=extras.getString("providerPhone");
            Log.d("check", "extra MD: "+mExtraItem);

            // Log.d("check", "onCreate: "+byteArray);


            getSupportActionBar().setTitle("Menu Details");

            SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MM-yyyy");
            Date date1= null;
            try {
                date1 = dateFormat.parse(mDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat newFormate=new SimpleDateFormat("dd MMM, yyyy");
            String date2=newFormate.format(date1);

            String total="৳ "+mPrice;
            name.setText(mName);
            type.setText(mType);
            price.setText(total);
            desc.setText(mDesc);
            date.setText(date2);
            providerName.setText(mProviderName);
            providerAddress.setText(mProviderAddress);


            Picasso.get().load(mImageUrl).placeholder(R.drawable.image).into(image);
            //Glide.with(contex).load(obj.mImageUrl).into(holder.image);

            /*
            if (byteArray != null) {
                //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                image.setImageBitmap(bitmap);
                // btnDelete.setVisibility(View.VISIBLE);
            }
            */
        }

    }

    public void order(View view){

        Intent intent = new Intent(this, AddOrder.class);
        intent.putExtra("name", mName);
        intent.putExtra("price", mPrice);
        intent.putExtra("menuId", mId);
        intent.putExtra("desc", mDesc);
        intent.putExtra("extraItem",mExtraItem);
        intent.putExtra("extraItemPrice", mExtraItemPrice);
        intent.putExtra("type", mType);
        intent.putExtra("imageUrl", mImageUrl);
        intent.putExtra("date",mDate);
        intent.putExtra("providerName", mProviderName);
        intent.putExtra("providerAddress", mProviderAddress);
        intent.putExtra("providerPhone", mProviderPhone);
        intent.putExtra("providerId", mProviderId);

        startActivity(intent);

    }
}
