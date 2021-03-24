package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CSRChatActivity extends AppCompatActivity {

    public static final String TAG = "TAG";

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    Button btnSendMessage, btnSolved;
    EditText etMessage;
    TextView tvTitle, tvMessages, tvSolveDate;

    String queryID, messageBody, staffID, customerID, title, status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_chat);

        Intent intent = getIntent();
        queryID = intent.getStringExtra("queryID");
        staffID = intent.getStringExtra("staffID");
        customerID = intent.getStringExtra("customerID");
        status = intent.getStringExtra("status");

        btnSendMessage = (Button) findViewById(R.id.btn_sendMessage);
        btnSolved = (Button) findViewById(R.id.btn_solved);
        tvMessages = (TextView) findViewById(R.id.tv_messages);
        tvSolveDate = (TextView) findViewById(R.id.tv_solveDate);
        etMessage = (EditText) findViewById(R.id.et_message);
        tvTitle=(TextView) findViewById(R.id.tv_title);


        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        if (queryID != null && status.equals("Opened"))
        {
            DocumentReference ref = firestore.collection("OpenQueries").document(queryID);
            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapShot Data: " + document.getData());
                            Map<String, Object> queryData = document.getData();
                            messageBody = (String) queryData.get("Body");
                            tvTitle.append((String) queryData.get("Title"));
                            title = queryData.get("Title").toString();
                            tvMessages.setText(messageBody);
                            etMessage.setText("");
                            etMessage.setEnabled(true);
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with: ", task.getException());
                    }
                }
            });
        } // end if

        if (queryID != null && status.equals("Solved"))
        {
            DocumentReference ref = firestore.collection("SolvedQueries").document(queryID);
            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapShot Data: " + document.getData());
                            Map<String, Object> queryData = document.getData();
                            messageBody = (String) queryData.get("Body");
                            tvTitle.append((String) queryData.get("Title"));
                            title = queryData.get("Title").toString();
                            tvMessages.setText(messageBody);
                            etMessage.setText("");
                            etMessage.setVisibility(View.GONE);
                            btnSendMessage.setVisibility(View.GONE);
                            btnSolved.setVisibility(View.GONE);
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with: ", task.getException());
                    }
                }
            });
        } // end if



        btnSendMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // if the query exists
                if(!etMessage.getText().toString().equals("")) {
                    DocumentReference ref2 = firestore.collection("OpenQueries").document(queryID);
                    ref2.update("Body", messageBody + "\nStaff: "+etMessage.getText().toString());
                    etMessage.setText("");
                    ref2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapShot Data: " + document.getData());

                                    Map<String, Object> queryData = document.getData();
                                    messageBody = (String) queryData.get("Body");
                                    tvMessages.setText(messageBody);
                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with: ", task.getException());
                            }
                        }
                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CSRChatActivity.this);
                    builder.setTitle("Fields cannot be empty");
                    builder.setMessage("Please enter the message").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create().show();
                }
            }
        }); // end btnSendMessage

        btnSolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference ref2 = firestore.collection("OpenQueries").document(queryID);
                ref2.update("Status", "Solved");
                ref2.update("Last Update", getCurrnetDateTime());

                DocumentReference ref3 = firestore.collection("queries").document(queryID);
                ref3.update("Status", "Solved");
                ref3.update("Last Update", getCurrnetDateTime());

                DocumentReference ref= firestore.collection("SolvedQueries").document(queryID);
                // queryID = ref2.getId();
                Map<String, Object> query = new HashMap<>();
                query.put("QueryID",queryID);
                query.put("Title", title);
                query.put("CsrID", staffID);
                query.put("CustomerID", customerID);
                query.put("Body", messageBody);
                query.put("Last Update", getCurrnetDateTime());
                query.put("Status", "Solved"); // Submitted, Opened, Resolved

                ref.set(query).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: The query successfully opened for "+ staffID  );
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: "+ e.toString());
                    }
                });
                etMessage.setEnabled(false);
                tvSolveDate.setText("This query was solved on this date: "+ getCurrnetDateTime());
                etMessage.setVisibility(View.GONE);
                btnSendMessage.setVisibility(View.GONE);
                btnSolved.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), CSRFeedActivity.class);
                startActivity(intent);
            }
        });

    } // end onCreate

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
