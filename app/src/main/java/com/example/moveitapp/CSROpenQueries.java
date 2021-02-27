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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CSROpenQueries extends AppCompatActivity{

    ListView lvOpenQueries;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    ArrayList<String> queryList;
    ArrayAdapter<String> queryAdapter;

    String staffID, queryID, customerID, title, messageBody, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_queries);

        lvOpenQueries = (ListView) findViewById(R.id.lv_openQueries);

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);


        staffID = firebaseAuth.getCurrentUser().getUid();
        queryList = new ArrayList<>();


        final Task<QuerySnapshot> collection = firestore.collection("OpenQueries")
                .whereEqualTo("CsrID", staffID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                // bring only the open cases; without solved queries
                                if (document.getString("Status").equals("Opened"))
                                {
                                    queryList.add(document.getString("QueryID"));
                                    queryID = document.getString("QueryID");
                                    customerID = document.getString("CustomerID");
                                    title = document.getString("Title");
                                    messageBody=document.getString("Body");
                                    status = document.getString("Status");


                                    // populating the listview
                                    queryAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, queryList
                                    );
                                    lvOpenQueries.setAdapter(queryAdapter);

                                    // setting up the itemclicklistner
                                    lvOpenQueries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            String queryID = (String) ((TextView)view).getText();

                                            DocumentReference document1= firestore.collection("OpenQueries").document(queryID);
                                            // document1.update("Status", "Opened");


                                            Intent intent = new Intent (getApplicationContext(), CSRChatActivity.class );
                                            intent.putExtra("queryID", queryID);
                                            intent.putExtra("customerID", customerID);
                                            intent.putExtra("staffID", staffID);
                                            intent.putExtra("title", title);
                                            intent.putExtra("body", messageBody);
                                            intent.putExtra("status", status);
                                            startActivity(intent);
                                        }
                                    });
                                }

                            } // end for loop

                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
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