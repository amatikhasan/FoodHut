package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;

public class Kitchen extends AppCompatActivity {

    String today, thisMonth, sellToday;
    TextView tvSellToday, tvSellMonth, tvSellTotal,name;
    int sellMonth=0, sellTotal=0;
    private ProgressDialog dialog;

    CircleImageView circleImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        name=findViewById(R.id.name);
        circleImageView=findViewById(R.id.circlePhoto);

        tvSellToday=findViewById(R.id.tvSellsToday);
        tvSellMonth=findViewById(R.id.tvSellsMonth);
        tvSellTotal=findViewById(R.id.tvSellsTotal);

        today = checkDate();

        checkProfile();

        checkSellToday();
        checkSellMonth();
        checkSellTotal();


        dialog.cancel();
    }

    public void checkProfile(){
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/about")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //fetch files from firebase database and push in arraylist

                        if (dataSnapshot.getValue() != null) {

                            HashMap hashUser = (HashMap) dataSnapshot.getValue();
                          String  mName = (String) hashUser.get("kitchenName");
                         String   mImageUrl = (String) hashUser.get("avatar");

                         name.setText(mName);

                            if (!mImageUrl.equals("avatar")){
                                Picasso.get().load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(circleImageView);
                               // Glide.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.kitchen_icon_colour).into(circleImageView);
                            }


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public String checkDate() {
        // Get the calander
        int day, month, year;
        String date = null;
        Calendar c = Calendar.getInstance();

        // From calander get the year, month, day, hour, minute
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        c.set(year, month, day);
        date = day + "-" + (month + 1) + "-" + year;

        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("dd MMM yyyy");
        Date now = c.getTime();
        formattedDate = sdtf.format(now);

        String parts[] = formattedDate.split(" ");
        thisMonth = parts[1] + " " + parts[2];

        Log.d("check", "checkDate: " + date + " " + formattedDate);

        return formattedDate;
    }

    public void checkSellToday() {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(today).child("sellAmount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    sellToday = dataSnapshot.getValue().toString();
                    tvSellToday.setText(String.valueOf(sellToday));

                } else {
                    sellToday = "0";
                    tvSellToday.setText(sellToday);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkSellMonth() {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        if (dsp.getKey().contains(thisMonth))
                            getSellMonth(dsp.getKey());

                        Log.d("check", "onDataChange: date " + dsp.getKey());
                    }

                   // String sell = "৳ "+sellMonth;
                    tvSellMonth.setText(String.valueOf(sellMonth));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkSellTotal() {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {


                            getSellTotal(dsp.getKey());

                        Log.d("check", "onDataChange: date " + dsp.getKey());
                    }

                   // String sell = "৳ "+sellTotal;
                    tvSellTotal.setText(String.valueOf(sellTotal));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getSellMonth(String date) {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(date).child("sellAmount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    sellMonth += Integer.parseInt(dataSnapshot.getValue().toString());
                    tvSellMonth.setText(String.valueOf(sellMonth));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getSellTotal(String date) {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(date).child("sellAmount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    sellTotal += Integer.parseInt(dataSnapshot.getValue().toString());
                    tvSellTotal.setText(String.valueOf(sellTotal));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void goBack(View view) {
        finish();

    }
}
