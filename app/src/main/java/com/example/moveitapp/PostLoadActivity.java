package com.example.moveitapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/*
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;*/

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostLoadActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    String userName;

     Calendar calendar;
    SimpleDateFormat simpleDateFormat;
     String date;

    Spinner spinnerCategory, spinnerVehicle, spinnerWeight;
    EditText etPickup, etDestination, etWeight;

    TextView tvMilage, tvFees, tvOk;

    Button btnReset, btnPostLoad, btnCalculateMilage, btnCalculateFees;

    String pickup="", destination="", category="", vehicle="";
    double weight = 0;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    FirebaseFirestoreSettings settings;

    String userID;
    double totalFees =0;
    List<String> vehicleList= new ArrayList<String>();

    String email, password, name;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_load);

        // getting the email and password
        Intent intent = getIntent();
        email = intent.getStringExtra("email").toString();
        password = intent.getStringExtra("password").toString();
        name =  intent.getStringExtra("name").toString();
        getSupportActionBar().setTitle("User: "+name.toUpperCase());

        etPickup = (EditText) findViewById(R.id.tv_pickup);
        etDestination = (EditText) findViewById(R.id.tv_destination);
        etWeight = (EditText) findViewById(R.id.et_weight);
        tvMilage = (TextView) findViewById(R.id.tv_milage);
        tvFees = (TextView) findViewById(R.id.tv_fees);
        tvOk = (TextView) findViewById(R.id.tv_ok);
        btnCalculateMilage = (Button) findViewById(R.id.btn_calculateMilage);
        btnCalculateFees = (Button) findViewById(R.id.btn_calculateFees);
        btnReset = (Button) findViewById(R.id.btn_resetValues);
        btnPostLoad = (Button) findViewById(R.id.btn_postLoad);
        spinnerCategory = (Spinner) findViewById(R.id.spinner_category);
        spinnerVehicle = (Spinner) findViewById(R.id.spinner_truck);
       // spinnerWeight = (Spinner) findViewById(R.id.spinner_weight);
        //Initialize places
        Places.initialize(getApplicationContext(), "AIzaSyBCRypkpM0GIfFscExguwBNgQkgLJXODms");
        //Setting EditText as non focusable

        etDestination.setFocusable(false);
        etPickup.setFocusable(false);
        etDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Initialising place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS
                        ,Place.Field.LAT_LNG,Place.Field.NAME);
                //Create intent
                Intent intent= new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY
                        ,fieldList).build(PostLoadActivity.this);
                //start activity result
                startActivityForResult(intent,100);
            }
        });
        etPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Initialising place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS
                        ,Place.Field.LAT_LNG,Place.Field.NAME);
                //Create intent
                Intent intent= new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY
                        ,fieldList).build(PostLoadActivity.this);
                //start activity result
                startActivityForResult(intent,200);
            }
        });

        //populating the spinner
       vehicleList= addItemsOnSpinner();


        // getting the database instance
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        // clearing the entered values
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPickup.setText("");
                etDestination.setText("");
                etWeight.setText("");
                tvFees.setText("");
                tvMilage.setText("");
                tvOk.setVisibility(View.GONE);
            }
        });

        //
        btnCalculateMilage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tvOk.setVisibility(View.GONE);
                pickup = etPickup.getText().toString().trim();
                destination = etDestination.getText().toString().trim();
                double distance = getMilage(pickup, destination);
                distance = distance/1000;
                DecimalFormat f = new DecimalFormat("0.00");
                String aDistance = Double.toString(distance);
                tvMilage.setText(f.format(distance));
            }
        });

        btnCalculateFees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.refreshDrawableState();
                weight = Double.parseDouble(etWeight.getText().toString().trim());
                vehicle = spinnerVehicle.getSelectedItem().toString().trim();
                double distance = getMilage(pickup, destination);
                double priceForWeight = pricePerWeight(weight);
                double feesForVehicle = feesForVehicle(vehicle, vehicleList);
                totalFees = ((distance * priceForWeight)/1000 + feesForVehicle);
                totalFees = totalFees + (totalFees*0.13);
                DecimalFormat f = new DecimalFormat("0.00");
                tvFees.setText("$ "+f.format(totalFees));
                tvOk.setVisibility(View.VISIBLE);
                tvOk.setText("If you agree, click POST LOAD");
            }
        });
            // on clicking on postLoad button
            btnPostLoad.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                  pickup = etPickup.getText().toString().trim();
                  destination = etDestination.getText().toString().trim();
                  category = spinnerCategory.getSelectedItem().toString().trim();
                  vehicle = spinnerVehicle.getSelectedItem().toString().trim();
                  weight = Double.parseDouble(etWeight.getText().toString().trim());
                    //  progressBar.setVisibility(View.VISIBLE);
                    if ( validateData(pickup, destination, category, weight, vehicle))
                    {

                        // if the user is already registered
                        if(firebaseAuth.getCurrentUser() != null)
                        {
                            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful()){
                                        Toast.makeText(PostLoadActivity.this, "User verified", Toast.LENGTH_SHORT).show();
                                        userID = firebaseAuth.getCurrentUser().getUid();
                                        DocumentReference documentReference = firestore.collection("loads").document();
                                        Map<String, Object> load = new HashMap<>();
                                        load.put("UserID", userID);
                                        load.put("Status", "Not Booked");
                                        load.put("Delivery Fees", totalFees);
                                        load.put("Pickup", pickup);
                                        load.put("Destination", destination);
                                        load.put("Category", category);
                                        load.put("Weight", weight);
                                        load.put("Vehicle Wanted", vehicle);
                                        load.put("DateTime", getCurrnetDateTime());
                                        load.put("Driver Location","");
                                        load.put("Driver ID", "");

                                        documentReference.set(load).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: The load successfully posted for "+ userID  );
                                                Intent intent = new Intent (PostLoadActivity.this, CustomerFeedActivity.class );
                                                intent.putExtra("email", email);
                                                intent.putExtra("password", password);
                                                startActivity(intent);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: "+ e.toString());
                                            }
                                        });
                                    } else {
                                        Toast.makeText(PostLoadActivity.this, "Error: "+task.getException(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }

                } // end OnClick()

            }); // end btnPostLoad click
    } // end onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode == RESULT_OK){
            //when success
            //Initialise place
            Place place = Autocomplete.getPlaceFromIntent(data);
            etDestination.setText(place.getAddress());
        }
        else if(requestCode==200 && resultCode == RESULT_OK){
            //when success
            //Initialise place
            Place place1 = Autocomplete.getPlaceFromIntent(data);
            etPickup.setText(place1.getAddress());
        }
        else if (resultCode== AutocompleteActivity.RESULT_ERROR){
            //when error
            //initialize status
            Status status = Autocomplete.getStatusFromIntent(data);
            //display toast
            Toast.makeText(getApplicationContext(),status.getStatusMessage()
                    ,Toast.LENGTH_SHORT).show();
        }
    }

    //get date and time
    public static String getCurrnetDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy : HH:mm:ss");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    public List<String> addItemsOnSpinner(){
        spinnerVehicle = (Spinner) findViewById(R.id.spinner_truck);

        List<String> list = new ArrayList<String>();
        list.add("Choose Vehicle");
        list.add("SUV Van");
        list.add("15ft Truck");
        list.add("25ft Truck");
        list.add("35ft Truck");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, list
        );
        spinnerVehicle.setAdapter(arrayAdapter);
        return list;
    }

    public double feesForVehicle (String vehicle, List<String> list){
        double fees =0;
        if (vehicle.equals(list.get(1)))
        {
            fees = 10;
        }
        else if (vehicle.equals(list.get(2)))
        {
            fees = 20;
        }

        else if (vehicle.equals(list.get(3)))
        {
            fees = 40;
        }
        else if (vehicle.equals(list.get(4)))
        {
            fees = 50;
        }
        return fees;
    }

    // get weight category
    public double pricePerWeight(double weight){
        double priceOfKM =0;

        if (weight<1){
            Toast.makeText(getApplicationContext(), "Error: Weight should be greater than 0", Toast.LENGTH_LONG).show();
            etWeight.setText("Error: least value is 1");
        }

        if (weight<=100)
            priceOfKM = 1;
        if (weight>100 && weight <=200)
            priceOfKM = 1.5;
        if (weight>200 && weight <=300)
            priceOfKM = 2.0;
        if (weight>300 && weight <=500)
            priceOfKM = 2.5;
        if (weight>500)
            priceOfKM = 3.0;
        return priceOfKM;
    }

    public double getMilage(String pickup, String destination) {
       List<Address> addressList = null;
       double distance=0;
       Location location1=null, location2=null;
       try {
           if (pickup != null && !pickup.equals("") ) {
               Geocoder geocoder = new Geocoder(this);
               try {
                   addressList = geocoder.getFromLocationName(pickup, 1);
               } catch (IOException e) {
                   e.printStackTrace();
               }
               Address address = addressList.get(0);
               // start = new LatLng(address.getLatitude(), address.getLongitude());
               location1 = new Location(pickup);
               location1.setLatitude(address.getLatitude());
               location1.setLongitude(address.getLongitude());
           }
           if (destination != null || !destination.equals("")) {
               Geocoder geocoder = new Geocoder(this);
               try {
                   addressList = geocoder.getFromLocationName(destination, 1);
               } catch (IOException e) {
                   e.printStackTrace();
               }
               Address address = addressList.get(0);
              // end = new LatLng(address.getLatitude(), address.getLongitude());
               location2 = new Location(destination);
               location2.setLatitude(address.getLatitude());
               location2.setLongitude(address.getLongitude());
           }
           distance = location1.distanceTo(location2);
       }
       catch (Exception e) {
           e.printStackTrace();
       }
       return distance;
   }

    public Boolean validateData(String pickup, String destination, String category, double weight, String vehicle){
        Boolean result=true;
        // validating the data entered
        if(pickup.equals("".trim())){
            etPickup.setError("Error: email required!");
            result = false;
        }
        if(destination.equals("".trim())){
            etDestination.setError("Error: password required!");
            result = false;
        }
        if(category.equals("Load Category")){
            Toast.makeText(getApplicationContext(), "Error: Select Load Category!", Toast.LENGTH_LONG).show();
            result = false;
        }
        if(vehicle.equals("Choose Vehicle")){
            Toast.makeText(getApplicationContext(), "Error: Select Vehicle Category!", Toast.LENGTH_LONG).show();
            result = false;
        }
        if(weight == 0){
            Toast.makeText(getApplicationContext(), "Error: Enter the weight!", Toast.LENGTH_LONG).show();
            result = false;
        }
        return result;
    }

} // end class
