package com.ca.classattender;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginCA extends AppCompatActivity {

    TextView tvSignUp;
    EditText etEnr, etPass;
    Button btnLogIn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ca);

        tvSignUp = findViewById(R.id.tv_sign_up);
        etEnr = findViewById(R.id.et_enr);
        etPass = findViewById(R.id.et_pass);
        btnLogIn = findViewById(R.id.btn_log_in);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginCA.this, SignUpCA.class));
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etEnr.getText().toString().isEmpty()){
                    etEnr.setError("Please fill this field!");
                }else if (etPass.getText().toString().isEmpty()){
                    etPass.setError("Please fill this field!");
                }else {
//                   TODO LOGIN CODE OF FIREBASE
                    startActivity(new Intent(LoginCA.this, MainActivity.class));
                }
            }
        });
    }
}