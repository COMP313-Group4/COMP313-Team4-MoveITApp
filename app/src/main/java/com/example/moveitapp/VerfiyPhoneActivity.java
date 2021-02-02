package com.example.moveitapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.concurrent.TimeUnit;

public class VerfiyPhoneActivity extends AppCompatActivity {

    EditText one, two, three, four, five, six;
    Button btnVerify, btnResendCode;
    Boolean otpValid = true;

    TextView tvPhone;

    String phone;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    PhoneAuthCredential phoneAuthCredential;
    PhoneAuthProvider.ForceResendingToken token;
    String verificationID;
    FirebaseFirestoreSettings settings;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verfiy_phone);

        Intent intent = getIntent();
       // phone = intent.getStringExtra("phone");

       // tvPhone.setText(phone);




        one = (EditText) findViewById(R.id.otpOne);
        two = (EditText) findViewById(R.id.otpTwo);
        three = (EditText) findViewById(R.id.otpThree);
        four = (EditText) findViewById(R.id.otpFour);
        five = (EditText) findViewById(R.id.otpFive);
        six = (EditText) findViewById(R.id.otpSix);
        btnVerify = (Button) findViewById(R.id.btn_verify);
        btnResendCode = (Button) findViewById(R.id.btn_resendCode);


        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateField(one);
                validateField(two);
                validateField(three);
                validateField(four);
                validateField(five);
                validateField(six);

                if (otpValid){
                    // send otp to the user
                    String otp = one.getText().toString()+two.getText().toString()+three.getText().toString()+
                            four.getText().toString()+five.getText().toString()+six.getText().toString();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, otp);
                    //
                }
            }
        });

        // getting the database instance
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
                token = forceResendingToken;
                btnResendCode.setVisibility(View.GONE);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s){
                super.onCodeAutoRetrievalTimeOut(s);
                btnResendCode.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    verifyAuthentication(phoneAuthCredential);
                    btnResendCode.setVisibility(View.GONE);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(VerfiyPhoneActivity.this, "OTP Verification failed !", Toast.LENGTH_LONG).show();
            }
        };




        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resentOTP(phone);
            }
        });


    }


    // fields validation

    public void validateField(EditText field){
        if (field.getText().toString().isEmpty()){
            field.setError("Required");
            otpValid = false;
        } else {
            otpValid = true;
        }
    }

    // send OTP to the user method
    public void sentOTP(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, callbacks);
    }

    //
    public void resentOTP(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, callbacks, token);
    }

    // verify phone authentication
    public void verifyAuthentication(PhoneAuthCredential credential){
        firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(VerfiyPhoneActivity.this, "Verified Phone", Toast.LENGTH_LONG).show();
                // send to another activity
            }
        });
    }
}
