package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginCA extends AppCompatActivity {

    TextView tvSignUp;
    EditText etEmail, etPass;
    Button btnLogIn;
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
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
//                   TODO LOGIN CODE OF FIREBASE
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
                    startActivity(new Intent(LoginCA.this, MainActivity.class));
                }else{
                    Toast.makeText(LoginCA.this, "Log In Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(fbAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginCA.this, MainActivity.class));
        }
    }
}