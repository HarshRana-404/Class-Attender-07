package com.ca.classattender;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpCA extends AppCompatActivity {

    EditText etFullName, etEnr, etPass, etConfirmPass;
    Spinner spDepartment;
    TextView tvLogIn;
    Button btnSignUp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_ca);

        tvLogIn = findViewById(R.id.tv_log_in);
        btnSignUp = findViewById(R.id.btn_sign_up);
        etFullName = findViewById(R.id.et_name);
        etEnr = findViewById(R.id.et_enr);
        spDepartment = findViewById(R.id.sp_department);
        etPass = findViewById(R.id.et_pass);
        etConfirmPass = findViewById(R.id.et_confirm_pass);

        tvLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpCA.this, LoginCA.class));
            }
        });



        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dept = spDepartment.getSelectedItem().toString();
                if (etFullName.getText().toString().isEmpty()){
                    etFullName.setError("Please fill this field!");
                }else if (etEnr.getText().toString().isEmpty()){
                    etEnr.setError("Please fill this field!");
                }else if (dept.toLowerCase().equals("select department")){
                    Toast.makeText(SignUpCA.this, dept, Toast.LENGTH_SHORT).show();
                }else if (etPass.getText().toString().isEmpty()){
                    etPass.setError("Please fill this field!");
                }else if (etConfirmPass.getText().toString().isEmpty()){
                    etConfirmPass.setError("Please fill this field!");
                }else if (!etPass.getText().toString().equals(etConfirmPass.getText().toString())){
                    etConfirmPass.setError("Password mismatch!");
                }else {
//                   TODO SIGNUP CODE OF FIREBASE
                    startActivity(new Intent(SignUpCA.this, LoginCA.class));
                }
            }
        });
    }
}