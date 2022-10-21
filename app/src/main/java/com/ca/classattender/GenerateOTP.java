package com.ca.classattender;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class GenerateOTP {

    Context context;

    public GenerateOTP(Context context){
        this.context = context;
    }

    DatabaseReference dbRefIT = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");

    public void generateNewOTP(String subDay, String subName, String subTime, String subTeacher){
        dbRefIT.child(subDay).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Random rn = new Random();
                    String strOTP = String.valueOf(rn.nextInt(999999 - 111111) + 111111);
                    Calendar cl = Calendar.getInstance();
                    cl.add(Calendar.MINUTE, 30);
                    Date curDate = cl.getTime();
                    String otpExpTime = curDate.getHours()+":"+curDate.getMinutes();
                    String hrmn[] = otpExpTime.split(":");
                    if(Integer.parseInt(hrmn[0])<=9){
                        hrmn[0] = "0"+hrmn[0];
                    }
                    if(Integer.parseInt(hrmn[1])<=9){
                        hrmn[1] = "0"+hrmn[1];
                    }
                    otpExpTime = hrmn[0]+":"+hrmn[1];
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    if(fbData.subteacher.toUpperCase().equals(subTeacher.toUpperCase()) && fbData.subject.toUpperCase().equals(subName.toUpperCase()) && fbData.subtime.equals(subTime)){
                        dbRefIT.child(subDay).child("slot"+1).child("otp").setValue(strOTP);
                        dbRefIT.child(subDay).child("slot"+1).child("otpexp").setValue(otpExpTime);
                        Intent inFUllScreenOTP = new Intent(context, FullScreenOTP.class);
                        inFUllScreenOTP.putExtra("otp", strOTP);
                        context.startActivity(inFUllScreenOTP);
                    }
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        if(fbData.subteacher.toUpperCase().equals(subTeacher.toUpperCase()) && fbData.subject.toUpperCase().equals(subName.toUpperCase()) && fbData.subtime.equals(subTime)){
                            dbRefIT.child(subDay).child("slot"+i).child("otp").setValue(strOTP);
                            dbRefIT.child(subDay).child("slot"+i).child("otpexp").setValue(otpExpTime);
                            Intent inFUllScreenOTP = new Intent(context, FullScreenOTP.class);
                            inFUllScreenOTP.putExtra("otp", strOTP);
                            context.startActivity(inFUllScreenOTP);
                        }
                        i++;
                    }
                } catch (Exception e) {
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
