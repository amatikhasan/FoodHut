package xyz.foodhut.app.ui.provider;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.materialrangebar.RangeBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.adapter.NotificationAdapter;
import xyz.foodhut.app.adapter.PaymentHistoryAdapter;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.Notification;
import xyz.foodhut.app.model.PaymentDetails;
import xyz.foodhut.app.ui.customer.HomeCustomer;

public class Payments extends AppCompatActivity {

    TextView current, paid, pendingWithdraw;

    TextView request,addMethod;
    String mMethod="",mBkash,mAccount,mAH,mBank;
    long mCurrent = 0, mPendingWithdraw = 0, mPaid = 0;
    Bundle extras;

    RecyclerView recyclerView;
    ArrayList<PaymentDetails> arrayList = new ArrayList<>();
    PaymentHistoryAdapter adapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        current = findViewById(R.id.pCurrentBalance);
        paid = findViewById(R.id.pWithdrawn);
        pendingWithdraw = findViewById(R.id.pWithdrawRequest);

        request = findViewById(R.id.btnRequestPayment);
        addMethod = findViewById(R.id.btnPaymentMethod);

        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvPaymentHistory);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentHistoryAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);



     /*   extras = getIntent().getExtras();
        if (extras != null) {
            mCurrent = extras.getLong("current");
            mPendingWithdraw = extras.getLong("pending");
            mPaid = extras.getLong("paid");

            if (mCurrent>0||mPendingWithdraw>0||mPaid>0) {
                String currentBal = String.valueOf(mCurrent);
                String pendingWith = String.valueOf(mPendingWithdraw);
                String paidBal = String.valueOf(mPaid);
                current.setText(currentBal);
                pendingWithdraw.setText(pendingWith);
                paid.setText(paidBal);
            }
        }
        */

        checkBalance();
        checkPaymentMethod();

        loadHistory();
    }

    private void loadHistory() {

        FirebaseDatabase.getInstance().getReference("providers/" + StaticConfig.UID + "/payments/history")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        //fetch files from firebase database and push in arraylist
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PaymentDetails details = snapshot.getValue(PaymentDetails.class);
                            arrayList.add(details);
                            Log.d("Check", "list noti: " + arrayList.size());
                        }

                        Collections.reverse(arrayList);
                        //bind the data in adapter
                        Log.d("Check", "list noti: " + arrayList.size());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });

        dialog.cancel();
    }

    private void checkPaymentMethod() {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("payments")
                .child("paymentMethod").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    mMethod = (String) hashUser.get("paymentMethod");

                    if (mMethod.equals("bKash")){
                        mBkash=(String) hashUser.get("bKashNumber");
                    }
                    if (mMethod.equals("bank")){
                        mAH=(String) hashUser.get("accountHolder");
                        mAccount=(String) hashUser.get("accountNumber");
                        mBank=(String) hashUser.get("bankName");
                    }

                    addMethod.setText("Payment Method");

                    Log.d("check", "balance: " + mMethod);

                }
            }

            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void goBack(View view) {
        //  startActivity(new Intent(this,HomeProvider.class));
        finish();
        // finishAffinity();
    }

    public void checkBalance() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait..");
        dialog.show();

        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID).child("payments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    mCurrent = (long) hashUser.get("currentBalance");
                    mPendingWithdraw = (long) hashUser.get("withdrawRequest");
                    mPaid = (long) hashUser.get("totalWithdrawn");

                    String currentBal = String.valueOf(mCurrent);
                    String pendingWith = String.valueOf(mPendingWithdraw);
                    String paidBal = String.valueOf(mPaid);

                    current.setText(currentBal);
                    pendingWithdraw.setText(pendingWith);
                    paid.setText(paidBal);

                    dialog.dismiss();
                    Log.d("check", "balance: " + mCurrent);

                }
            }

            public void onCancelled(DatabaseError arg0) {
                dialog.cancel();
            }
        });

        dialog.cancel();
    }

    public void addPaymentMethod(View view) {
        startActivity(new Intent(this, PaymentMethod.class));
        finish();
    }


    public void requestWithdraw(View view) {

        if (!mMethod.isEmpty()) {
            if (mPendingWithdraw == 0) {

                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View inflate = layoutInflater.inflate(R.layout.layout_payment_details, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(inflate);

                final EditText etBalance = inflate.findViewById(R.id.etAmount);
                TextView method=inflate.findViewById(R.id.txtMethod);
                String bla = String.valueOf(mCurrent);
                method.setText(mMethod);
                etBalance.setText(bla);

                builder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final long balance = Integer.parseInt(etBalance.getText().toString().trim());
                                if (balance >= 500 && balance <= mCurrent) {
                                    sendRequest(balance);
                                    request.setEnabled(false);
                                }
                                else
                                    Toast.makeText(Payments.this, "You don't have sufficient balance", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                Toast.makeText(this, "you have a pending request", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            startActivity(new Intent(this,PaymentMethod.class));
            finish();
        }
    }

    public void sendRequest(long balance) {
            Calendar c = Calendar.getInstance();
            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
            final String date = dateFormat.format(c.getTime());

            // long newCurrent=(mCurrent-mPendingWithdraw);
            PaymentDetails details = new PaymentDetails();
            details.kitchenName = StaticConfig.KITCHENNAME;
            details.providerId=StaticConfig.UID;
            details.time=date;
            details.phone=StaticConfig.PHONE;
            details.method = mMethod;
           // details.currentBalance = mCurrent;
            details.withdrawRequest = balance;
         //   details.totalWithdrawn = mPaid;
            details.status = "Pending";

            String key=FirebaseDatabase.getInstance().getReference().child("admin/payments").push().getKey();

            details.key=key;

            FirebaseDatabase.getInstance().getReference().child("providers").child(StaticConfig.UID).child("payments").child("withdrawRequest").setValue(balance);
            // FirebaseDatabase.getInstance().getReference().child("admin/payments").child(StaticConfig.UID).child("withdrawRequest").setValue(mCurrent);
            FirebaseDatabase.getInstance().getReference().child("admin/payments").child(key).setValue(details);

      /*      xyz.foodhut.app.model.PaymentMethod method=new xyz.foodhut.app.model.PaymentMethod();
            if (mMethod.equals("bKash")){
                method.paymentMethod=mMethod;
                method.bKashNumber=mBkash;

             //   details.paymentMethod=method;

                FirebaseDatabase.getInstance().getReference().child("admin/payments").child(key).child(StaticConfig.UID).child("details").setValue(method);

            }
            if (mMethod.equals("bank")){
                method.paymentMethod=mMethod;
                method.accountHolder=mAH;
                method.accountNumber=mAccount;
                method.bankName=mBank;

              //  details.paymentMethod=method;

                FirebaseDatabase.getInstance().getReference().child("admin/payments").child(key).child(StaticConfig.UID).child("details").setValue(method);
            } */

            String temp = String.valueOf(balance);
            //  current.setText(temp);
            pendingWithdraw.setText(temp);
            Toast.makeText(this, "Withdraw request is successful!", Toast.LENGTH_SHORT).show();


    }
}
