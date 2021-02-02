package com.example.moveitapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import android.support.v7.app.AppCompatActivity;

public class CustomerRegisterActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    //  DatabaseHelper db;
    EditText etEmail, etPassword, etConfirmPassword, etName, etAddress, etPhone,codeEnter;
    Button btnRegister;
    TextView tvLogin, tvMain,state; // not used

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    FirebaseFirestoreSettings settings;
    CountryCodePicker codePicker;
    String verifiactionId;
    PhoneAuthProvider.ForceResendingToken token;
    Boolean verificationInProgress = false;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        etName = (EditText) findViewById(R.id.et_customerName);
        etAddress = (EditText) findViewById(R.id.et_customerAddress);
        etEmail = (EditText) findViewById(R.id.et_customerEmail);
        etPassword = (EditText) findViewById(R.id.et_customerPassword);
        etConfirmPassword = (EditText) findViewById(R.id.et_customerConfirmPassword);
        etPhone = (EditText) findViewById(R.id.et_customerPhone);
        btnRegister = (Button) findViewById(R.id.btn_customerRegister);
        codePicker = findViewById(R.id.ccp);
        codeEnter = findViewById(R.id.codeEnter);
        state = findViewById(R.id.state);

        //tvLogin = (TextView) findViewById(R.id.tv_customerLogin);



        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                final String fullName = etName.getText().toString();
                final String address = etAddress.getText().toString().trim();
                final String phone = "+"+codePicker.getSelectedCountryCode()+etPhone.getText().toString();


                    if (TextUtils.isEmpty(email)) {
                        etEmail.setError("Error: email required!");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        etEmail.setError("Error: password required!");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        etEmail.setError("Error: confirm password");
                        return;
                    }
                    if (password.length() < 6) {
                        etPassword.setError("Password must be >= 6 characters");
                        return;
                    }


                progressBar.setVisibility(View.VISIBLE);

                // if the user is already registered
                if(firebaseAuth.getCurrentUser() !=null){
                    startActivity(new Intent(getApplication(), CustomerLoginActivity.class));
                    finish();
                }

                // register the user in the firebase
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(CustomerRegisterActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                            userID = firebaseAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("Name", fullName);
                            user.put("Email", email);
                            user.put("Phone", phone);
                            user.put("Address", address);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user profile successfully filled for "+ userID  );
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: "+ e.toString());
                                }
                            });

                            startActivity(new Intent(getApplication(), CustomerLoginActivity.class));
                        } else {
                            Toast.makeText(CustomerRegisterActivity.this, "Error: "+task.getException(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        // getting the database instance
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

    }



    // to login page
    public void login(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplication(), CustomerLoginActivity.class));
        finish();
    }

    // back to main portal
    public void main(View view){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplication(), MainActivity.class));
            finish();
    }
}