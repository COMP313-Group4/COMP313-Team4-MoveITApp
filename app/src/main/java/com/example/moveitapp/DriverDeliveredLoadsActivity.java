package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverDeliveredLoadsActivity extends AppCompatActivity {

    ListView lvDeliveredLoads;
    Button btnHome;

    List<String> loadsArrayList;
    ArrayAdapter<String> loadsAdapter;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    FirebaseUser user;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivered_loads);

        lvDeliveredLoads= (ListView) findViewById(R.id.lv_deliveredLoads);
        btnHome = (Button) findViewById(R.id.btn_btnHome);
        loadsArrayList = new ArrayList<>();

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), DriverFeedActivity.class);
                startActivity(intent1);
            }
        });

        userId = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();

        if(firebaseAuth.getCurrentUser() != null)
        {
          final Task<QuerySnapshot> collection = firestore.collection("DeliveredLoads")
                    .whereEqualTo("Driver ID", userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult())
                                {
                                  loadsArrayList.add(
                                          document.getString("Pickup") + "       " + document.getString("Destination") +"\n"
                                  +document.getString("Post Date & Time") + "      " +document.getString("Delivery DateTime"));
                                    loadsAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, loadsArrayList
                                    );
                                    lvDeliveredLoads.setAdapter(loadsAdapter);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
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
}