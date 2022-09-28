package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    Button btnTakeAttendance;
    Spinner spDepartment, spSemester, spSubject;

    HashMap<String, ArrayAdapter> hmSubjects = new HashMap<>();
    HashMap<String, String> hmDepartments = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTakeAttendance = findViewById(R.id.btn_take_attendance);
        spDepartment = findViewById(R.id.sp_department);
        spSubject = findViewById(R.id.sp_subject);
        spSemester = findViewById(R.id.sp_semester);

        hmSubjects.put("3", new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.it_sem_3)));
        hmSubjects.put("4", new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.it_sem_4)));

        hmDepartments.put("Information Technology", "it");
        hmDepartments.put("Computer Engineering", "ce");
        hmDepartments.put("Biomedical Engineering", "bm");
        hmDepartments.put("Electrical Communication Engineering", "ece");
        hmDepartments.put("Mechanical Engineering", "mce");
        hmDepartments.put("Metallurgy Engineering", "mte");

        try{
            dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/");
        } catch (Exception e) {
            Log.d("chaiiya", e.toString());
        }

        spDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!spDepartment.getSelectedItem().toString().equals("Select Department")){
                    spSemester.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.semesters)));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spSubject.setAdapter(hmSubjects.get(spSemester.getSelectedItem().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        btnTakeAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dept = spDepartment.getSelectedItem().toString();
                String sem = spSemester.getSelectedItem().toString();
                String sub = spSubject.getSelectedItem().toString();
                if(dept.toLowerCase().equals("select department")){
                    Toast.makeText(MainActivity.this, "Select a Department!", Toast.LENGTH_SHORT).show();
                }else if(sem.toLowerCase().equals("select subject")){
                    Toast.makeText(MainActivity.this, "Select a Semester!", Toast.LENGTH_SHORT).show();
                }else if(sub.toLowerCase().equals("select semester")){
                    Toast.makeText(MainActivity.this, "Select a Subject!", Toast.LENGTH_SHORT).show();
                }else{
                    Random rn = new Random();
                    dbRef.child("class_attender").child("otps").child(hmDepartments.get(dept)).child(sub.toLowerCase()).child("otp").setValue(rn.nextInt(1111));
                }
            }
        });
    }
}