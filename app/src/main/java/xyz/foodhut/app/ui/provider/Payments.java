package xyz.foodhut.app.ui.provider;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;

public class Payments extends AppCompatActivity {

    TextView current,paid,pendingWithdraw;
    Button request;
    long mCurrent,mPendingWithdraw,mPaid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        current=findViewById(R.id.pCurrentBalance);
        paid=findViewById(R.id.pWithdrawn);
        pendingWithdraw=findViewById(R.id.pWithdrawRequest);

        request=findViewById(R.id.btnRequestPayment);

        checkBalance();
    }

    public void checkBalance() {

            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("payments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        HashMap hashUser = (HashMap) snapshot.getValue();
                        mCurrent = (long) hashUser.get("currentBalance");
                        mPendingWithdraw = (long) hashUser.get("withdrawRequest");
                        mPaid = (long) hashUser.get("totalWithdrawn");

                        String currentBal= String.valueOf(mCurrent);
                        String pendingWith= String.valueOf(mPendingWithdraw);
                        String paidBal= String.valueOf(mPaid);

                        current.setText(currentBal);
                        pendingWithdraw.setText(pendingWith);
                        paid.setText(paidBal);

                        Log.d("check", "balance: " + mCurrent);

                    }
                }

                public void onCancelled(DatabaseError arg0) {
                }
            });
    }

    public void requestWithdraw(View view){

        if(mPendingWithdraw==0) {
            FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("payments").child("withdrawRequest").setValue(mCurrent);
            FirebaseDatabase.getInstance().getReference().child("payments/" + StaticConfig.UID).child("withdrawRequest").setValue(mCurrent);

            long newCurrent=(mCurrent-mPendingWithdraw);
            FirebaseDatabase.getInstance().getReference().child("payments/" + StaticConfig.UID).child("currentBalance").setValue(newCurrent);

            String temp= String.valueOf(newCurrent);
            current.setText(temp);
            pendingWithdraw.setText((int) mPendingWithdraw);
        }
        else {
            Toast.makeText(this, "you have a pending request", Toast.LENGTH_SHORT).show();
        }


        }
}
