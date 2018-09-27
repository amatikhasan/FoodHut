package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.DBHelper;
import xyz.foodhut.app.model.MenuProvider;
import xyz.foodhut.app.ui.customer.HomeCustomer;

import static xyz.foodhut.app.ui.PhoneAuthActivity.userID;

public class AddMenu extends AppCompatActivity {
    boolean isUpdate = false;
    EditText name, type, price, desc,extraItem,extraItemPrice;
    ImageView image;
    Button submit;
    TextView tvLocation, tvLunch, tvSnacks, tvDinner, tvBF, nName,tvPkg1,tvPkg2,tvPkg3,tvPkg4,tvPkg5;
    LinearLayout llLunch, llSnacks, llDinner, llBF;

    byte[] byteArray;
    public static byte[] bytes;
    private static final int IMAGE_REQUEST = 1;
    private Uri filePath;
    String mName, mType,mPackage="1", mPrice, mDesc, mImageUrl, mId,mExtraItem,mExtraItemPrice;
    int id;
    DBHelper dbHelper;
    Bundle extras;
    Toolbar toolbar;

    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);

        //toolbar = findViewById(R.id.toolbar);
       // getSupportActionBar();
        //setSupportActionBar(toolbar);
       // getSupportActionBar().setTitle("Add Menu");
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        name = findViewById(R.id.etItemName);
     //   type = findViewById(R.id.etItemType);
        price = findViewById(R.id.etItemPrice);
        extraItem = findViewById(R.id.etExtraItem);
        extraItemPrice = findViewById(R.id.etExtraPrice);
        name = findViewById(R.id.etItemName);
        image = (ImageView) findViewById(R.id.ivItemImage);
        submit =  findViewById(R.id.btnSubmit);
        desc=findViewById(R.id.etItemDesc);

        tvBF = findViewById(R.id.tvBF);
        tvDinner = findViewById(R.id.tvDinner);
        tvLunch = findViewById(R.id.tvLunch);
        tvSnacks = findViewById(R.id.tvSnacks);

        llBF = findViewById(R.id.llBF);
        llDinner = findViewById(R.id.llDinner);
        llLunch = findViewById(R.id.llLunch);
        llSnacks = findViewById(R.id.llSnacks);

        tvPkg1 = findViewById(R.id.pkg1);
        tvPkg2 = findViewById(R.id.pkg2);
        tvPkg3 = findViewById(R.id.pkg3);
        tvPkg4 = findViewById(R.id.pkg4);
        tvPkg5 = findViewById(R.id.pkg5);

        dbHelper = new DBHelper(getApplicationContext());

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString("id");
            mName = extras.getString("name");
            mType = extras.getString("type");
            mPackage = extras.getString("pkgSize");
            mPrice = extras.getString("price");
            mDesc = extras.getString("desc");
            mExtraItem = extras.getString("extraItem");
            mExtraItemPrice = extras.getString("extraItemPrice");
            //byteArray = extras.getByteArray("url");
            mImageUrl = extras.getString("url");
            // Log.d("check", "onCreate: "+byteArray);

            isUpdate = true;
           // getSupportActionBar().setTitle("Update Menu");

            name.setText(mName);
            price.setText(mPrice);
            desc.setText(mDesc);
            extraItem.setText(mExtraItem);
            extraItemPrice.setText(mExtraItemPrice);
            submit.setText("Update");
            setType();
            setPackage();

            Picasso.get().load(mImageUrl).placeholder(R.drawable.image).into(image);
            //Glide.with(contex).load(obj.imageUrl).into(holder.image);

            /*
            if (byteArray != null) {
                //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                image.setImageBitmap(bitmap);
                // btnDelete.setVisibility(View.VISIBLE);
            }
            */
        }
        Log.d("Extra Data Check", id + " " + mName + " " + isUpdate);

        checkFilePermissions();
    }

    public void goBack(View view){
        startActivity(new Intent(this,HomeProvider.class));
        finish();

    }

    public void browsImages(View view) {


        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", "image/*");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Select File");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Select File");
        }
        startActivityForResult(chooserIntent, IMAGE_REQUEST);
    }

    public void captureImage(View view) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }


    private void checkFilePermissions() {
        if (Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
            int permissionCheck = AddMenu.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += AddMenu.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
            }
        } else {
            Log.d("Check", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
              //  bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
                // bitmap.recycle();

                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("check", "onActivityResult: " + image);
        }

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                image.setImageBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
                // bitmap.recycle();
                stream.close();

                Log.d("check", "onActivityResult: " + image);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void submit(View view) {

        mName = name.getText().toString().trim();
        mPrice = price.getText().toString().trim();
        mDesc = desc.getText().toString().trim();
        mExtraItem=extraItem.getText().toString().trim();
        mExtraItemPrice=extraItemPrice.getText().toString().trim();

        if (isUpdate) {
            update();
        } else {

            save();
        }

        Log.d("check", "addList: " + id);
    }


    public void save() {

        //displaying a progress dialog while upload is going on
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");

        final String[] imageURL = {null};
        final boolean[] isError = {true};

        int number = (new Random().nextInt(100));

        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            //
            Toast.makeText(this, "Error authenticating", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UserID", "uploadImage: " + userID);


        if (!mName.equals("") || !mType.equals("") || !mPrice.equals("") || !mDesc.equals("")) {
            //if there is a file to upload
            if (filePath != null) {

                progressDialog.show();
                StorageReference riversRef = mStorageRef.child("images/" + userID + "/" + mName + number);
                riversRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //if the upload is successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                imageURL[0] = taskSnapshot.getDownloadUrl().toString();

                                //and displaying a success toast
                                if (imageURL[0] != null) {
                                   // Toast.makeText(getApplicationContext(), "Image Uploaded ", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(AddMenu.this, "Something Wrong! Please try again", Toast.LENGTH_SHORT).show();
                                }
                                Log.d("state", "onSuccess::  imageURL: " + imageURL[0]);
                                //save image info in database
                                String key = databaseReference.push().getKey();
                                MenuProvider menuProvider = new MenuProvider(key, mName, mType, mPrice, mDesc,mExtraItem,mExtraItemPrice, mPackage,taskSnapshot.getDownloadUrl().toString(), "0", "0");
                                databaseReference.child("providers/" + userID).child("menu").child(key).setValue(menuProvider);

                                isError[0] = false;

                                startActivity(new Intent(AddMenu.this, Menus.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //calculating progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                //displaying percentage in progress dialog
                                //progressDialog.setMessage("Uploaded " + ((int) progress) + "%");
                            }
                        });

               /* if (isError[0]) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadImage.this, "Something Wrong! Please try again", Toast.LENGTH_SHORT).show();
                    Log.d("state", "onSuccess " + isError[0] + " imageURL: " + imageURL[0]);

                } */

            } else {
                Toast.makeText(AddMenu.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
        //if there is not any file
        else {
            Toast.makeText(this, "Required Fields Are Missing", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteItem() {

        //displaying a progress dialog while upload is going on
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");


        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            //
            Toast.makeText(this, "Error authenticating", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UserID", "uploadImage: " + userID);


        databaseReference.child("providers/" + userID).child("menu").child(mId).removeValue();


        startActivity(new Intent(AddMenu.this, Menus.class));
        finish();
    }

    public void update() {


        //displaying a progress dialog while upload is going on
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");

        final String[] imageURL = {null};
        final boolean[] isError = {true};

        int number = (new Random().nextInt(100));

        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            //
            Toast.makeText(this, "Error authenticating", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UserID", "uploadImage: " + userID);


        if (!mName.equals("") || !mType.equals("") || !mPrice.equals("") || !mDesc.equals("")) {
            //if there is a file to upload
            if (filePath != null) {


                progressDialog.show();
                StorageReference riversRef = mStorageRef.child("images/" + userID + "/" + mName + number);
                riversRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //if the upload is successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                imageURL[0] = taskSnapshot.getDownloadUrl().toString();

                                //and displaying a success toast
                                if (imageURL[0] != null) {
                                    Toast.makeText(getApplicationContext(), "Image Uploaded ", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(AddMenu.this, "Something Wrong! Please try again", Toast.LENGTH_SHORT).show();
                                }
                                Log.d("state", "onSuccess::  imageURL: " + imageURL[0]);
                                //save image info in database
                                String id = databaseReference.push().getKey();
                                MenuProvider menuProvider = new MenuProvider(mId, mName, mType, mPrice, mDesc,mExtraItem,mExtraItemPrice,mPackage ,taskSnapshot.getDownloadUrl().toString(), "0", "0");
                                databaseReference.child("providers/" + userID).child("menu").child(mId).setValue(menuProvider);

                                isError[0] = false;

                                startActivity(new Intent(AddMenu.this, Menus.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //calculating progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                //displaying percentage in progress dialog
                                //progressDialog.setMessage("Uploaded " + ((int) progress) + "%");
                            }
                        });

            } else if (mImageUrl != null) {
                MenuProvider menuProvider = new MenuProvider(mId, mName, mType, mPrice, mDesc,mExtraItem,mExtraItemPrice,mPackage ,mImageUrl, "0", "0");
                databaseReference.child("providers/" + userID).child("menu").child(mId).setValue(menuProvider);

            } else {
                Toast.makeText(AddMenu.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
        //if there is not any file
        else {
            Toast.makeText(this, "Required Fields Are Missing", Toast.LENGTH_SHORT).show();
        }
    }

    public void getType(View view) {
        if (view.getId() == R.id.llLunch) {

            mType = "Lunch";

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.white));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.llSnacks) {

            mType = "Snacks";

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.white));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.llDinner) {

            mType = "Dinner";

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.white));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.llBF) {

            mType = "Breakfast";

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.white));
        }
    }


    public void getPackage(View view) {
        if (view.getId() == R.id.pkg1) {

            mPackage = "1";

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.white));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.pkg2) {

            mPackage = "2";

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.white));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.pkg3) {

            mPackage = "3";

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.white));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }
        if (view.getId() == R.id.pkg4) {

            mPackage = "4";

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.white));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }

        if (view.getId() == R.id.pkg5) {
            mPackage = "5";

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.white));
        }
    }

    public void setType() {
        if (mType.equals("Lunch")) {

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.white));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (mType.equals("Snacks")) {


            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.white));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (mType.equals("Dinner")) {

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_black_border));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.white));
            tvBF.setTextColor(getResources().getColor(R.color.gray));
        }
        if (mType.equals("Breakfast")) {

            llLunch.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llSnacks.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llDinner.setBackground(getResources().getDrawable(R.drawable.corner_black_border));
            llBF.setBackground(getResources().getDrawable(R.drawable.corner_primary_filled));

            tvLunch.setTextColor(getResources().getColor(R.color.gray));
            tvSnacks.setTextColor(getResources().getColor(R.color.gray));
            tvDinner.setTextColor(getResources().getColor(R.color.gray));
            tvBF.setTextColor(getResources().getColor(R.color.white));
        }
    }

    public void setPackage() {
        if (mPackage.equals("1")) {

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.white));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }
        if (mPackage.equals("2")) {

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.white));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }
        if (mPackage.equals("3")) {

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.white));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }
        if (mPackage.equals("4")) {

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.white));
            tvPkg5.setTextColor(getResources().getColor(R.color.gray));
        }

        if (mPackage.equals("5")) {

            tvPkg1.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg2.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg3.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg4.setBackground(getResources().getDrawable(R.drawable.round_primary_empty));
            tvPkg5.setBackground(getResources().getDrawable(R.drawable.round_primary_filled));

            tvPkg1.setTextColor(getResources().getColor(R.color.gray));
            tvPkg2.setTextColor(getResources().getColor(R.color.gray));
            tvPkg3.setTextColor(getResources().getColor(R.color.gray));
            tvPkg4.setTextColor(getResources().getColor(R.color.gray));
            tvPkg5.setTextColor(getResources().getColor(R.color.white));
        }
    }

    //For Action Bar

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);

        if (isUpdate) {
            getMenuInflater().inflate(R.menu.menu_update, menu);
        }
        if (!isUpdate) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            //for toolbar arrow
            case android.R.id.home:
                startActivity(new Intent(getApplicationContext(), Menus.class));
                finish();
                break;
            case R.id.menuSave:
                addList();
                //Toast.makeText(getApplicationContext(), "Save Button Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menuDelete:
                deleteItem();
                //Toast.makeText(getApplicationContext(), "Delete Button Clicked", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    */
}
