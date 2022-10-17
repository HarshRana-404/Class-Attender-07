package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    String slotNo="", subDay="";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_presence);

        lvPresence = findViewById(R.id.lv_presence);

        try {
            Bundle bl = getIntent().getExtras();
            slotNo = bl.getString("slotno");
            subDay = bl.getString("subday");

            dbRefIT.child(subDay).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FbData fbData = snapshot.child(slotNo).getValue(FbData.class);
                    if(fbData.presentcnt>0){
                        loadAttendance();
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
                    FbStdData fbStdData = snapshot.child("std"+1).getValue(FbStdData.class);
                    alPresence.add(fbStdData.stdenr+", "+fbStdData.stdname);
                    int s=2;
                    while(fbStdData!=null){
                        fbStdData= snapshot.child("std"+s).getValue(FbStdData.class);
                        alPresence.add(fbStdData.stdenr+", "+fbStdData.stdname);
                        s++;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (Exception e) {}
        finally {
            Toast.makeText(this, alPresence.size()+"", Toast.LENGTH_SHORT).show();
            if(alPresence.size()>0){
                lvPresence.setAdapter(new ArrayAdapter<String>(ViewPresence.this, android.R.layout.simple_list_item_1, alPresence));
            }else{
                alPresence.add("No Attendance!");
            }
        }
    }
}