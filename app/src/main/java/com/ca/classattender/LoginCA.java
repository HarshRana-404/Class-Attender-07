package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginCA extends AppCompatActivity {

    TextView tvSignUp;
    EditText etEmail, etPass;
    Button btnLogIn;
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/");
    DBHelper dbh = new DBHelper(this, null, null, 1);
    String usrEmail="", usrName="", usrEnr="", usrDept="";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ca);

        tvSignUp = findViewById(R.id.tv_sign_up);
        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_pass);
        btnLogIn = findViewById(R.id.btn_log_in);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginCA.this, SignUpCA.class));
            }
        });

//        startActivity(new Intent(LoginCA.this, MainActivity.class));

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etEmail.getText().toString().isEmpty()){
                    etEmail.setError("Please fill this field!");
                }else if (etPass.getText().toString().isEmpty()){
                    etPass.setError("Please fill this field!");
                }else {
                    loginUser();
                }
            }
        });
    }

    private void loginUser() {
        String usrEmail = etEmail.getText().toString();
        String usrPass = etPass.getText().toString();
        fbAuth.signInWithEmailAndPassword(usrEmail, usrPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LoginCA.this, "Log In Successful!", Toast.LENGTH_SHORT).show();
                    if(usrEmail.endsWith("@classattender.com")){
                        startActivity(new Intent(LoginCA.this, MainActivity.class));
                    }else{
                        storeInSqlite(usrEmail);
                        startActivity(new Intent(LoginCA.this, StudentCA.class));
                    }
                }else{
                    Toast.makeText(LoginCA.this, "Log In Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeInSqlite(String em) {
        try {
            String[] strEmail = em.split("@");
            usrEmail = strEmail[0];

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usrName = snapshot.child("class_attender").child("students").child("it").child(usrEmail).child("s_name").getValue().toString();
                    usrEnr = snapshot.child("class_attender").child("students").child("it").child(usrEmail).child("s_enr").getValue().toString();
                    usrDept = snapshot.child("class_attender").child("students").child("it").child(usrEmail).child("s_dept").getValue().toString();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            dbh.storeStudentDetails(em, usrEnr, usrName, usrDept);

        } catch (Exception e) {
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(fbAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginCA.this, MainActivity.class));
            finish();
        }
    }
}