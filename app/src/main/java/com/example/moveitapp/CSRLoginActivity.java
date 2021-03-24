package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class CSRLoginActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    //saving login credentials
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    EditText etEmail, etPassword;
    Button btnLogin;
    CheckBox checkBoxRemember;
    TextView tvMain;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);

        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvMain = (TextView) findViewById(R.id.tv_main);
        checkBoxRemember = (CheckBox) findViewById(R.id.check_box);

        // SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        checkSharedPreferences();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        // getting the database instance
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

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
                                        Log.d(TAG, "get failed with", task.getException());
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

                            Intent intent = new Intent(getApplicationContext(), CSRFeedActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: invalid email and/or password ", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    } // end onCreate

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
    }   //  checkSharedPreferencesEnd

    // move to Main Portal page
    public void main(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplication(), MainActivity.class));
        finish();
    }   //  mainEnd
}   //  classEnd