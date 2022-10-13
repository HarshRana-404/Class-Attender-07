package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class StudentCA extends AppCompatActivity {

    DBHelper dbh = new DBHelper(StudentCA.this, null, null, 1);
    Button btnVerifyOTP;
    EditText etOTP;
    Cursor csr;
    String otp, usrEmail="", usrEnr="", usrName="", usrDept="", subTeacherShortName="";
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    RecyclerView rvDaysSlots[] = new RecyclerView[6];
    ArrayList<ArrayList<SlotModel>> slotList = new ArrayList<>();
    ArrayList<Integer> slotTemplateImg = new ArrayList<>();
    ArrayList<String> slotDaysList = new ArrayList<>();
    SlotRvAdapterStudent slotRvAdapters[] = new SlotRvAdapterStudent[6];
    DatabaseReference dbRefList = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    HashMap<String,String> hmSubCode = new HashMap<>();
    ProgressDialog pd;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_ca);

        rvDaysSlots[0] = findViewById(R.id.rv_monday_slots_std);
        rvDaysSlots[1] = findViewById(R.id.rv_tuesday_slots_std);
        rvDaysSlots[2] = findViewById(R.id.rv_wednesday_slots_std);
        rvDaysSlots[3] = findViewById(R.id.rv_thursday_slots_std);
        rvDaysSlots[4] = findViewById(R.id.rv_friday_slots_std);
        rvDaysSlots[5] = findViewById(R.id.rv_saturday_slots_std);

        slotTemplateImg.add(R.drawable.slot_template_1);
        slotTemplateImg.add(R.drawable.slot_template_2);
        slotTemplateImg.add(R.drawable.slot_template_3);
        slotTemplateImg.add(R.drawable.slot_template_4);
        slotTemplateImg.add(R.drawable.slot_template_5);
        slotTemplateImg.add(R.drawable.slot_template_6);

        hmSubCode.put("ETC", "3130004");
        hmSubCode.put("DBMS", "3130703");
        hmSubCode.put("DE", "3130008");
        hmSubCode.put("DF", "3130704");
        hmSubCode.put("DS", "3130702");
        hmSubCode.put("IC", "3130007");
        hmSubCode.put("PAS", "3130006");

        slotDaysList.add("mon");
        slotDaysList.add("tue");
        slotDaysList.add("wed");
        slotDaysList.add("thu");
        slotDaysList.add("fri");
        slotDaysList.add("sat");

        try {
            for (int i=0; i<6; i++){
                rvDaysSlots[i].setLayoutManager(new LinearLayoutManager(StudentCA.this, LinearLayoutManager.HORIZONTAL, false));
                slotList.add(new ArrayList<>());
                slotRvAdapters[i] = new SlotRvAdapterStudent(StudentCA.this, slotList.get(i));
                rvDaysSlots[i].setAdapter(slotRvAdapters[i]);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        try {
            View tb = findViewById(R.id.toolbar);
            setSupportActionBar((Toolbar) tb);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        pd = new ProgressDialog(StudentCA.this, R.style.CustomProgressDialog);
        pd.setMessage("Loading Slots");
        pd.setCancelable(false);
        pd.show();
        pd.setContentView(R.layout.progress_dialog_layout);
        getAllSlots();

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

    @SuppressLint("NotifyDataSetChanged")
    public void notifyAdapter(int i){
        slotRvAdapters[i].notifyDataSetChanged();
    }

    public void getAllSlots(){
        dbRefList.child("mon").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        slotList.get(0).clear();
                        FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                        slotList.get(0).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "mon"));
                        notifyAdapter(0);
                        int i = 2;
                        while (fbData != null){
                            fbData = snapshot.child("slot"+i).getValue(FbData.class);
                            slotList.get(0).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "mon"));
                            notifyAdapter(0);
                            i++;
                        }
                    } catch (Exception e) {

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        dbRefList.child("tue").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    slotList.get(1).clear();
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    slotList.get(1).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "tue"));
                    notifyAdapter(1);
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        slotList.get(1).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "tue"));
                        notifyAdapter(1);
                        i++;
                    }
                } catch (Exception e) {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        dbRefList.child("wed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    slotList.get(2).clear();
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    slotList.get(2).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "wed"));
                    notifyAdapter(2);
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        slotList.get(2).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "wed"));
                        notifyAdapter(2);
                        i++;
                    }
                } catch (Exception e) {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        dbRefList.child("thu").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    slotList.get(3).clear();
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    slotList.get(3).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "thu"));
                    notifyAdapter(3);
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        slotList.get(3).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "thu"));
                        notifyAdapter(3);
                        i++;
                    }
                } catch (Exception e) {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        dbRefList.child("fri").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    slotList.get(4).clear();
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    slotList.get(4).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "fri"));
                    notifyAdapter(4);
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        slotList.get(4).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "fri"));
                        notifyAdapter(4);
                        i++;
                    }
                } catch (Exception e) {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        dbRefList.child("sat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    slotList.get(5).clear();
                    FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                    slotList.get(5).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "sat"));
                    notifyAdapter(5);
                    int i = 2;
                    while (fbData != null){
                        fbData = snapshot.child("slot"+i).getValue(FbData.class);
                        slotList.get(5).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "sat"));
                        notifyAdapter(5);
                        i++;
                    }
                } catch (Exception e) {

                }finally {
                    pd.hide();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        fbAuth.signOut();
        startActivity(new Intent(StudentCA.this, LoginCA.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}