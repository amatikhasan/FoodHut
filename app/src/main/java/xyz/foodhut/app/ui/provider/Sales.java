package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.dacer.androidcharts.BarView;
import im.dacer.androidcharts.LineView;
import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.OrderDetails;

public class Sales extends AppCompatActivity {

    TextView kitchen, totalSells, month, sells;
    String mKitchen, mTotalSells, mMonth, mSells;

    Map<String, Integer> listSells;
    ArrayList<String> dateList;
    ArrayList<Integer> sellsList;

    private ProgressDialog dialog;
    String monthYear;
    int daysInMonth;
    ArrayList<Integer> days;

    int dailySells = 0;

    LineView barView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        days = new ArrayList<>();
        dateList = new ArrayList<>();
        sellsList = new ArrayList<>();
        listSells = new HashMap<>();



        kitchen = findViewById(R.id.mKitchen);
        totalSells = findViewById(R.id.mTotalSells);
        month = findViewById(R.id.mMonth);
        sells = findViewById(R.id.mSells);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        barView = findViewById(R.id.barView);

        monthYear = checkDate();


        kitchen.setText(StaticConfig.KITCHENNAME);
        month.setText(monthYear);
        String txt = "Sells in  " + monthYear;
        totalSells.setText(txt);

        getDate();

    }

    public void prepareChart() {
        Log.d("check", "prepare chart: " + days.size());


        ArrayList<String> labels = new ArrayList<String>();
        for (int i = 0; i < days.size(); i++) {
            labels.add(String.valueOf(days.get(i)));
            Log.d("check", "prepare chart: " + days.get(i));
        }

        /*
        BarData data = new BarData( labels, bardataset);
        barChart.setData(data); // set the data and list of lables into chart
        barChart.setDescription("Sells in "+monthYear);  // set the description
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.animateY(1000);
        */

        ArrayList<ArrayList<Integer>> data = new ArrayList<>();
        data.add(sellsList);

        barView.setDrawDotLine(true);
        barView.setShowPopup(LineView.SHOW_POPUPS_All);
        barView.setColorArray(new int[]{
                Color.parseColor("#F44336")
        });
        barView.setBottomTextList(labels);
        // barView.setDataList(sellsList,31);
        barView.setDataList(data);
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

        daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        c.set(year, month, day);
        date = day + "-" + (month + 1) + "-" + year;

        String formattedDate;
        SimpleDateFormat sdtf = new SimpleDateFormat("MMM yyyy");
        Date now = c.getTime();
        formattedDate = sdtf.format(now);

        Log.d("check", "checkDate: " + date + " " + formattedDate);

        return formattedDate;
    }

    public void goBack(View view) {
        //   startActivity(new Intent(this,HomeProvider.class));
        finish();
        //  finishAffinity();
    }

    public void getDate() {
        if (StaticConfig.UID != null) {
            FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/orders").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        dateList.add(String.valueOf(dsp.getKey())); //add result into array list

                        Log.d("check", "onDataChange: date " + dsp.getKey());
                    }
                    Log.d("check", "dateList size: " + dateList.size());
                    // getOrderList();
                    getMenuList();
                    //  Collections.reverse(obj);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }
    }

    public void getMenuList() {
        for (int i = 0; i < dateList.size(); i++) {
            final String date = dateList.get(i);

            final int finalI = i;

            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(date).child("sellAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dialog.dismiss();
                    //fetch files from firebase database and push in arraylist
                    if (dataSnapshot.getValue() != null) {

                        int sell = Integer.parseInt(String.valueOf(dataSnapshot.getValue()));


                        String parts[] = date.split(" ");
                        days.add(Integer.parseInt(parts[0].trim()));
                        sellsList.add(sell);


                        if (finalI == dateList.size() - 1) {
                            Log.d("check", "calling prepare chart ");
                            prepareChart();
                        }


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //  dialog.dismiss();
                }
            });



        }
    }

    public void getOrderList(final String menuId, final String date, final int i) {
        //menuList.clear();
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(date).child(menuId).child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    getOrderDetails(menuId, date, String.valueOf(dsp.getKey()), i);

                    Log.d("check", "onDataChange: orderId from list " + dsp.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //  dialog.dismiss();
            }
        });
    }

    public void getOrderDetails(final String menuId, final String date, String orderId, final int i) {


        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID + "/orders").child(date).child(menuId).child("orders").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist


                        OrderDetails details = dataSnapshot.getValue(OrderDetails.class);

                        if (details != null) {


                       /*     if (sellsList.get(i)!=null){
                                int sell=sellsList.get(i)+details.amount;
                                sellsList.set(i,sell);
                            }
                            else
                                sellsList.add(details.amount);

                                */

                            dailySells = dailySells + details.amount;

                            Log.d("Check list", "onDataChange: details " + dailySells);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });

        //  }


    }


    public void getOrderList() {
        for (int i = 0; i < dateList.size(); i++) {
            final String date = dateList.get(i);
            final int[] sellCount = {0};

            final int finalI = i;
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("orders").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dialog.dismiss();
                    //fetch files from firebase database and push in arraylist

                    //List<String> lst = new ArrayList<String>(); // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        sellCount[0] += dsp.getChildrenCount();
                        Log.d("check", "onDataChange: sell " + date + " " + dsp.getKey() + " " + dsp.getChildrenCount());
                    }

                    listSells.put(date, sellCount[0]);
                    String parts[] = date.split(" ");
                    days.add(Integer.parseInt(parts[0].trim()));

                    sellsList.add(sellCount[0]);

                    Log.d("check", "date: " + parts[0] + " " + date);
                    Log.d("check", "sellCount: " + sellCount[0]);
                    Log.d("check", "listSells size: " + listSells.size());

                    if (finalI == dateList.size() - 1) {
                        Log.d("check", "calling prepare chart ");
                        prepareChart();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //  dialog.dismiss();
                }
            });
        }
    }
}
