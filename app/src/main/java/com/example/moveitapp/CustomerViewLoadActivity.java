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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerViewLoadActivity extends AppCompatActivity {

   // public static final String TAG = TAG;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseFirestoreSettings settings;
    DocumentSnapshot documentSnapshot;

    ListView lvDate, lvDestination;
    String email, password, name, userID;

    List<String> dateList, destinationList;
    ArrayAdapter<String> dateAdapter, destinationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_view_load);
        dateList = new ArrayList<String>();
        destinationList= new ArrayList<String>();
        lvDate = (ListView) findViewById(R.id.lv_dateTime);
        lvDestination = (ListView) findViewById(R.id.lv_destination);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
        name =  intent.getStringExtra("name");
        userID = intent.getStringExtra("userID");
        getSupportActionBar().setTitle("User: "+name.toUpperCase());

        //firebase connection
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        //userID = firebaseAuth.getCurrentUser().getUid();
       if(firebaseAuth.getCurrentUser() != null)
        {
            //Toast.makeText(getApplicationContext(), "Current user ID: " + user.getUid(), Toast.LENGTH_LONG).show();

                        final Task<QuerySnapshot> collection = firestore.collection("loads")
                                .whereEqualTo("UserID", user.getUid())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                       /* int counter =0;
                                        String documentID = "";*/

                                    for (QueryDocumentSnapshot document : task.getResult())
                                    {
                                       // loadsList.add(document.getId());
                                        dateList.add(document.getString("DateTime"));
                                        destinationList.add(document.getString("Destination"));

                                       // Toast.makeText(getApplicationContext(), "Current Load: " + dateList.get(counter), Toast.LENGTH_LONG).show();
                                        //counter++;
                                        dateAdapter = new ArrayAdapter<String>(
                                                getApplicationContext(),
                                                android.R.layout.simple_list_item_1, dateList
                                        );
                                        lvDate.setAdapter(dateAdapter);
                                        destinationAdapter = new ArrayAdapter<String>(
                                                getApplicationContext(),
                                                android.R.layout.simple_list_item_1, destinationList
                                        );
                                        lvDestination.setAdapter(destinationAdapter);


                                    }
                                    lvDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                            String loadDate = (String) ((TextView)view).getText();
                                            Toast.makeText(getApplicationContext(), "Load: " + loadDate, Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent (CustomerViewLoadActivity.this, CustomerLoadDetailsActivity.class );
                                            intent.putExtra("email", email);
                                            intent.putExtra("name", name);
                                            intent.putExtra("password", password);
                                            intent.putExtra("userID", user.getUid());
                                            intent.putExtra("loadDate", loadDate );
                                            startActivity(intent);
                                        }
                                    });



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
