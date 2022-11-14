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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabAddSlot;
    ImageButton imgClose, imgAddSlot;
    Button btnAddTime;
    TextView tvShowTime, tvError;
    Spinner spDay, spSubject;
    int positionOfSubject=0, positionOfDay=0, index=0, slotNums=0, indexOfTvDays=0;
    String sub="", strTime="", subTeacherShortName="", subCode="", dayOfWeek = "";
    RecyclerView rvDaysSlots[] = new RecyclerView[6];
    ArrayList<ArrayList<SlotModel>> slotList = new ArrayList<>();
    ArrayList<Integer> slotTemplateImg = new ArrayList<>();
    ArrayList<String> slotDaysList = new ArrayList<>();
    SlotRvAdapter slotRvAdapters[] = new SlotRvAdapter[6];
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DatabaseReference dbRefList = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    HashMap<String,String> hmSubCode = new HashMap<>();
    ProgressDialog progressDialog;
    LinearLayout llWeekDays[] = new LinearLayout[6];
    ImageView ivWeekDays[] = new ImageView[6];

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

        llWeekDays[0] = findViewById(R.id.ll_monday);
        llWeekDays[1] = findViewById(R.id.ll_tuesday);
        llWeekDays[2] = findViewById(R.id.ll_wednesday);
        llWeekDays[3] = findViewById(R.id.ll_thursday);
        llWeekDays[4] = findViewById(R.id.ll_friday);
        llWeekDays[5] = findViewById(R.id.ll_saturday);

        ivWeekDays[0] = findViewById(R.id.iv_monday);
        ivWeekDays[1] = findViewById(R.id.iv_tuesday);
        ivWeekDays[2] = findViewById(R.id.iv_wednesday);
        ivWeekDays[3] = findViewById(R.id.iv_thursday);
        ivWeekDays[4] = findViewById(R.id.iv_friday);
        ivWeekDays[5] = findViewById(R.id.iv_saturday);

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

        // Showing Progress Dialog
        progressDialog = new ProgressDialog(MainActivity.this, R.style.CustomProgressDialog);
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.progress_dialog_layout, (ViewGroup) findViewById(R.id.progress_root_layout));
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(v);

        getSubjectTeacherShortName();
        getAllSlots(6);

        // Setting LayoutManager and Adapters to RecyclerViews
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

        try {
            // Only Current Day's Slots are visible
            for (int i=0; i<6; i++){
                rvDaysSlots[i].setVisibility(View.GONE);
                rvDaysSlots[i].animate().translationX(500.0f).setDuration(300);
            }
            int indexOfWeekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            rvDaysSlots[(indexOfWeekDay-2)%6].setVisibility(View.VISIBLE);
            rvDaysSlots[(indexOfWeekDay-2)%6].animate().translationX(0.0f).setDuration(300);
            ivWeekDays[(indexOfWeekDay-2)%6].animate().rotation(90.0f);
        } catch (Exception e) {}

        // Making all days collapsable and expandable
        llWeekDaysOnClickListeners();

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
                        TimePickerDialog tprogressDialog = new TimePickerDialog(
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
                        tprogressDialog.show();
                    }
                });
            }
        });
    }

    private void llWeekDaysOnClickListeners() {
        llWeekDays[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseExpandRv(0);
            }
        });

        llWeekDays[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseExpandRv(1);
            }
        });

        llWeekDays[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseExpandRv(2);
            }
        });

        llWeekDays[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseExpandRv(3);
            }
        });

        llWeekDays[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseExpandRv(4);
            }
        });

        llWeekDays[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseExpandRv(5);
            }
        });
    }

    private void collapseExpandRv(int i) {
        Animation translateRv = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate_days_rv);
        if (rvDaysSlots[i].getVisibility() == View.VISIBLE){
            ivWeekDays[i].animate().rotation(0.0f);
            rvDaysSlots[i].animate().translationX(600.0f).setDuration(300);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rvDaysSlots[i].setVisibility(View.GONE);
                }
            }, 300);
        }else {
            rvDaysSlots[i].setVisibility(View.VISIBLE);
//            rvDaysSlots[i].startAnimation(translateRv);
            rvDaysSlots[i].animate().translationX(0.0f).setDuration(300);
            ivWeekDays[i].animate().rotation(90.0f);
        }
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
                        if(rvDaysSlots[positionOfDay-1].getVisibility() == View.GONE){
                            collapseExpandRv(positionOfDay-1);
                        }
                        strTime="";
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
                            progressDialog.hide();
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