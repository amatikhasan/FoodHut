package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
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

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;

public class Sales extends AppCompatActivity {

    TextView kitchen, totalSells, month, sells;
    String mKitchen, mTotalSells, mMonth, mSells;
    BarChart barChart;
    Map<String, Integer> listSells;
    ArrayList<String> dateList;
    ArrayList<BarEntry> entries;
    private ProgressDialog dialog;
    String monthYear;
    int daysInMonth;
    ArrayList<Integer> days;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        days = new ArrayList<>();
        dateList = new ArrayList<>();
        listSells = new HashMap<>();
      entries = new ArrayList<>();

        barChart = (BarChart) findViewById(R.id.mChart);
        kitchen = findViewById(R.id.mKitchen);
        totalSells = findViewById(R.id.mTotalSells);
        month = findViewById(R.id.mMonth);
        sells = findViewById(R.id.mSells);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Menu...");
        dialog.show();


        monthYear = checkDate();


        kitchen.setText(StaticConfig.NAME);
        month.setText(monthYear);
        String txt="Sells in  "+monthYear;
        totalSells.setText(txt);

        getDate();

    }

    public void prepareChart(){
        Log.d("check", "prepare chart: "+days.size());

        BarDataSet bardataset = new BarDataSet(entries, "Sells");

        ArrayList<String> labels = new ArrayList<String>();
        for (int i=0;i<days.size();i++){
            labels.add(String.valueOf(days.get(i)));
            Log.d("check", "prepare chart: "+days.get(i));
        }

        BarData data = new BarData( labels, bardataset);
        barChart.setData(data); // set the data and list of lables into chart
        barChart.setDescription("Sells in "+monthYear);  // set the description
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        barChart.animateY(1000);
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

    public void goBack(View view){
        startActivity(new Intent(this,HomeProvider.class));
        finish();
        finishAffinity();
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
                    getOrderList();
                    //  Collections.reverse(obj);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }
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

                    entries.add(new BarEntry(sellCount[0], finalI));

                    Log.d("check", "date: " + parts[0]+" "+date);
                    Log.d("check", "sellCount: " + sellCount[0]);
                    Log.d("check", "listSells size: " + listSells.size());

                    if(finalI==dateList.size()-1){
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
