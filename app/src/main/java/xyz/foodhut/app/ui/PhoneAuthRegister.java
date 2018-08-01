package xyz.foodhut.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.User;
import xyz.foodhut.app.ui.provider.ProviderHome;

public class PhoneAuthRegister extends AppCompatActivity {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    EditText phone, code, name, pass, repeatPass;
    LinearLayout llName, llPhone, llPass, llrepeatPass;
    Button signup, verify, resend;
    String mVerificationId, mCode, mPhone, mName,mPass;
    String userId;
    TextView timertext;
    Timer timer;
    Boolean mVerified = false;
    public static String STR_EXTRA_ACTION_REGISTER = "register";
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth_register);
        getSupportActionBar().setTitle("My Chat");
        phone = (EditText) findViewById(R.id.et_phone);
        name = (EditText) findViewById(R.id.et_name);
        code = (EditText) findViewById(R.id.et_code);
        signup = (Button) findViewById(R.id.bt_signup);
        verify = (Button) findViewById(R.id.bt_verify);
        resend = (Button) findViewById(R.id.bt_resend);
        timertext = (TextView) findViewById(R.id.tv_timer);


        initFirebase();

        isLoggedIn();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:" + credential);
                getCode(credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Verification Failed !! Invalied verification Code", Snackbar.LENGTH_LONG);
                    //snackbar.show();
                    Toast.makeText(PhoneAuthRegister.this, "Invalied verification Code", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Verification Failed !! Too many request. Try after some time. ", Snackbar.LENGTH_LONG);
                    //snackbar.show();
                    Toast.makeText(PhoneAuthRegister.this, "Something wrong, Try again", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                Toast.makeText(PhoneAuthRegister.this, "Code Sent", Toast.LENGTH_SHORT).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPhone = phone.getText().toString().trim();
                mName=name.getText().toString().trim();

                    if (!mPhone.isEmpty() && mPhone.length() == 11) {

                        startPhoneNumberVerification(phone.getText().toString().trim());
                        mVerified = false;
                        starttimer();
                        code.setVisibility(View.VISIBLE);
                        verify.setVisibility(View.VISIBLE);
                        timertext.setVisibility(View.VISIBLE);

                        signup.setVisibility(View.GONE);
                        phone.setVisibility(View.GONE);
                        name.setVisibility(View.GONE);

                        //signup.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
                        signup.setTag(getResources().getString(R.string.tag_verify));
                    } else {
                        phone.setError("invalid mobile number");
                    }

                if (!code.getText().toString().trim().isEmpty() && !mVerified) {
                    //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Please wait...", Snackbar.LENGTH_LONG);
                    //snackbar.show();
                    Toast.makeText(PhoneAuthRegister.this, "Please Wait...", Toast.LENGTH_SHORT).show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code.getText().toString().trim());
                    signInWithPhoneAuthCredential(credential);
                }
                if (mVerified) {
                    startActivity(new Intent(PhoneAuthRegister.this, ProviderHome.class));
                }
            }
        });

    }

    private void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky

            mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        StaticConfig.UID = user.getUid();
                        Log.d("check", "onAuthStateChanged:signed_in:" + user.getUid());

                        startActivity(new Intent(PhoneAuthRegister.this, ProviderHome.class));
                        PhoneAuthRegister.this.finish();

                        Log.d("Check", "onAuthStateChanged:signed_out");
                    }
    }

    public void isLoggedIn(){
        if(SharedPreferenceHelper.getInstance(getApplicationContext()).isLoggedIn()) {
            startActivity(new Intent(this,ProviderHome.class));
        }
    }

    public void verify(View view) {
        if(!code.getText().toString().trim().isEmpty()) {
            if (!mVerified) {
                //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Please wait...", Snackbar.LENGTH_LONG);
                //snackbar.show();
                Toast.makeText(this, "Please Wait...", Toast.LENGTH_SHORT).show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code.getText().toString().trim());
                signInWithPhoneAuthCredential(credential);
            }

            if (mVerified) {
                startActivity(new Intent(PhoneAuthRegister.this, MainActivity.class));
            }
        }else
            Toast.makeText(this, "Please Enter Code", Toast.LENGTH_SHORT).show();
    }

    public void getCode(PhoneAuthCredential cred) {
        // Set the verification text based on the credential
        if (cred != null) {
            if (cred.getSmsCode() != null) {
                mCode = cred.getSmsCode();
                code.setText(cred.getSmsCode());
            } else {
                //         mVerificationField.setText(R.string.instant_validation);
            }
        }
    }

    public void resend(View view) {
        if (!mPhone.isEmpty() && mPhone.length() == 11) {
            resendVerificationCode(mPhone, mResendToken);
            mVerified = false;
            starttimer();
            code.setVisibility(View.VISIBLE);
            //signup.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
            signup.setTag(getResources().getString(R.string.tag_verify));
            //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Resending verification code...", Snackbar.LENGTH_LONG);
            //snackbar.show();
        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            userId = user.getUid();

                            mVerified = true;
                            timer.cancel();
                            timertext.setVisibility(View.INVISIBLE);
                            //phone.setEnabled(false);
                            code.setEnabled(false);

                            newUserInfo();

                            //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Successfully Verified", Snackbar.LENGTH_LONG);
                            //snackbar.show();
                            Toast.makeText(PhoneAuthRegister.this, "Sign Up Completed", Toast.LENGTH_SHORT).show();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                //Snackbar snackbar = Snackbar.make((CoordinatorLayout) findViewById(R.id.parentlayout), "Invalid OTP ! Please enter correct OTP", Snackbar.LENGTH_LONG);
                                //snackbar.show();
                                Toast.makeText(PhoneAuthRegister.this, "Please enter correct code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+88" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    public void starttimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //timertext.setText("RESEND CODE");
                            resend.setVisibility(View.VISIBLE);
                            timertext.setVisibility(View.GONE);
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

  public void newUserInfo() {


        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    StaticConfig.UID=userId;
                    Intent y = new Intent(PhoneAuthRegister.this, ProviderHome.class);
                    y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(y);

                    //user exists, do something
                    saveUserInfo();
                    //store user id in sharedprefMngr class
                    // SharedPrefManager.getmInstance(getApplicationContext()).userLogin(userID);

                } else {

                    //store user id in sharedprefMngr class
                    //SharedPrefManager.getmInstance(getApplicationContext()).userLogin(userID);

                    //if user not exist in databse, then add
                    // name=mNameField.getText().toString();
                    //contactno = mPhoneNumberField.getText().toString();
                    //user does not exist, do something else
                    StaticConfig.UID=userId;

                    User newUser = new User();
                    newUser.phone = mPhone;
                    newUser.name=mName;
                    newUser.address="Address";
                    newUser.avatar = StaticConfig.STR_DEFAULT_BASE64;

                    FirebaseDatabase.getInstance().getReference().child("providers/" + userId).child("about").setValue(newUser);

                    Intent y = new Intent(PhoneAuthRegister.this, ProviderHome.class);
                    y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(y);

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });

    }

  public   void saveUserInfo() {
        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                User userInfo = new User();
                userInfo.phone = (String) hashUser.get("phone");
                userInfo.avatar = (String) hashUser.get("avatar");
                SharedPreferenceHelper.getInstance(PhoneAuthRegister.this).saveUserInfo(userInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
