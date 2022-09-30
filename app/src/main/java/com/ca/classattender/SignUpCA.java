package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpCA extends AppCompatActivity {

    EditText etFullName, etEnr, etEmail, etPass, etConfirmPass;
    Spinner spDepartment;
    TextView tvLogIn;
    Button btnSignUp;
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/");
    String dept;
    HashMap<String, String> hmDepartments = new HashMap<>();
    DBHelper dbh = new DBHelper(SignUpCA.this , null, null, 1);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_ca);

        tvLogIn = findViewById(R.id.tv_log_in);
        btnSignUp = findViewById(R.id.btn_sign_up);
        etFullName = findViewById(R.id.et_name);
        etEnr = findViewById(R.id.et_enr);
        etEmail = findViewById(R.id.et_email);
        spDepartment = findViewById(R.id.sp_department);
        etPass = findViewById(R.id.et_pass);
        etConfirmPass = findViewById(R.id.et_confirm_pass);

        hmDepartments.put("Information Technology", "it");
        hmDepartments.put("Computer Engineering", "ce");
        hmDepartments.put("Biomedical Engineering", "bm");
        hmDepartments.put("Electrical Communication Engineering", "ece");
        hmDepartments.put("Mechanical Engineering", "mce");
        hmDepartments.put("Metallurgy Engineering", "mte");

        tvLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpCA.this, LoginCA.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dept = spDepartment.getSelectedItem().toString();
                if (etFullName.getText().toString().isEmpty()){
                    etFullName.setError("Please fill this field!");
                }else if (etEnr.getText().toString().isEmpty()){
                    etEnr.setError("Please fill this field!");
                }else if (etEmail.getText().toString().isEmpty()){
                    etEmail.setError("Please fill this field!");
                }else if (!dept.equals("Information Technology")){
                    Toast.makeText(SignUpCA.this, "Only I.T. department is available now!", Toast.LENGTH_SHORT).show();
                }else if (etPass.getText().toString().isEmpty()){
                    etPass.setError("Please fill this field!");
                }else if (etConfirmPass.getText().toString().isEmpty()){
                    etConfirmPass.setError("Please fill this field!");
                }else if (!etPass.getText().toString().equals(etConfirmPass.getText().toString())){
                    etConfirmPass.setError("Password mismatch!");
                }else {
                    registerUser();
                }
            }
        });
    }

    private void registerUser() {
        String usrName = etFullName.getText().toString();
        String usrEnr = etEnr.getText().toString();
        String usrEmail = etEmail.getText().toString();
        String usrDept = hmDepartments.get(dept);
        String usrPass = etConfirmPass.getText().toString();
        fbAuth.createUserWithEmailAndPassword(usrEmail, usrPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try {
                        String[] strEmail = usrEmail.split("@");
                        dbRef.child("class_attender").child("students").child(usrDept).child(strEmail[0]).child("s_enr").setValue(usrEnr);
                        dbRef.child("class_attender").child("students").child(usrDept).child(strEmail[0]).child("s_name").setValue(usrName);
                        dbRef.child("class_attender").child("students").child(usrDept).child(strEmail[0]).child("s_dept").setValue(usrDept);
                        Toast.makeText(SignUpCA.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        dbh.writeUserRecentRegister(1);
                        startActivity(new Intent(SignUpCA.this, LoginCA.class));
                        finish();
                    } catch (Exception e) {
                        Log.d("bhak", e.toString());
                    }
                }else{
                    Toast.makeText(SignUpCA.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}