package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ViewPresence extends AppCompatActivity {

    ListView lvPresence;
    DatabaseReference dbRefIT = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    ArrayList<String> alPresence = new ArrayList<>();
    Button btnExportToExcel;
    String slotNo="", subDay="", subTitle="";
    int stdCnt=0;
    TextView tvSubjectDetails;
    String fileSubject="", fileSubTeacher="", fileSubTime="", fileSubDay="", fileDate="";
    HashMap<String, String> hmDay= new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_presence);

        lvPresence = findViewById(R.id.lv_presence);
        tvSubjectDetails = findViewById(R.id.tv_sub_details);
        btnExportToExcel = findViewById(R.id.btn_export_excel);
        Bundle bl = getIntent().getExtras();
        slotNo = bl.getString("slotno");
        subDay = bl.getString("subday");
        subTitle = bl.getString("subtitle");

        hmDay.put("mon", "Monday");
        hmDay.put("tue", "Tuesday");
        hmDay.put("wed", "Wednesday");
        hmDay.put("thu", "Thursday");
        hmDay.put("fri", "Friday");
        hmDay.put("sat", "Saturday");

        tvSubjectDetails.setText(subTitle);
        try {
            dbRefIT.child(subDay).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FbData fbData = snapshot.child(slotNo).getValue(FbData.class);
                    stdCnt = fbData.presentcnt;
                    if(fbData.presentcnt>0){
                        fileSubDay = hmDay.get(subDay);
                        fileSubject = fbData.subject;
                        fileSubTeacher = fbData.subteacher;
                        fileSubTime = fbData.subtime;
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

        btnExportToExcel.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                try{
                    int WRITE_EXTERNAL_PERMISSION = ActivityCompat.checkSelfPermission(ViewPresence.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if(WRITE_EXTERNAL_PERMISSION==0){
                        if(alPresence.size()>0){
                            HSSFWorkbook workbook = new HSSFWorkbook();
                            HSSFSheet sheet = workbook.createSheet();
                            HSSFRow row = sheet.createRow(0);
                            HSSFCell cell = row.createCell(0);
                            cell.setCellValue(fileSubject+" by "+fileSubTeacher+" on "+fileSubDay+", "+fileSubTime);
                            row = sheet.createRow(1);
                            cell = row.createCell(0);
                            cell.setCellValue("Student Count: "+alPresence.size());
                            for(int i=0;i<alPresence.size();i++){
                                String[] stdDet = alPresence.get(i).split(", ");
                                row = sheet.createRow(i+2);
                                cell = row.createCell(0);
                                cell.setCellValue(stdDet[0]);
                                cell = row.createCell(1);
                                cell.setCellValue(stdDet[1]);
                            }
                            Calendar cl = Calendar.getInstance();
                            Date d = cl.getTime();
                            fileDate = d.getDay()+"-"+d.getMonth()+"-"+d.getYear();
                            String fileName = fileDate+" "+fileSubDay+" "+fileSubject+" "+fileSubTeacher+" "+fileSubTime;
                            File exFile = new File(Environment.getExternalStorageDirectory()+"/excel.xls");
                            if(!exFile.exists()){
                                exFile.createNewFile();
                            }
                            FileOutputStream fOut = new FileOutputStream(exFile);
                            workbook.write(fOut);
                            Toast.makeText(ViewPresence.this, "File exported: "+fileName, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ViewPresence.this, "There is no Presence!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ViewPresence.this, e+"", Toast.LENGTH_SHORT).show();
                }
            }
        });

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