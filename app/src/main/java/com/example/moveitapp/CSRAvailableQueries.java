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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CSRAvailableQueries extends AppCompatActivity {
    public static final String TAG = "TAG";
    public static final String TAG1 = "TAG";
    ListView lvQueries;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    ArrayList<String> queryList;
    ArrayAdapter<String> queryAdapter;

    String staffID, queryID, customerID, bodyMessages, title, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_queries);

        lvQueries = (ListView) findViewById(R.id.lv_queries);

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);


        staffID = firebaseAuth.getCurrentUser().getUid();
        queryList = new ArrayList<>();


        final Task<QuerySnapshot> collection = firestore.collection("queries")
                .whereEqualTo("Status", "Submitted")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                // loadsList.add(document.getId());
                                // queryID = document.getId();
                                queryList.add(document.getString("QueryID"));
                                customerID = document.getString("CustomerID");
                                bodyMessages = document.getString("Body");
                                title = document.getString("Title");
                                queryAdapter = new ArrayAdapter<String>(
                                        getApplicationContext(),
                                        android.R.layout.simple_list_item_1, queryList
                                );
                                lvQueries.setAdapter(queryAdapter);

                                lvQueries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        String queryID = (String) ((TextView)view).getText();

                                        // queries collection
                                        DocumentReference ref = firestore.collection("queries").document(queryID);
                                        ref.update("Status", "Opened");
                                        ref.update("Last Update", getCurrnetDateTime());
                                        ref.update("CsrID", staffID);
                                        status = "Opened";

                                        // openedQueries collection
                                        DocumentReference ref2= firestore.collection("OpenQueries").document(queryID);
                                        // queryID = ref2.getId();
                                        Map<String, Object> query = new HashMap<>();
                                        query.put("Status", "Opened"); // Submitted, Opened, Resolved
                                        query.put("QueryID",queryID);
                                        query.put("CsrID", staffID);
                                        query.put("CustomerID", customerID);
                                        query.put("Body", bodyMessages);
                                        query.put("Last Update", getCurrnetDateTime());
                                        query.put("Title", title);
                                        ref2.set(query).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: The query successfully opened for "+ staffID  );
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG1, "onFailure: "+ e.toString());
                                            }
                                        });

                                        Intent intent = new Intent (getApplicationContext(), CSRChatActivity.class );
                                        intent.putExtra("queryID", queryID);
                                        intent.putExtra("staffID", staffID);
                                        intent.putExtra("customerID", customerID);
                                        intent.putExtra("status", "Opened");
                                        startActivity(intent);
                                    }
                                });
                            } // end for loop

                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }

                });


    }

    //get date and time
    public static String getCurrnetDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy : HH:mm:ss");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
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