package com.example.moveitapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moveitapp.R;

public class MainActivity extends AppCompatActivity {

    private Button btnCustomer, btnDriver, btnCSR;

   // public DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCustomer = (Button) findViewById(R.id.btn_customer);
        btnDriver = (Button) findViewById(R.id.btn_driver);
        btnCSR = (Button) findViewById(R.id.btn_csr);

       // db = new DatabaseHelper(this);
        // click listeners
        btnCustomer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

       btnDriver.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
//        btnCSR.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, StaffLoginActivity.class);
//                startActivity(intent);
//                finish();
//                return;
//            }
//        });
    }
}
