package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabAddSlot;
    ImageButton imgClose, imgAddSlot;
    Button btnAddTime;
    TextView tvShowTime, tvError;
    Spinner spDay, spSubject;
    int positionOfSubject=0, positionOfDay=0, index=0;
    String sub="", strTime="", subTeacherShortName="", subCode="", dayOfWeek = "";
    RecyclerView rvDaysSlots[] = new RecyclerView[6];
    ArrayList<ArrayList<SlotModel>> slotList = new ArrayList<>();
    ArrayList<Integer> slotTemplateImg = new ArrayList<>();
    ArrayList<String> slotDaysList = new ArrayList<>();
    SlotRvAdapter slotRvAdapters[] = new SlotRvAdapter[6];
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DatabaseReference dbRefList = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    HashMap<String,String> hmSubCode = new HashMap<>();
    ProgressDialog pd;
    int slotNums=0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            View tb = findViewById(R.id.toolbar);
            setSupportActionBar((Toolbar) tb);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        fabAddSlot = findViewById(R.id.fab_add_slot);
        rvDaysSlots[0] = findViewById(R.id.rv_monday_slots);
        rvDaysSlots[1] = findViewById(R.id.rv_tuesday_slots);
        rvDaysSlots[2] = findViewById(R.id.rv_wednesday_slots);
        rvDaysSlots[3] = findViewById(R.id.rv_thursday_slots);
        rvDaysSlots[4] = findViewById(R.id.rv_friday_slots);
        rvDaysSlots[5] = findViewById(R.id.rv_saturday_slots);

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

        pd = new ProgressDialog(MainActivity.this, R.style.CustomProgressDialog);
        pd.setMessage("Loading Slots");
        pd.setCancelable(false);
        pd.show();
        pd.setContentView(R.layout.progress_dialog_layout);
        getSubjectTeacherShortName();
        getAllSlots(6);
        try {
            for (int i=0; i<6; i++){
                rvDaysSlots[i].setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                slotList.add(new ArrayList<>());
                slotRvAdapters[i] = new SlotRvAdapter(MainActivity.this, slotList.get(i));
                rvDaysSlots[i].setAdapter(slotRvAdapters[i]);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        fabAddSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bsAddSlot = new BottomSheetDialog(MainActivity.this);
                bsAddSlot.setCancelable(true);
                View v = getLayoutInflater().inflate(R.layout.add_slot_bottom_sheet, (ViewGroup) findViewById(R.id.bs_root_layout));
                bsAddSlot.setContentView(v);

                imgClose = bsAddSlot.findViewById(R.id.img_close_bs);
                imgAddSlot = bsAddSlot.findViewById(R.id.img_add_slot);
                btnAddTime = bsAddSlot.findViewById(R.id.btn_add_time);
                tvShowTime = bsAddSlot.findViewById(R.id.tv_show_time);
                tvError = bsAddSlot.findViewById(R.id.tv_error);
                spDay = bsAddSlot.findViewById(R.id.sp_day);
                spSubject = bsAddSlot.findViewById(R.id.sp_subject);
                bsAddSlot.show();

                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bsAddSlot.dismiss();
                    }
                });

                imgAddSlot.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view) {
                        dayOfWeek = spDay.getSelectedItem().toString();
                        sub = spSubject.getSelectedItem().toString();
                        positionOfDay = spDay.getSelectedItemPosition();
                        positionOfSubject = spSubject.getSelectedItemPosition();

                        if(dayOfWeek.equals("Select Day")){
                            tvError.setText("Select day!");
                        }else if(sub.equals("Select Subject")){
                            tvError.setText("Select subject!");
                        }else if(strTime.equals("")){
                            tvError.setText("Set time!");
                        }else {
                            subCode = hmSubCode.get(sub);
                            addNewSlot();
                            bsAddSlot.dismiss();
                        }
                    }
                });
                btnAddTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TimePickerDialog tpd = new TimePickerDialog(
                                MainActivity.this, R.style.MyTimePickerDialog,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                        if (hour < 10 && minute < 10){
                                            strTime = "0"+hour+":"+"0"+minute;
                                        }else if (hour < 10){
                                            strTime = "0"+hour+":"+minute;
                                        }else if (minute < 10){
                                            strTime = hour+":"+"0"+minute;
                                        }else { 
                                            strTime = hour+":"+minute;
                                        }
                                        tvShowTime.setText("Time: "+strTime);
                                    }
                                }, 10, 30, true);
                        tpd.show();
                    }
                });
            }
        });
    }
    public void getSubjectTeacherShortName(){
        String tName = fbAuth.getCurrentUser().getEmail().toString();
        if(tName.startsWith("mahendra")){
            subTeacherShortName = "mnp";
        }else if(tName.startsWith("suresh")){
            subTeacherShortName = "sbp";
        }else if(tName.startsWith("prashant")){
            subTeacherShortName = "pc";
        }else if(tName.startsWith("sashi")){
            subTeacherShortName = "svr";
        }else if(tName.startsWith("aswin")){
            subTeacherShortName = "akr";
        }else if(tName.startsWith("vashishtha")){
            subTeacherShortName = "vjp";
        }else if(tName.startsWith("anamika")){
            subTeacherShortName = "am";
        }else if(tName.startsWith("ruturaj")){
            subTeacherShortName = "rpr";
        }else if(tName.startsWith("darshak")){
            subTeacherShortName = "dbm";
        }
    }

    public void addNewSlot(){
        try {
            slotNums=0;
            dbRefList.child(slotDaysList.get(positionOfDay-1)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                        slotNums++;
                        int i = 2;
                        while (fbData != null){
                            fbData = snapshot.child("slot"+i).getValue(FbData.class);
                            slotNums++;
                            i++;
                        }
                    } catch (Exception e) {}
                    finally {
                        dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotNums).child("subcode").setValue(subCode);
                        dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotNums).child("subteacher").setValue(subTeacherShortName.toUpperCase());
                        dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotNums).child("subject").setValue(sub);
                        dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotNums).child("subtime").setValue(strTime);
                        dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotNums).child("template").setValue((positionOfSubject-1)%6);
                        dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotNums).child("presentcnt").setValue(0);
                        getAllSlots(positionOfDay-1);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyAdapter(int i){
        slotRvAdapters[i].notifyDataSetChanged();
    }

    public void getAllSlots(int in){
        index = in;
        switch(in){
            case 0:
                dbRefList.child("mon").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(index).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "mon"));
                            }
                            notifyAdapter(index);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "mon"));
                                }
                                notifyAdapter(index);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
            case 1:
                dbRefList.child("tue").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(index).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "tue"));
                            }
                            notifyAdapter(index);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "tue"));
                                }
                                notifyAdapter(index);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
            case 2:
                dbRefList.child("wed").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(index).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "wed"));
                            }
                            notifyAdapter(index);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "wed"));
                                }
                                notifyAdapter(index);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
            case 3:
                dbRefList.child("thu").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(index).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "thu"));
                            }
                            notifyAdapter(index);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "thu"));
                                }
                                notifyAdapter(index);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
            case 4:
                dbRefList.child("fri").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(index).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "fri"));
                            }
                            notifyAdapter(index);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "fri"));
                                }
                                notifyAdapter(index);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
            case 5:
                dbRefList.child("sat").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(index).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "sat"));
                            }
                            notifyAdapter(index);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(index).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "sat"));
                                }
                                notifyAdapter(index);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
            case 6:
                dbRefList.child("mon").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            slotList.get(0).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(0).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "mon"));
                            }
                            notifyAdapter(0);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(0).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "mon"));
                                }
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
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(1).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "tue"));
                            }
                            notifyAdapter(1);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(1).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "tue"));
                                }
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
                            Log.d("bhen", "index: "+index);
                            slotList.get(2).clear();
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(2).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "wed"));
                            }
                            notifyAdapter(2);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(2).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "wed"));
                                }
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
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(3).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "thu"));
                            }
                            notifyAdapter(3);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(3).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "thu"));
                                }
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
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(4).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "fri"));
                            }
                            notifyAdapter(4);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(4).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "fri"));
                                }
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
                            Log.d("bhen", "indexsat: "+index);
                            FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                            if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                slotList.get(5).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "sat"));
                            }
                            notifyAdapter(5);
                            int i = 2;
                            while (fbData != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                if(fbData.subteacher.toUpperCase().equals(subTeacherShortName.toUpperCase())){
                                    slotList.get(5).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher, "sat"));
                                }
                                notifyAdapter(5);
                                i++;
                            }
                        } catch (Exception e) {
                            
                        }
                        finally {
                            pd.hide();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        fbAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginCA.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}