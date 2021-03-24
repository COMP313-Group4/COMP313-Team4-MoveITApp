package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CustomerLoginActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    public static final String TAG1 = "TAG";
    EditText etEmail, etPassword;
    Button btnLogin;
    CheckBox checkBoxRemember;
    TextView tvRegister, tvResetPassword;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    FirebaseFirestoreSettings settings;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    PhoneAuthProvider.ForceResendingToken token;
    String verificationID;
    String userPhone;

    //saving login credentials
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private int loginCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        tvRegister = (TextView) findViewById(R.id.tv_register);
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvResetPassword=(TextView) findViewById(R.id.tv_customerResetPassword);
        checkBoxRemember = (CheckBox) findViewById(R.id.check_box);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s){
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                verifyAuthentication(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(CustomerLoginActivity.this, "OTP Verification failed !", Toast.LENGTH_LONG).show();
            }
        };


        // SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        checkSharedPreferences();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String email = etEmail.getText().toString().trim();
                final String password = etPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Error: email required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etEmail.setError("Error: password required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etEmail.setError("Error: confirm password");
                    return;
                }
                if(password.length() <6){
                    etPassword.setError("Password must be >= 6 characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                // authenticate the user
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                           String userID = firebaseAuth.getCurrentUser().getUid();

                            DocumentReference documentReference = firestore.collection("users").document(userID);

                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                    } else {
                                        Log.d(TAG1, "get failed with", task.getException());
                                    }
                                }
                            });

                            // checking if the checkbox is ticked
                            if (checkBoxRemember.isChecked()){
                                // setting the checbox as Checked
                                editor.putString(getString(R.string.checkbox), "True");
                                editor.commit();
                                // saving the username
                                editor.putString(getString(R.string.username), email);
                                editor.commit();
                                // saving the password
                                editor.putString(getString(R.string.password), password);
                                editor.commit();
                            }
                            else{
                                editor.putString(getString(R.string.checkbox), "False");
                                editor.commit();
                                editor.putString(getString(R.string.username), "");
                                editor.commit();
                                editor.putString(getString(R.string.password), "");
                                editor.commit();
                            }

                            Intent intent = new Intent(CustomerLoginActivity.this, CustomerFeedActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        } else {
                            Toast.makeText(CustomerLoginActivity.this, "Error: invalid email and/or password ", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordRestDialog = new AlertDialog.Builder((v.getContext()));
                passwordRestDialog.setTitle("Reset Password");
                passwordRestDialog.setMessage("Enter your email to receive the Password Reset Link");
                passwordRestDialog.setView(resetMail);

                passwordRestDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send the reset link
                        String email = resetMail.getText().toString();

                        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CustomerLoginActivity.this, "Password Reset Link sent to your email . . . ", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CustomerLoginActivity.this, "Error: Reset Link not sent ! "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                passwordRestDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });
                passwordRestDialog.create().show();
            }
        });

    }

    // move to Main Portal page
    public void main(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplication(), MainActivity.class));
        finish();
    }

    // move to Register page
    public void register (View view){
        startActivity(new Intent(getApplicationContext(), CustomerRegisterActivity.class));
        finish();
    }

    // saving the credentials
    private void checkSharedPreferences(){
        String checkBox = preferences.getString(getString(R.string.checkbox), "False");
        String name = preferences.getString(getString(R.string.username), "");
        String password = preferences.getString(getString(R.string.password), "");

        etEmail.setText(name);
        etPassword.setText(password);

        if (checkBox.equals("True")){
            checkBoxRemember.setChecked(true);
        }
        else{
            checkBoxRemember.setChecked(false);
        }
    }

    // send OTP to the user method
    public void sendOTP(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, callbacks);
    }

    // verify phone authentication
    public void verifyAuthentication(PhoneAuthCredential credential){
        firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(CustomerLoginActivity.this, "Verified Phone", Toast.LENGTH_LONG).show();
                // send to another activity
            }
        });
    }

}
