package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentCA extends AppCompatActivity {

    DBHelper dbh = new DBHelper(StudentCA.this, null, null, 1);
    Button btnVerifyOTP;
    EditText etOTP;
    Cursor csr;
    String otp, usrEmail="", usrEnr="", usrName="", usrDept="";
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_ca);
    }

    public void verifyOTPClick(View view) {
        BottomSheetDialog bsVerifyOTP = new BottomSheetDialog(StudentCA.this);
        View v = getLayoutInflater().inflate(R.layout.verify_otp_bottom_sheet, (ViewGroup) findViewById(R.id.bs_otp_root_layout));
        bsVerifyOTP.setContentView(v);
        btnVerifyOTP = v.findViewById(R.id.btn_verify_otp);
        etOTP = v.findViewById(R.id.et_otp);
        bsVerifyOTP.show();

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
                            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String strOTP;
                                    int otp;
                                    strOTP = snapshot.child("class_attender").child("otps").child("it").child("de").child("otp").getValue().toString();
                                    otp = Integer.parseInt(strOTP);
                                    Toast.makeText(StudentCA.this, otp+"", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
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