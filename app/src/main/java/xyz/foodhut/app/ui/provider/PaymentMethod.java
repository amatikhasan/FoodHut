package xyz.foodhut.app.ui.provider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.StaticConfig;

public class PaymentMethod extends AppCompatActivity {

    CheckBox bKash, bank;
    EditText phone, name, account, bankName;
    LinearLayout llBKash, llBank;
    String mMethod = "", mPhone = "", mName = "", mBank = "", mAccount = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        phone = findViewById(R.id.etPhone);
        name = findViewById(R.id.etName);
        account = findViewById(R.id.etAccount);
        bankName = findViewById(R.id.etBank);

        bKash = findViewById(R.id.checkBkash);
        bank = findViewById(R.id.checkBank);

        llBKash = findViewById(R.id.llBKash);
        llBank = findViewById(R.id.llBank);

        bKash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llBKash.setVisibility(View.VISIBLE);
                    bKash.setTextColor(getResources().getColor(R.color.colorPrimary));
                    llBank.setVisibility(View.GONE);
                    bank.setTextColor(getResources().getColor(R.color.black));
                    bank.setChecked(false);

                    mMethod = "bkash";
                }
            }
        });

        bank.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llBKash.setVisibility(View.GONE);
                    bKash.setTextColor(getResources().getColor(R.color.black));
                    bKash.setChecked(false);
                    llBank.setVisibility(View.VISIBLE);
                    bank.setTextColor(getResources().getColor(R.color.colorPrimary));

                    mMethod = "bank";
                }
            }
        });

        method();



    }

    public void goBack(View view) {
        //  startActivity(new Intent(this,HomeCustomer.class));
        finish();
        // finishAffinity();
    }

    public void method() {
        FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID)
                .child("payments").child("paymentMethod").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    HashMap hashUser = (HashMap) snapshot.getValue();
                    mMethod = (String) hashUser.get("paymentMethod");
                    if (mMethod.equals("bank")) {
                        mName = (String) hashUser.get("accountHolder");
                        mAccount = (String) hashUser.get("accountNumber");
                        mBank = (String) hashUser.get("bankName");

                        name.setText(mName);
                        account.setText(mAccount);
                        bankName.setText(mBank);

                        bank.setChecked(true);
                        bKash.setChecked(false);
                    } else {
                        mPhone = (String) hashUser.get("bKashNumber");

                        phone.setText(mPhone);

                        bank.setChecked(false);
                        bKash.setChecked(true);
                    }

                    Log.d("check", "method: " + mMethod);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }


    public void done(View view) {

        if (mMethod.equals("bkash")) {

            mPhone = phone.getText().toString().trim();

            if (!mPhone.isEmpty()) {

                final xyz.foodhut.app.model.PaymentMethod method = new xyz.foodhut.app.model.PaymentMethod();
                method.paymentMethod = "bKash";
                method.bKashNumber = mPhone;

                final AlertDialog.Builder builder = new AlertDialog.Builder(PaymentMethod.this);
                builder.setMessage("Are you sure you want to add " + mPhone + " as your bKash account number?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {

                                FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID)
                                        .child("payments").child("paymentMethod").setValue(method);

                                Toast.makeText(PaymentMethod.this, "Payment method updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();

            } else {
                Toast.makeText(this, "Please give your bKash number", Toast.LENGTH_SHORT).show();
            }

        } else if (mMethod.equals("bank")) {

            mName = name.getText().toString().trim();
            mAccount = account.getText().toString().trim();
            mBank = bankName.getText().toString().trim();

            if (!mName.isEmpty() && !mAccount.isEmpty() && !mBank.isEmpty()) {

                final xyz.foodhut.app.model.PaymentMethod method = new xyz.foodhut.app.model.PaymentMethod();
                method.paymentMethod = "bank";
                method.accountHolder = mName;
                method.accountNumber = mAccount;
                method.bankName = mBank;

                final AlertDialog.Builder builder = new AlertDialog.Builder(PaymentMethod.this);
                builder.setMessage("Are you sure you want to add " + mAccount + " as your bank account number?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {

                                FirebaseDatabase.getInstance().getReference().child("providers/" + StaticConfig.UID)
                                        .child("payments").child("paymentMethod").setValue(method);

                                Toast.makeText(PaymentMethod.this, "Payment method updated", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();

            } else {
                Toast.makeText(this, "Please give your account information", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
        }

    }
}
