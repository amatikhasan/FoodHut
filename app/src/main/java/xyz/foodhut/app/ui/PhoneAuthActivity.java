package xyz.foodhut.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import xyz.foodhut.app.R;
import xyz.foodhut.app.data.SharedPreferenceHelper;
import xyz.foodhut.app.data.StaticConfig;
import xyz.foodhut.app.model.User;
import xyz.foodhut.app.ui.customer.HomeCustomer;
import xyz.foodhut.app.ui.provider.HomeProvider;
import xyz.foodhut.app.ui.provider.ProviderHome;

public class PhoneAuthActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    private String code;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    Bundle extras;

    private EditText mNameField;
    private EditText mVerificationField;
    private EditText mPhoneNumberField;

    int checkTimer = 0;
    CountDownTimer countDownTimer;
    public static String userID;
    private Button mStartButton;
    private Button mVerifyButton;
    private Button mResendButton;
    private String phone="", name, type, userType;
    private TextView timer;
    private TextView tvLogin, tvVerifyCode, tvDRC, tvPEVC, tvGSWF;
    ImageView ivCustomer, ivKitchen;
    private LinearLayout timerLayout;
    private LinearLayout layoutSuccess;
    private LinearLayout layoutPhone;
    private LinearLayout layoutGoogle;
    private LinearLayout llGoogleSignIn;

    CountryCodePicker ccp;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        //  getSupportActionBar().hide();

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign views

        dialog = new ProgressDialog(this);
        dialog.setMessage("Signing In");

        mPhoneNumberField = (EditText) findViewById(R.id.field_phone_number);
        mVerificationField = (EditText) findViewById(R.id.field_verification_code);
        // mNameField = (EditText) findViewById(R.id.field_name);
        //smsCode = (Pinview) findViewById(R.id.sms_code);
        timer = (TextView) findViewById(R.id.timer);

        ccp=findViewById(R.id.ccpicker);

        tvDRC = (TextView) findViewById(R.id.tvDRC);
        tvPEVC = (TextView) findViewById(R.id.txtPEVC);
        tvGSWF = (TextView) findViewById(R.id.txtGSWF);

        timerLayout = findViewById(R.id.timerLayout);
        layoutPhone = findViewById(R.id.llPhone);
        layoutGoogle = findViewById(R.id.llGoogle);
        llGoogleSignIn = findViewById(R.id.llGoogleSignIn);

        ivCustomer = findViewById(R.id.iv_round_customer);
        ivKitchen = findViewById(R.id.iv_round_kitchen);

        mStartButton = (Button) findViewById(R.id.button_start_verification);
        mVerifyButton = (Button) findViewById(R.id.button_verify_phone);
        mResendButton = (Button) findViewById(R.id.button_resend);

        // Assign click listeners
        mStartButton.setOnClickListener(this);
        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //get Intent Data
        extras = getIntent().getExtras();
        if (extras != null) {
            type = extras.getString("type");

            if (type != null && type.equals("provider")) {

                ivKitchen.setVisibility(View.VISIBLE);
                ivCustomer.setVisibility(View.GONE);
                mStartButton.setBackground(getResources().getDrawable(R.drawable.round_button_green));
                mVerifyButton.setBackground(getResources().getDrawable(R.drawable.round_button_green));
                mResendButton.setBackground(getResources().getDrawable(R.drawable.round_button_green));
            }
        }

        initFirebase();


        //enableViews(mStartButton, mPhoneNumberField, mNameField, mPasswordField,tvPLTVC);
        //disableViews(mVerifyButton, mResendButton, mVerificationField, timerLayout);
        //enableViews(layoutSignUp);
        //disableViews(layoutVerify);


        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mPhoneNumberField.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    //  Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                    //    Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]

/*
        smsCode.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean b) {
                smsCode.setValue(code);
                //trigger this when the OTP code has finished typing
                final String verifyCode = smsCode.getValue();
                verifyPhoneNumberWithCode(mVerificationId, verifyCode);
            }
        });

        */
        llGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
    }


    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {

            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        if (phoneNumber.length()==11) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+88" + phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks        // OnVerificationStateChangedCallbacks
                    );
        }// ForceResendingToken from callbacks
        if (phoneNumber.length()==10) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+880" + phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks         // OnVerificationStateChangedCallbacks
                    );
        }// ForceResendingToken from callbacks

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        if (phoneNumber.length()==11) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+88" + phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks,         // OnVerificationStateChangedCallbacks
                    token);
        }// ForceResendingToken from callbacks
        if (phoneNumber.length()==10) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+880" + phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks,         // OnVerificationStateChangedCallbacks
                    token);
        }// ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();


                            //check if user exist or not in Database


                            userID = user.getUid();
                            StaticConfig.UID = userID;

                            newUserInfo(userID);

                            //  myRef.child(userID).child("title").setValue("true");
                            //    myRef.child(userID).child("description").setValue("true");
                            // myRef.child(userID).child("imageurl").setValue("true");
                            //  myRef.child(userID).child("url").setValue("true");
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);

                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });

        progressDialog.cancel();
    }
    // [END sign_in_with_phone]

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            userID = user.getUid();
                            StaticConfig.UID = userID;
                            newUserInfo(userID);
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //  Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //  Toast.makeText(PhoneAuthActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            updateUI(STATE_SIGNIN_FAILED);
                        }

                        // ...
                    }
                });

        dialog.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        dialog.show();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                dialog.cancel();
                // ...
            }
        }
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                //enableViews(mStartButton, mPhoneNumberField, mNameField, tvLogin);
                //disableViews(mVerifyButton, mResendButton, mVerificationField, timerLayout, tvDRC, tvVerifyCode, tvPLTVC);
                //enableViews(layoutSignUp);
                //disableViews(layoutVerify);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                //enableViews(mVerifyButton, mResendButton, mPhoneNumberField, mVerificationField);
                //disableViews(mStartButton);

                Toast.makeText(PhoneAuthActivity.this, "code sent", Toast.LENGTH_LONG).show();
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options

                //cancel countdowntimer
                countDownTimer.cancel();
                disableViews(mVerifyButton, mResendButton, mVerificationField, timerLayout, tvDRC, tvPEVC);
                enableViews(mStartButton, layoutPhone, layoutGoogle);
                //  mDetailText.setText(R.string.status_verification_failed);


                Toast.makeText(PhoneAuthActivity.this, "verification failed", Toast.LENGTH_LONG).show();
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                //disableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,mVerificationField,timerLayout);

                //  mDetailText.setText(R.string.status_verification_succeeded);

                Toast.makeText(PhoneAuthActivity.this, "verification success", Toast.LENGTH_LONG).show();

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        code = cred.getSmsCode();
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        //         mVerificationField.setText(R.string.instant_validation);
                    }
                }

                //disableViews(mVerifyButton, mVerificationField, timerLayout, tvDRC, tvVerifyCode, tvPLTVC);
                //enableViews(tvSuccess);

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                //  mDetailText.setText(R.string.status_sign_in_failed);
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check

                break;
        }

        if (user == null) {
            // Signed out
            //  mPhoneNumberViews.setVisibility(View.VISIBLE);
            //    mSignedInViews.setVisibility(View.GONE);

            //  mStatusText.setText(R.string.signed_out);
        } else {
            // Signed in


            //  mStatusText.setText(R.string.signed_in);
            //  mDetailText.setText(getString(R.string.firebase_status_fmt, user.getUid()));
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber) || !(phoneNumber.length() > 9 && phoneNumber.length() < 12)) {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
            v.setVisibility(View.VISIBLE);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
            v.setVisibility(View.GONE);
        }
    }

    private void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            StaticConfig.UID = user.getUid();
            Log.d("check", "onAuthStateChanged:signed_in:" + user.getUid());

            if (type.equals("provider")) {
                Intent y = new Intent(PhoneAuthActivity.this, HomeProvider.class);
                startActivity(y);
                this.finish();
            }
            if (type.equals("customer")) {
                Intent y = new Intent(PhoneAuthActivity.this, HomeCustomer.class);
                startActivity(y);
                this.finish();
            }

            Log.d("Check", "onAuthStateChanged:signed_out");
        }
    }

    public void isLoggedIn() {
        if (SharedPreferenceHelper.getInstance(getApplicationContext()).isLoggedIn()) {

            startActivity(new Intent(this, ProviderHome.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start_verification:

             /*   if(mNameField.getText().toString().isEmpty())
                {
                    mNameField.setError("Enter Name");
                    return;
                }  */
                if (!validatePhoneNumber()) {
                    return;
                }
                //mStartButton.setBackgroundColor(pink);
                phone = mPhoneNumberField.getText().toString();

                startPhoneNumberVerification(mPhoneNumberField.getText().toString());
                setTimer();

                disableViews(mStartButton, layoutPhone, tvGSWF, layoutGoogle);
                enableViews(mVerifyButton, mVerificationField, timerLayout, tvDRC, tvPEVC);
                break;
            case R.id.button_verify_phone:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                } else
                    verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.button_resend:
                //set timer for resent code
                setTimer();
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
                mResendButton.setVisibility(View.INVISIBLE);
                timerLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void setTimer() {
        checkTimer = 1;

        //show loading screen
        //enableViews(mVerificationField, mVerifyButton, timerLayout);


        //time to show retry button
        countDownTimer = new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long l) {
                timer.setText("0:" + l / 1000 + " s");
                mResendButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinish() {
                timer.setText(0 + " s");
                mResendButton.startAnimation(AnimationUtils.loadAnimation(PhoneAuthActivity.this, android.R.anim.slide_out_right));
                mResendButton.setVisibility(View.VISIBLE);

                timerLayout.setVisibility(View.GONE);

                checkTimer = 0;
            }
        }.start();
        //timer ends here
    }

    public void newUserInfo(final String userId) {

        // String phone=mPhoneNumberField.getText().toString();
        StaticConfig.UID = userId;

        if (type.equals("customer")) {
            checkCustomer(userId);
        }
        if (type.equals("provider")) {
            checkProvider(userId);
        }

    }

    public void checkProvider(final String userId) {
        FirebaseDatabase.getInstance().getReference().child("providers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    Intent y = new Intent(PhoneAuthActivity.this, HomeProvider.class);
                    y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(y);
                    finish();

                    SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setUID(userId);
                    SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setType("provider");

                } else {


                    User newUser2 = new User();
                    if (!phone.isEmpty())
                        newUser2.phone = phone;
                    else
                        newUser2.phone = "phone";

                    newUser2.name = "name";
                    newUser2.kitchenName = "kitchen";
                    newUser2.address = "address";
                    newUser2.status = "Inactive";
                    newUser2.rating = "0";
                    newUser2.ratingCount = "0";
                    newUser2.latitude = "0";
                    newUser2.longitude = "0";
                    newUser2.avatar = StaticConfig.STR_DEFAULT_BASE64;
                    FirebaseDatabase.getInstance().getReference().child("providers/" + userId).child("about").setValue(newUser2);

                    SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setUID(userId);
                    SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setType("provider");

                    Intent y = new Intent(PhoneAuthActivity.this, HomeProvider.class);
                    y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(y);
                    finish();

                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    public void checkCustomer(final String userId) {
        FirebaseDatabase.getInstance().getReference().child("customers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                        Intent y = new Intent(PhoneAuthActivity.this, HomeCustomer.class);
                        startActivity(y);
                    y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();

                        //saveUserInfo(String);
                        SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setUID(userId);
                        SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setType("customer");

                } else {
                    //FirebaseDatabase.getInstance().getReference().child("users/" + userId).setValue(newUser);

                    if (type.equals("customer")) {
                        User newUser3 = new User();
                        newUser3.phone = phone;
                        newUser3.name = "name";
                        newUser3.address = "address";
                        newUser3.status = "Active";
                        newUser3.corporateCode = "code";
                        newUser3.corporateDue = 0;

                        //    newUser3.latitude=0;
                        //    newUser3.longitude=0;
                        newUser3.avatar = StaticConfig.STR_DEFAULT_BASE64;
                        FirebaseDatabase.getInstance().getReference().child("customers/" + userId).child("about").setValue(newUser3);

                        SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setUID(userId);
                        SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).setType("customer");

                        Intent y = new Intent(PhoneAuthActivity.this, HomeCustomer.class);
                        y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(y);
                        finish();
                    }
                }
            }

            public void onCancelled(DatabaseError arg0) {
            }
        });
    }


    public void saveUserInfo(String type) {
        FirebaseDatabase.getInstance().getReference().child("users/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                User userInfo = new User();
                userInfo.phone = (String) hashUser.get("phone");
                // userInfo.avatar = (String) hashUser.get("avatar");
                userInfo.type = (String) hashUser.get("type");
                // userInfo.phone = (String) hashUser.get("status");


                SharedPreferenceHelper.getInstance(PhoneAuthActivity.this).saveUserInfo(userInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getType() {
        FirebaseDatabase.getInstance().getReference().child("users/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    HashMap hashUser = (HashMap) dataSnapshot.getValue();
                    userType = (String) hashUser.get("type");
                    Log.d("check", "type: " + userType);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}