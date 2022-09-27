package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    Button btnClick;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnClick = findViewById(R.id.btn_click);
        try{
            dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/");
        } catch (Exception e) {
            Log.d("chaiiya", e.toString());
        }

        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    dbRef.child("class_attender").child("otps").child("it").child("ds").setValue("");
                } catch (Exception e) {
                    Log.d("chaiiya", e.toString());
                }
            }
        });
    }
}