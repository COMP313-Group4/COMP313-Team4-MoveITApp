package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1beta1.Document;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerFeedActivity extends AppCompatActivity {

    public static final String TAG = "TAG";

    Button btnPostLoad, btnViewLoads, btnViewQueries;
    ListView lvCustomer;

    Button btnLocaiton;
    Button resendCode;
    TextView verifyMsg;

    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;
    DocumentSnapshot documentSnapshot;

    String userId;
    String name, email, password;
    List<String> loadsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_feed);

        // getting the email and password
        Intent intent = getIntent();
        email = intent.getStringExtra("email").toString();
        password = intent.getStringExtra("password").toString();


        btnPostLoad = (Button) findViewById(R.id.btn_postLoad);
        btnLocaiton = (Button) findViewById(R.id.btn_location);
        btnViewLoads= (Button) findViewById(R.id.btn_viewLoads);
        btnViewQueries = (Button) findViewById(R.id.btn_viewQueries);
        resendCode = findViewById(R.id.resendCode);
        verifyMsg = findViewById(R.id.verifyMsg);

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        userId = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();

        if(!user.isEmailVerified()){
            verifyMsg.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                        }
                    }); //  sendVerificationLink
                }   //  onClick
            }); //  resendCode  =>  Send email verification code
        }   //  Email verification


        if(firebaseAuth.getCurrentUser() != null)
        {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        userId = firebaseAuth.getCurrentUser().getUid();

                        DocumentReference ref = firestore.collection("users").document(userId);
                        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapShot Data: " + document.getData());

                                        Map<String, Object> userData = document.getData();
                                        name = (String) userData.get("Name");
                                        //Toast.makeText(getApplicationContext(), "User details: "+ name, Toast.LENGTH_LONG).show();
                                        getSupportActionBar().setTitle("User: "+name.toUpperCase());

                                    } else {
                                        Log.d("TAG", "No such document");
                                    }
                                } else {
                                    Log.d("TAG", "get failed with: ", task.getException());
                                }
                            }
                        });
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        String email = user.getEmail();
                        String phone = user.getPhoneNumber();
                       // Toast.makeText(CustomerFeedActivity.this, "User: "+user.getUid(), Toast.LENGTH_SHORT).show();
                        DocumentReference documentReference = firestore.collection("loads").document();
                        Map<String, Object> load = new HashMap<>();
                    }
                }
            });
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(firebaseAuth.getCurrentUser() != null)
        {
            Toast.makeText(getApplicationContext(), "Current user ID: " + user.getUid(), Toast.LENGTH_LONG).show();

            Task<QuerySnapshot> collection = firestore.collection("loads")
                    .whereEqualTo("UserID", user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                    int counter =0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    loadsList.add(document.getId());
                                   // Toast.makeText(getApplicationContext(), "Current Load: " + loadsList.get(counter), Toast.LENGTH_LONG).show();
                                    counter++;
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }

                    });
        }
        for (String i : loadsList){ // loadslist here is empty
            Toast.makeText(getApplicationContext(), "Current Load: " + i, Toast.LENGTH_LONG).show();
        }

        btnPostLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (CustomerFeedActivity.this, PostLoadActivity.class );
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                intent.putExtra("userID", userId);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });

        btnViewLoads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (CustomerFeedActivity.this, CustomerViewLoadActivity.class );
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                intent.putExtra("password", password);
                intent.putExtra("userID", userId);
                startActivity(intent);
            }
        });
        btnViewQueries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (CustomerFeedActivity.this, CustomerQueriesActivity.class );
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                intent.putExtra("userID", userId);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });
//        btnViewQueries.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent (CustomerFeedActivity.this, CustomerQueriesActivity.class );
//                intent.putExtra("email", email);
//                intent.putExtra("name", name);
//                intent.putExtra("userID", userId);
//                intent.putExtra("password", password);
//                startActivity(intent);
//            }
//        });





        btnLocaiton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerFeedActivity.this, DriverLocation.class);
                startActivity(intent);
                finish();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}
