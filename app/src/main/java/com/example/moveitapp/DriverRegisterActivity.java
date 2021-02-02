package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverRegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    Spinner spinnerVehicle;
    List<String> vehicleList= new ArrayList<String>();

    EditText etName, etEmail, etPassword, etConfirmPassword, etPhone, etLicensePlate, etAddress;

    TextView tvLogin, tvMain;

    Button btnRegister;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    FirebaseFirestoreSettings settings;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        // getting the database instance
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        etName = (EditText) findViewById(R.id.et_driverName);
        etAddress = (EditText) findViewById(R.id.et_driverAddress);
        etEmail = (EditText) findViewById(R.id.et_driverEmail);
        etPassword = (EditText) findViewById(R.id.et_driverPassword);
        etConfirmPassword = (EditText) findViewById(R.id.et_driverConfirmPassword);
        etPhone = (EditText) findViewById(R.id.et_driverPhone);
        etLicensePlate=(EditText) findViewById(R.id.et_licensePlate);
        spinnerVehicle = (Spinner) findViewById(R.id.spinner_truckCategory);
        btnRegister = (Button) findViewById(R.id.btn_driverRegister);
        tvLogin = (TextView) findViewById(R.id.tv_driverLogin);
        tvMain = (TextView) findViewById(R.id.tv_driverMain);

        //populating the spinner
        vehicleList= addItemsOnSpinner();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = etEmail.getText().toString().trim();
                final String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                final String fullName = etName.getText().toString();
                final String address = etAddress.getText().toString().trim();
                final String phone = etPhone.getText().toString().trim();
                final String licensePlate = etLicensePlate.getText().toString().trim();
                final String vehicle = spinnerVehicle.getSelectedItem().toString().trim();

                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Error: email required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etPassword.setError("Error: password required!");
                    return;
                }
                if(!password.equals(confirmPassword)){
                    etConfirmPassword.setError("Password is different");
                    return;
                }
                if(password.length() <6){
                    etPassword.setError("Password must be >= 6 characters");
                    return;
                }

               // progressBar.setVisibility(View.VISIBLE);

                // if the user is already registered
                if(firebaseAuth.getCurrentUser() !=null){
                    startActivity(new Intent(getApplication(), DriverLoginActivity.class));
                    finish();
                }

                // register the user in the firebase
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(DriverRegisterActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                            userID = firebaseAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("drivers").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("Name", fullName);
                            user.put("Email", email);
                            user.put("Phone", phone);
                            user.put("Address", address);
                            user.put("Password", password);
                            user.put("Vehicle Type", vehicle);
                            user.put("License Plate", licensePlate);
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

                            startActivity(new Intent(getApplication(), DriverLoginActivity.class));
                        } else {
                            Toast.makeText(DriverRegisterActivity.this, "Error: "+task.getException(), Toast.LENGTH_SHORT).show();
                          //  progressBar.setVisibility(View.GONE);
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




    } // enc onCreate



    // to login page
    public void login(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplication(), DriverLoginActivity.class));
        finish();
    }

    // back to main portal
    public void main(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplication(), MainActivity.class));
        finish();
    }

    public List<String> addItemsOnSpinner(){
        spinnerVehicle = (Spinner) findViewById(R.id.spinner_truckCategory);

        List<String> list = new ArrayList<String>();
        list.add("Choose Vehicle");
        list.add("SUV Van");
        list.add("15ft Truck");
        list.add("25ft Truck");
        list.add("35ft Truck");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, list
        );
        spinnerVehicle.setAdapter(arrayAdapter);
        return list;
    }

}

