package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    int dayIndex=0;
    String strTime="", subTeacherShortName="", subCode="";
//    RecyclerView rvMondaySlots, rvTuesdaySlots, rvWednesdaySlots, rvThursdaySlots, rvFridaySlots, rvSaturdaySlots;
    RecyclerView rvDaysSlots[] = new RecyclerView[6];
    ArrayList<ArrayList<SlotModel>> slotList = new ArrayList<>();
    ArrayList<Integer> slotTemplateImg = new ArrayList<>();
    ArrayList<String> slotDaysList = new ArrayList<>();
    SlotRvAdapter slotRvAdapters[] = new SlotRvAdapter[6];
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/");
    DatabaseReference dbRefIT = FirebaseDatabase.getInstance().getReferenceFromUrl("https://class-attender-07-default-rtdb.firebaseio.com/class_attender/otps/it");
    DatabaseReference dbRefList = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    HashMap<String,String> hmSubCode = new HashMap<>();
    String dbName="", dbCode="", dbTeacher="", dbTime="";



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

        getSubjectTeacherShortName();

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
                    @Override
                    public void onClick(View view) {
                        String dayOfWeek = spDay.getSelectedItem().toString();
                        String sub = spSubject.getSelectedItem().toString();
                        int positionOfDay = spDay.getSelectedItemPosition();
                        int positionOfSubject = spSubject.getSelectedItemPosition();

                        if(dayOfWeek.equals("Select Day")){
                            tvError.setText("Select day!");
                        }else if(sub.equals("Select Subject")){
                            tvError.setText("Select subject!");
                        }else if(strTime.equals("")){
                            tvError.setText("Set time!");
                        }else {
                            subCode = hmSubCode.get(sub);

//                            dbRef.child("class_attender").child("otps").child("it").child(dayOfWeek.toLowerCase()).child(strTime).child(subCode).setValue(subTeacherShortName);
//                            dbRef.child("class_attender").child("otps").child("it").child(dayOfWeek.toLowerCase()).child(strTime).child("subject").setValue(sub);
////                            dbRef.child("class_attender").child("otps").child("it").child(dayOfWeek.toLowerCase()).child(sub).setValue(dayOfWeek.toLowerCase());
//
//                            slotList.get(positionOfDay-1).add(new SlotModel(slotTemplateImg.get((positionOfSubject-1)%6), sub, strTime, subCode, subTeacherShortName.toUpperCase()));
//                            slotRvAdapters[positionOfDay-1].notifyDataSetChanged();
//                            bsAddSlot.dismiss();

//                            slotList.get(positionOfDay-1).add(new SlotModel(slotTemplateImg.get((positionOfSubject-1)%6), sub, strTime, 3130004, "MNP"));
//                            slotRvAdapters[positionOfDay-1].notifyDataSetChanged();

                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("template").setValue((positionOfSubject-1)%6);
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subject").setValue(sub);
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subcode").setValue(subCode);
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subteacher").setValue(subTeacherShortName.toUpperCase());
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subtime").setValue(strTime);

                            getAllSlots();
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
        }
    }

    public void getAllSlots(){
        try {
            slotList.clear();
            slotList.add(new ArrayList<>());
            dbRefList.child("mon").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        FbData fbData = snapshot.child("slot0"+1).getValue(FbData.class);
                        Toast.makeText(MainActivity.this, fbData.subject+", "+fbData.subcode+", "+fbData.subteacher+", "+fbData.subtime, Toast.LENGTH_SHORT).show();
                        slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
                        int i = 2;
                        while (fbData.subject != null){
                            fbData = snapshot.child("slot0"+i).getValue(FbData.class);
                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
                            i++;
                        }
                    } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();                        slotRvAdapters[0].notifyDataSetChanged();
                    }finally {
                        slotRvAdapters[0].notifyDataSetChanged();
                        slotList.get(0).notifyAll();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
//            dbRefList.child("tue").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    try {
//                        FbData fbData = snapshot.child("slot0"+1).getValue(FbData.class);
//                        slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                        int i = 2;
//                        while (fbData.subject != null){
//                            fbData = snapshot.child("slot0"+i).getValue(FbData.class);
//                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                            i++;
//                        }
//                        slotRvAdapters[dayIndex].notifyDataSetChanged();
//                    } catch (Exception e) {
//                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });
//            dbRefList.child("wed").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    try {
//                        FbData fbData = snapshot.child("slot0"+1).getValue(FbData.class);
//                        slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                        int i = 2;
//                        while (fbData.subject != null){
//                            fbData = snapshot.child("slot0"+i).getValue(FbData.class);
//                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                            i++;
//                        }
//                        slotRvAdapters[dayIndex].notifyDataSetChanged();
//                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });
//            dbRefList.child("thu").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    try {
//                        FbData fbData = snapshot.child("slot0"+1).getValue(FbData.class);
//                        slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                        int i = 2;
//                        while (fbData.subject != null){
//                            fbData = snapshot.child("slot0"+i).getValue(FbData.class);
//                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                            i++;
//                        }
//                        slotRvAdapters[dayIndex].notifyDataSetChanged();
//                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });
//            dbRefList.child("fri").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    try {
//                        FbData fbData = snapshot.child("slot0"+1).getValue(FbData.class);
//                        slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                        int i = 2;
//                        while (fbData.subject != null){
//                            fbData = snapshot.child("slot0"+i).getValue(FbData.class);
//                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                            i++;
//                        }
//                        slotRvAdapters[dayIndex].notifyDataSetChanged();
//                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });
//            dbRefList.child("sat").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    try {
//                        FbData fbData = snapshot.child("slot0"+1).getValue(FbData.class);
//                        slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                        int i = 2;
//                        while (fbData.subject != null){
//                            fbData = snapshot.child("slot0"+i).getValue(FbData.class);
//                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                            i++;
//                        }
//                        slotRvAdapters[dayIndex].notifyDataSetChanged();
//                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });

        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
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














//    public void getAllSlots(){
//        try {
//            slotList.clear();
//            for(int j=0; j<slotDaysList.size(); j++){
//                dayIndex=j;
//                slotList.add(new ArrayList<>());
//                dbRefList.child(slotDaysList.get(j)).addValueEventListener(new ValueEventListener() {
//                    @SuppressLint("NotifyDataSetChanged")
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        try {
//                            Toast.makeText(MainActivity.this, "slot0"+dayIndex, Toast.LENGTH_SHORT).show();
//                            FbData fbData = snapshot.child("slot0"+dayIndex+1).getValue(FbData.class);
//                            Toast.makeText(MainActivity.this, fbData.subject+fbData.subcode+fbData.subteacher+fbData.subtime, Toast.LENGTH_SHORT).show();
//                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                            Toast.makeText(MainActivity.this, "after adding slot", Toast.LENGTH_SHORT).show();
//                            int i = 2;
//                            while (fbData.subject != null){
//                                fbData = snapshot.child("slot0"+i+1).getValue(FbData.class);
//                                slotList.get(dayIndex).add(new SlotModel(fbData.template, fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
//                                Toast.makeText(MainActivity.this, fbData.subject+fbData.subcode+fbData.subteacher+fbData.subtime, Toast.LENGTH_SHORT).show();
//                                i++;
//                            }
//                            slotRvAdapters[dayIndex].notifyDataSetChanged();
//                        } catch (Exception e) {
//                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
//        }
//    }