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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerQueriesActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;

    ListView lvTitle, lvQueryID;
    List<String> titleList, queryIDList;
    ArrayAdapter<String> titleAdapter, queryIDAdapter;

    Button btnOpenQueryForm;
    String userID, email, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_queries);


        Intent intent = getIntent();
         email = intent.getStringExtra("email");

        lvTitle = (ListView) findViewById(R.id.lv_title);
        lvQueryID = (ListView) findViewById(R.id.lv_queryID);
        btnOpenQueryForm = (Button) findViewById(R.id.btn_openQueryForm);

        titleList = new ArrayList<String>();
        queryIDList = new ArrayList<>();

        btnOpenQueryForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CustomerChatActivity.class);
                startActivity(intent);
            }
        });

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        userID = firebaseAuth.getCurrentUser().getUid();
        if (firebaseAuth.getCurrentUser() != null)
        {
            Task<QuerySnapshot> collection = firestore.collection("queries")
                    .whereEqualTo("CustomerID", firebaseAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult())
                                {
                                    titleList.add(document.getString("Title"));
                                    queryIDList.add(document.getId());
                                    status = document.getString("Status");

                                    titleAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, titleList
                                    );
                                    lvTitle.setAdapter(titleAdapter);

                                    queryIDAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1, queryIDList
                                    );
                                    lvQueryID.setAdapter(queryIDAdapter);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    lvQueryID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            String queryId = (String) ((TextView)view).getText();

                            Intent intent = new Intent (getApplicationContext(), CustomerChatActivity.class );
                            intent.putExtra("queryID", queryId);
                            intent.putExtra("email", email);
                            intent.putExtra("status", status);
                            startActivity(intent);
                        }
                    });
        }
    } // end onCreate



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }   //  onCreateOptionsMenuEnd

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }   //  onOptionsItemSelectedEnd
}   //  classEnd
