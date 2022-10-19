package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewPresence extends AppCompatActivity {

    ListView lvPresence;
    DatabaseReference dbRefIT = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    ArrayList<String> alPresence = new ArrayList<>();
    String slotNo="", subDay="", subTitle="";
    int stdCnt=0;
    TextView tvSubjectDetails;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_presence);

        lvPresence = findViewById(R.id.lv_presence);
        tvSubjectDetails = findViewById(R.id.tv_sub_details);
        Bundle bl = getIntent().getExtras();
        slotNo = bl.getString("slotno");
        subDay = bl.getString("subday");
        subTitle = bl.getString("subtitle");
        tvSubjectDetails.setText(subTitle);
        try {
            dbRefIT.child(subDay).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FbData fbData = snapshot.child(slotNo).getValue(FbData.class);
                    stdCnt = fbData.presentcnt;
                    if(fbData.presentcnt>0){
                        loadAttendance();
                    }else{
                        alPresence.add("No Attendance!");
                        lvPresence.setAdapter(new ArrayAdapter<>(ViewPresence.this, android.R.layout.simple_list_item_1, alPresence));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (Exception e) {
            Toast.makeText(ViewPresence.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    public void loadAttendance(){
        try {
            dbRefIT.child(subDay).child(slotNo).child("presence").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int cnt = 1;
                    while(cnt!=stdCnt+1){
                        FbStdData fbStdData= snapshot.child("std"+cnt).getValue(FbStdData.class);
                        alPresence.add(fbStdData.stdenr+", "+fbStdData.stdname);
                        cnt++;
                    }
                    lvPresence.setAdapter(new ArrayAdapter<>(ViewPresence.this, android.R.layout.simple_list_item_1, alPresence));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (Exception e) {}
    }
}