package com.ca.classattender;

import android.content.Context;
import android.content.Intent;
import android.content.RestrictionEntry;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class VerifyOTP extends BottomSheetDialog{

    Context context;
    Button btnVerifyOTP;
    EditText etOTP;
    Cursor csr;
    String otp="", usrEnr="", usrName="", usrEmail="", usrDept="";
    DBHelper dbh;
    ArrayList<Integer> slotTemplateImg = new ArrayList<>();
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    TextView tvSubName, tvSubTime, tvSubCode, tvSubTeacher;
    ImageView ivSlotTemplate;
    String dlSlotNo="", dlSlotDay="";

    public VerifyOTP(Context context){
        super(context);
        this.context = context;
        dbh = new DBHelper(context, null, null, 1);
        slotTemplateImg.add(R.drawable.slot_template_1);
        slotTemplateImg.add(R.drawable.slot_template_2);
        slotTemplateImg.add(R.drawable.slot_template_3);
        slotTemplateImg.add(R.drawable.slot_template_4);
        slotTemplateImg.add(R.drawable.slot_template_5);
        slotTemplateImg.add(R.drawable.slot_template_6);
    }

    public void verifyOTP(String subDay, String subName, String subTime, String subTeacher, String subCode, int subTemplate){
        try{
            BottomSheetDialog bsVerifyOTP = new BottomSheetDialog(context);
            View v = LayoutInflater.from(context).inflate(R.layout.verify_otp_bottom_sheet, (ViewGroup) findViewById(R.id.bs_otp_root_layout));
            bsVerifyOTP.setContentView(v);
            btnVerifyOTP = v.findViewById(R.id.btn_verify_otp);
            etOTP = v.findViewById(R.id.et_otp);

            tvSubName = v.findViewById(R.id.tv_sub_name_std);
            tvSubCode = v.findViewById(R.id.tv_sub_code_std);
            tvSubTime = v.findViewById(R.id.tv_slot_time_std);
            tvSubTeacher = v.findViewById(R.id.tv_sub_teacher_std);
            ivSlotTemplate = v.findViewById(R.id.iv_slot_template_std);

            tvSubName.setText(subName.toUpperCase());
            tvSubCode.setText(subCode.toUpperCase());
            tvSubTime.setText(subTime.toUpperCase());
            tvSubTeacher.setText(subTeacher.toUpperCase());
            ivSlotTemplate.setBackgroundResource(subTemplate);

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
                            }
                            dbRef.child(subDay).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    try {
                                        FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                                        String dbOTPExp = fbData.otpexp;
                                        Calendar curCl = Calendar.getInstance();
                                        Calendar dbCl = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                        Date d = sdf.parse(dbOTPExp);
                                        dbCl.setTime(d);
                                        Date curDate = curCl.getTime();
                                        String otpExpTime = curDate.getHours()+":"+curDate.getMinutes();
                                        String[] curHrmn = otpExpTime.split(":");
                                        String[] dbHrmn = dbOTPExp.split(":");
                                        int curHr=0, curMn=0, dbHr=0, dbMn=0;
                                        curHr = Integer.parseInt(curHrmn[0]);
                                        curMn = Integer.parseInt(curHrmn[1]);
                                        dbHr = Integer.parseInt(dbHrmn[0]);
                                        dbMn = Integer.parseInt(dbHrmn[1]);

                                        if(fbData.subject.toUpperCase().equals(subName) && fbData.subteacher.toUpperCase().equals(subTeacher) && fbData.subtime.toUpperCase().equals(subTime)){
                                            dlSlotDay = subDay;
                                            dlSlotNo = "slot"+1;
                                            String dbOTP;
                                            dbOTP = fbData.otp;
                                            if(dbOTP.equals(otp)){
                                                if(true) {
                                                    if (true) {
                                                        if(fbData.presentcnt == 0){
                                                            dbRef.child(subDay).child("slot"+1).child("presence").child("std1").child("stdname").setValue(usrName);
                                                            dbRef.child(subDay).child("slot"+1).child("presence").child("std1").child("stdenr").setValue(usrEnr);
                                                            dbRef.child(subDay).child("slot"+1).child("presentcnt").setValue(1);
                                                            Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show();
                                                            bsVerifyOTP.dismiss();
                                                        }else{
                                                            Boolean marked=false;
                                                            int cnt = 1;
                                                            int tigada=1;
                                                            if(fbData.presentcnt>=2){
                                                                tigada=0;
                                                            }
                                                            else{
                                                                tigada=1;
                                                            }
                                                            while(cnt!=fbData.presentcnt+tigada){
                                                                FbStdData fbStdData= snapshot.child("slot"+cnt).child("presence").child("std"+1).getValue(FbStdData.class);
                                                                if(fbStdData.stdenr.equals(usrEnr)){
                                                                    marked = true;
                                                                }
                                                                cnt++;
                                                            }
                                                            if(!marked){
                                                                int n = fbData.presentcnt+1;
                                                                dbRef.child(subDay).child("slot"+1).child("presence").child("std"+n).child("stdname").setValue(usrName);
                                                                dbRef.child(subDay).child("slot"+1).child("presence").child("std"+n).child("stdenr").setValue(usrEnr);
                                                                dbRef.child(subDay).child("slot"+1).child("presentcnt").setValue(n);
                                                                Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show();
                                                                bsVerifyOTP.dismiss();
                                                            }else{
                                                                Toast.makeText(context, "Attendance already marked!", Toast.LENGTH_SHORT).show();
                                                                bsVerifyOTP.dismiss();
                                                            }
                                                        }
                                                    }
                                                    else{
                                                        deleteOTP();
                                                    }
                                                }
                                                else{
                                                    deleteOTP();
                                                }
                                            }else{
                                                Toast.makeText(context, "Wrong OTP!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        int i = 2;
                                        while (fbData != null){
                                            fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                            if(fbData.subject.toUpperCase().equals(subName) && fbData.subteacher.toUpperCase().equals(subTeacher) && fbData.subtime.toUpperCase().equals(subTime)){
                                                dlSlotDay = subDay;
                                                dlSlotNo = "slot"+i;
                                                String dbOTP;
                                                dbOTP = fbData.otp;
                                                if(dbOTP.equals(otp)){
                                                    if(true) {
                                                        if (true) {
                                                            if(fbData.presentcnt == 0){
                                                                dbRef.child(subDay).child("slot"+i).child("presence").child("std1").child("stdname").setValue(usrName);
                                                                dbRef.child(subDay).child("slot"+i).child("presence").child("std1").child("stdenr").setValue(usrEnr);
                                                                dbRef.child(subDay).child("slot"+i).child("presentcnt").setValue(1);
                                                                Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show();
                                                                bsVerifyOTP.dismiss();
                                                            }else{
                                                                Boolean marked=false;
                                                                int cnt = 1;
                                                                int tigada=1;
                                                                if(fbData.presentcnt>=2){
                                                                    tigada=0;
                                                                }
                                                                else{
                                                                    tigada=1;
                                                                }
                                                                while(cnt!=fbData.presentcnt+tigada){
                                                                    FbStdData fbStdData= snapshot.child("slot"+i).child("presence").child("std"+cnt).getValue(FbStdData.class);
                                                                    if(fbStdData.stdenr.equals(usrEnr)){
                                                                        marked = true;
                                                                    }
                                                                    cnt++;
                                                                }
                                                                if(!marked){
                                                                    int n = fbData.presentcnt+1;
                                                                    dbRef.child(subDay).child("slot"+i).child("presence").child("std"+n).child("stdname").setValue(usrName);
                                                                    dbRef.child(subDay).child("slot"+i).child("presence").child("std"+n).child("stdenr").setValue(usrEnr);
                                                                    dbRef.child(subDay).child("slot"+i).child("presentcnt").setValue(n);
                                                                    Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show();
                                                                    bsVerifyOTP.dismiss();
                                                                }else{
                                                                    Toast.makeText(context, "Attendance already marked!", Toast.LENGTH_SHORT).show();
                                                                    bsVerifyOTP.dismiss();
                                                                }
                                                            }
                                                        }
                                                        else{
                                                            deleteOTP();
                                                        }
                                                    }
                                                    else{
                                                        deleteOTP();
                                                    }
                                                }else{
                                                    Toast.makeText(context, "Wrong OTP!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            i++;
                                        }
                                    } catch (Exception e) {
//                                        Toast.makeText(context, e.toString(),  Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        } catch (Exception e) {
//                            Toast.makeText(context, e.toString(),  Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(context, "Enter OTP!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
//            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteOTP(){
        Toast.makeText(context, "OTP is expired!", Toast.LENGTH_SHORT).show();
        dbRef.child(dlSlotDay).child(dlSlotNo).child("otp").removeValue();
        dbRef.child(dlSlotDay).child(dlSlotNo).child("otpexp").removeValue();
    }

}
