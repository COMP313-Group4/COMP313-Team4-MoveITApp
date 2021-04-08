package com.example.moveitapp;

import  androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Driver;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverChatActivity extends AppCompatActivity{
    public static final String TAG = "TAG";
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    Button btnSendMessage, btnGoHome;
    TextView tvMessages, tvTitle;
    EditText etMessage, etTitle;

    String userID, queryID=null, messageBody, email, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_chat);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        queryID = intent.getStringExtra("queryID");
        status = intent.getStringExtra("status");

        btnSendMessage = (Button) findViewById(R.id.btn_sendMessage);

        tvMessages = (TextView) findViewById(R.id.tv_messages);
        etMessage = (EditText) findViewById(R.id.et_message);
        etTitle = (EditText) findViewById(R.id.et_title);
        tvTitle=(TextView) findViewById(R.id.tv_title);

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        if (queryID != null)
        {
            DocumentReference ref = null;
            etTitle.setVisibility(View.GONE);
            tvTitle.setVisibility(View.GONE);
            if (status.equals("Submitted")){
                ref = firestore.collection("queries").document(queryID);
            } else if (status.equals("Opened"))
            {
                ref = firestore.collection("OpenQueries").document(queryID);
            }
            else if (status.equals("Solved")){
                ref = firestore.collection("SolvedQueries").document(queryID);
                etMessage.setVisibility(View.GONE);
                btnSendMessage.setVisibility(View.GONE);
            }
            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapShot Data: " + document.getData());

                            Map<String, Object> queryData = document.getData();
                            messageBody = (String) queryData.get("Body");
                            tvMessages.setText(messageBody);
                            etMessage.setText("");
                            // Intent intent = new Intent (getApplicationContext(), DriverChatActivity.class);

                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with: ", task.getException());
                    }
                }
            });
        }

        btnSendMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                userID = firebaseAuth.getCurrentUser().getUid();
                DocumentReference ref2=null;
                // if this is a new query
                if (queryID == null) {
                    if (!etMessage.getText().toString().equals("") && !etTitle.getText().toString().equals("")) {
                        ref2 = firestore.collection("queries").document();
                        queryID = ref2.getId();
                        Map<String, Object> query = new HashMap<>();
                        query.put("Status", "Submitted"); // Submitted, Opened, Resolved
                        query.put("QueryID", ref2.getId());
                        query.put("CustomerID", userID);
                        query.put("CsrID", "");
                        query.put("Body", "Driver: " + etMessage.getText().toString());
                        query.put("Last Update", getCurrentDateTime());
                        query.put("Title", etTitle.getText().toString());
                        ref2.set(query).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: The query successfully added for " + userID);
                                etMessage.setText("");
                                tvMessages.append(etMessage.getText().toString());
                                etTitle.setVisibility(View.GONE);
                                tvTitle.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });
                        // update the body after clicking on send button
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(DriverChatActivity.this);
                        builder.setTitle("Fields cannot be empty");
                        builder.setMessage("Please enter the title or the message").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.create().show();
                    }
                }
                else {
                    if(!etMessage.getText().toString().equals("")) {
                        if (status.equals("Submitted")){
                            ref2 = firestore.collection("queries").document(queryID);
                        } else if (status.equals("Opened"))
                        {
                            ref2 = firestore.collection("OpenQueries").document(queryID);
                        }
                        else if (status.equals("Solved")){
                            ref2 = firestore.collection("SolvedQueries").document(queryID);
                        }
                        // if the query exists
                        ref2.update("Body", messageBody + "\nDriver: "+etMessage.getText().toString());
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
                                        // Intent intent = new Intent (getApplicationContext(), CustomerChatActivity.class);

                                    } else {
                                        Log.d("TAG", "No such document");
                                    }
                                } else {
                                    Log.d("TAG", "get failed with: ", task.getException());
                                }
                            }
                        });
                    }else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DriverChatActivity.this);
                        builder.setTitle("Fields cannot be empty");
                        builder.setMessage("Please enter the message").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.create().show();
                    }
                }
            }
        });
    }

    //get date and time
    public static String getCurrentDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy : HH:mm:ss");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }



    public  void sendMessage (){
        DocumentReference ref = firestore.collection("queries").document();
        Map<String, Object> query = new HashMap<>();
        query.put("QueryID", ref.getId());
        query.put("DriverID", userID);
        query.put("CsrID", "");
        query.put("Body", etMessage.getText().toString());
        query.put("Last Update", getCurrentDateTime());

        ref.set(query).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: The load successfully posted for "+ userID  );

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+ e.toString());
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
