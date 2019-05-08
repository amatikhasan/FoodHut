package xyz.foodhut.app.ui.provider;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import xyz.foodhut.app.R;

public class ProviderHome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);
    }

    public void myMenu(View view){
        startActivity(new Intent(this,Menus.class));
    }

    public void mySchedule(View view){
        startActivity(new Intent(this, ScheduleTab.class));
    }
    public void orders(View view){
        startActivity(new Intent(this,OrdersDate.class));
    }

}
