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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.model.DocumentCollections;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverLoadDetailsActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    String email, password, name, userID, loadDate;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    ListView lvDetails;
    List<String> loadDetails = new ArrayList<>();
    ArrayAdapter<String> detailsAdapter;
    String loadID;

    Button btnAcceptLoad, btnConfirmDelivery;
    TextView tvDelivered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_load_details);

        Intent intent = getIntent();
        loadID = intent.getStringExtra("loadID");
        btnAcceptLoad = (Button) findViewById(R.id.btn_acceptLoad);
        btnConfirmDelivery = (Button) findViewById(R.id.btn_confirmDelivery);

        lvDetails = (ListView) findViewById(R.id.lv_details);
        tvDelivered= (TextView) findViewById(R.id.tv_delivered);

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        final FirebaseUser user = firebaseAuth.getCurrentUser();


        btnAcceptLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference ref = firestore.collection("loads").document(loadID);
                ref.update("Status", "Booked");
                ref.update("Driver ID", user.getUid());
                btnConfirmDelivery.setVisibility(View.VISIBLE);
                btnAcceptLoad.setVisibility(View.INVISIBLE);

            }
        });
        btnConfirmDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference ref = firestore.collection("loads").document(loadID);
                ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapShot Data: " + document.getData());
                                /*Map<String, Object> loadData = document.getData();
                                loadData.put("Status", "Delivered");*/
                                Map<String, Object> load2 = new HashMap<>();
                                load2.put("Driver ID", user.getUid());
                                load2.put("Delivery Fees", document.get("Delivery Fees"));
                                load2.put("Pickup", document.get("Pickup"));
                                load2.put("Destination", document.get("Destination"));
                                load2.put("Category", document.get("Category"));
                                load2.put("Weight", document.get("Weight"));
                                load2.put("Vehicle Wanted", document.get("Vehicle Wanted"));
                                load2.put("Post Date & Time", document.get("DateTime"));
                                load2.put("Delivery DateTime", getCurrnetDateTime());
                                DocumentReference documentReference = firestore.collection("DeliveredLoads").document();
                                documentReference.set(load2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: The load successfully added to your delivered loads list");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: "+ e.toString());
                                    }
                                });
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with: ", task.getException());
                        }
                    }
                });
                ref.update("Status", "Delivered");
                tvDelivered.setVisibility(View.VISIBLE);
                Intent intent1 = new Intent(getApplicationContext(), DriverFeedActivity.class);
                startActivity(intent1);
            }
        });


        if(firebaseAuth.getCurrentUser() != null) {
            DocumentReference ref = firestore.collection("loads").document(loadID);
            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapShot Data: " + document.getData());

                            Map<String, Object> userData = document.getData();
                           // name = (String) userData.get("Name");
                           // loadDetails.add(loadID);
                            loadDetails.add(userData.get("Category").toString());
                            loadDetails.add(userData.get("DateTime").toString());
                            loadDetails.add(userData.get("Delivery Fees").toString());
                            loadDetails.add(userData.get("Pickup").toString());
                            loadDetails.add(userData.get("Destination").toString());
                            loadDetails.add(userData.get("Vehicle Wanted").toString());
                            loadDetails.add(userData.get("Weight").toString());

                            detailsAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    android.R.layout.simple_list_item_1, loadDetails
                            );
                            lvDetails.setAdapter(detailsAdapter);
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with: ", task.getException());
                    }
                }
            });
        }
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

    //get date and time
    public static String getCurrnetDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy : HH:mm:ss");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }
}