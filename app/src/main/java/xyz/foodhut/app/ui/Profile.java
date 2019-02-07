package xyz.foodhut.app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.MenuCustomer;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.ui.customer.Favourites;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.provider.HomeProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

public class Profile extends AppCompatActivity {

    CircleImageView image;
    TextView name;
    String mName,mImageUrl="default",mAddress,mPhone,mType,mKitchen;
    RelativeLayout rlFav;
    Bundle extras;
    private TextDrawable textDrawable;
    private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;

    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        image=findViewById(R.id.circlePhoto);
        name=findViewById(R.id.name);

        rlFav=findViewById(R.id.rlFav);

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mType = extras.getString("type");
            mName=extras.getString("name");
            mAddress=extras.getString("address");
            mImageUrl=extras.getString("avatar");
            mPhone=extras.getString("phone");

            if (mType.equals("provider")) {
                mKitchen = extras.getString("kitchen");
                rlFav.setVisibility(View.GONE);
            }

            if (!mImageUrl.equals("default")){
                Picasso.get().load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(image);

              //  Glide.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(image);
            }

            Log.d("check", "info: "+mName+" "+mImageUrl);

            //   name.setText(mName);
            //   address.setText(mAddress);
        }

        checkProfile();

      //  setTextDrawable(mName);

        if (mType.equals("customer"))
        name.setText(mName);
        else
            name.setText(mKitchen);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (StaticConfig.BITMAP!=null){
            image.setImageBitmap(StaticConfig.BITMAP);
        }
    }

    public void callUs(View View){
        Intent call=new Intent(Intent.ACTION_DIAL);
        call.setData(Uri.parse("tel:01721195353"));

        startActivity(call);

    }

    public void visitSite(View view){
        Intent visit=new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.foodhut.xyz"));
        startActivity(visit);
    }

    public void editProfile(View view){
        // Handle the camera action
        Intent intent = new Intent(this, ProfileUpdate.class);
        intent.putExtra("type", mType);
        intent.putExtra("name", mName);
        intent.putExtra("isUpdate", "true");
        intent.putExtra("address", mAddress);
        intent.putExtra("phone", mPhone);
        intent.putExtra("avatar", mImageUrl);
        if (mType.equals("provider"))
            intent.putExtra("kitchen", mKitchen);
        startActivity(intent);

    }

    public void logout(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        SharedPreferenceHelper.getInstance(this).logout();
        auth.signOut();
        finish();
        finishAffinity();
    }

    public void feedBack(View view) {
        Intent intent = new Intent(this, FeedBack.class);
        intent.putExtra("user", mType);
        startActivity(intent);
        finish();
    }

    public void aboutUs(View view) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
       // finish();
    }

    public void faq(View view) {
        Intent intent = new Intent(this, FAQ.class);
        startActivity(intent);
       // finish();
    }

    public void privacy(View view) {
        Intent intent = new Intent(this, Privacy.class);
        startActivity(intent);
        // finish();
    }

    public void myWishList(View view) {
        Intent intent = new Intent(this, Favourites.class);
        startActivity(intent);
        finish();
    }

    public void checkProfile() {
        if (mType.equals("customer")) {
            FirebaseDatabase.getInstance().getReference().child("customers/" + StaticConfig.UID + "/about")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //fetch files from firebase database and push in arraylist

                            if (dataSnapshot.getValue() != null) {

                                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                                mName = (String) hashUser.get("name");
                                mImageUrl = (String) hashUser.get("avatar");
                                mPhone = (String) hashUser.get("phone");
                                mAddress = (String) hashUser.get("address");

                                name.setText(mName);


                           //     if (!mImageUrl.equals("default")){
                            //        Glide.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(image);
                           //     }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
        if (mType.equals("provider")) {
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/about")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //fetch files from firebase database and push in arraylist

                            if (dataSnapshot.getValue() != null) {

                                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                                mName = (String) hashUser.get("name");
                                mKitchen = (String) hashUser.get("kitchen");
                                mImageUrl = (String) hashUser.get("avatar");
                                mPhone = (String) hashUser.get("phone");
                                mAddress = (String) hashUser.get("address");

                           //     if (!mImageUrl.equals("default")){
                           //         Glide.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(image);
                           //     }


                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void goBack(View view) {
        finish();

    }

    // Set reminder title view
    public void setTextDrawable(String name) {
        String letter = "N";

        if(name != null && !name.isEmpty()) {
            letter = name.substring(0, 1);
        }

        int color = mColorGenerator.getRandomColor();

        // Create a circular icon consisting of  a random background colour and first letter of title
        textDrawable = TextDrawable.builder()
                .buildRound(letter, color);
      //  image.setImageDrawable(textDrawable);
    }
}
