package com.ca.classattender;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    if(fbData.subteacher.toUpperCase().equals(subTeacher.toUpperCase()) && fbData.subject.toUpperCase().equals(subName.toUpperCase()) && fbData.subtime.equals(subTime)){
                        dbRefIT.child(subDay).child("slot"+1).child("otp").setValue(strOTP);
                    }
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        if(fbData.subteacher.toUpperCase().equals(subTeacher.toUpperCase()) && fbData.subject.toUpperCase().equals(subName.toUpperCase()) && fbData.subtime.equals(subTime)){
                            dbRefIT.child(subDay).child("slot"+i).child("otp").setValue(strOTP);
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
