package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverFeedActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private static final int GALLERY_INTENT_CODE = 1023 ;
    FirebaseAuth fAuth;
    String userId;
    String loadId;
    Button viewLoadDetailsBtn;
    ProgressBar progressBar;
    FirebaseUser user;
    RecyclerView recyclerView;
    ArrayList<Load> loadsArrayList;
    RecyclerAdapter recyclerAdapter;
    Button resendCode;
    TextView verifyMsg;

    Button btnLocaiton;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    Button btnViewDetails;
    Button btnDeliveredLoads;

    ListView lvLoads;
    ArrayList<String> loads = new ArrayList<>();

    ArrayAdapter<String> loadsAdapter;
    String loadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_feed);
        fAuth = FirebaseAuth.getInstance();
        btnLocaiton = (Button) findViewById(R.id.btn_location);
        btnDeliveredLoads = (Button) findViewById(R.id.btn_deliveredLoads);
        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);


        lvLoads= (ListView) findViewById(R.id.lv_loads);



        userId = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();



        //Firebase
        loadsArrayList = new ArrayList<>();
        //Clear previous List
        ClearAll();
        //Get Data Method
        btnViewDetails = (Button) findViewById(R.id.btn_viewDetails);

        if(firebaseAuth.getCurrentUser() != null)
        {
          final Task<QuerySnapshot> collection = firestore.collection("loads")
                    .whereEqualTo("Status", "Not Booked")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int counter =0;
                                String documentID = "";

                                for (QueryDocumentSnapshot document : task.getResult())
                                {
                                    // loadsList.add(document.getId());
                                    loadID = document.getId();
                                    loads.add("Post Date: "+document.getString("DateTime") +
                                            "\nVehicle Wanted: "+document.getString("Vehicle Wanted"));
                                    counter++;
                                    loadsAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, loads
                                    );
                                    lvLoads.setAdapter(loadsAdapter);

                                    lvLoads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            String loadDate = (String) ((TextView)view).getText();
                                            Toast.makeText(getApplicationContext(), "Load: " + loadDate, Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent (getApplicationContext(), DriverLoadDetailsActivity.class );
                                            intent.putExtra("loadID", loadID);
                                            startActivity(intent);
                                        }
                                    });
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }

                    });

        }


        btnDeliveredLoads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverFeedActivity.this, DriverDeliveredLoadsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLocaiton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverFeedActivity.this, DriverLocation.class);
                startActivity(intent);
                finish();
            }
        });
    }   //  onCreateEnd

    private void getDataFromFirebase(){
        firestore = FirebaseFirestore.getInstance();
        final Task<QuerySnapshot> collection = firestore.collection("loads")
                .whereEqualTo("Status", "Not Booked")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int counter =0;
                            String documentID = "";

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Load load = new Load();
                                load.setCategory(document.get("Category").toString());
                                load.setDateTime(document.get("DateTime").toString());
                                load.setDeliveryFees((Double) document.get("Delivery Fees"));
                                load.setDestination(document.get("Destination").toString());
                                load.setPickup(document.get("Pickup").toString());
                                load.setStatus(document.get("Status").toString());
                                load.setVehicleWanted(document.get("Vehicle Wanted").toString());
                                load.setUserID(document.get("UserID").toString());
                                load.setWeight((Double) document.get("Weight"));
                                loadsArrayList.add(load);

                                recyclerAdapter = new RecyclerAdapter(getApplicationContext(), loadsArrayList);
                                recyclerView.setAdapter(recyclerAdapter);
                                //recyclerAdapter.notifyDataSetChanged();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                        Toast.makeText(getApplicationContext(), "counts: "+loadsArrayList.size(), Toast.LENGTH_LONG).show();
                    }

                });

    }   //  getDataFromFirebase

    private void ClearAll(){
        if(loadsArrayList !=null){
            loadsArrayList.clear();
            if(recyclerAdapter != null){
                recyclerAdapter.notifyDataSetChanged();
            }
        }
        else{
            loadsArrayList = new ArrayList<>();
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

}