package com.ca.classattender;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class StudentCA extends AppCompatActivity {

    DBHelper dbh = new DBHelper(StudentCA.this, null, null, 1);
    Button btnVerifyOTP;
    EditText etOTP;
    Cursor csr;
    String otp, usrEmail="", usrEnr="", usrName="", usrDept="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_ca);

        btnVerifyOTP = findViewById(R.id.btn_verify_otp);
        etOTP = findViewById(R.id.et_otp);

        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp = etOTP.getText().toString();
                if(!otp.equals("")){
                    try{
                        csr = dbh.getStudentDetails();
                        while(csr.moveToNext()){
                            usrEmail = csr.getString(0);
                            usrEnr = csr.getString(1);
                            usrName = csr.getString(2);
                            usrDept = csr.getString(3);
                        }
                        Toast.makeText(StudentCA.this, usrEmail+", "+usrEnr+", "+usrName+", "+usrDept,  Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(StudentCA.this, e.toString(),  Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(StudentCA.this, "Enter OTP!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}