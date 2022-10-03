package com.ca.classattender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity<arr> extends AppCompatActivity {

    FloatingActionButton fabAddSlot;
    ImageButton imgClose, imgAddSlot;
    Button btnAddTime;
    TextView tvShowTime, tvError;
    Spinner spDay, spSubject;
    int hr, min;
    int dayIndex;
    String strTime="";
//    RecyclerView rvMondaySlots, rvTuesdaySlots, rvWednesdaySlots, rvThursdaySlots, rvFridaySlots, rvSaturdaySlots;
    RecyclerView rvDaysSlots[] = new RecyclerView[6];
    ArrayList<ArrayList<SlotModel>> slotList = new ArrayList<>();
    ArrayList<Integer> slotTemplateImg = new ArrayList<>();
    SlotRvAdapter slotRvAdapters[] = new SlotRvAdapter[6];

//    ArrayList<String> subAl = new ArrayList<>();
    DatabaseReference dbRefList = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    ArrayList<String> slotDaysList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        slotDaysList.add("mon");
        slotDaysList.add("tue");
        slotDaysList.add("wed");
        slotDaysList.add("thu");
        slotDaysList.add("fri");
        slotDaysList.add("sat");

//        subAl.add("DS");
//        subAl.add("ETC");
//        subAl.add("DF");
//        subAl.add("PAS");

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

//        try {
//            for (String day: slotDaysList){
//                for (int i=1; i<=3; i++){
//                    dbRefList.child(day).child("slot"+i).child("subject").setValue(day + " " + subAl.get(i-1));
//                    dbRefList.child(day).child("slot"+i).child("subcode").setValue("3130004");
//                    dbRefList.child(day).child("slot"+i).child("subteacher").setValue("MNP");
//                    dbRefList.child(day).child("slot"+i).child("subtime").setValue("10:30");
//                }
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
//        }

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
//                            slotList.get(positionOfDay-1).add(new SlotModel(slotTemplateImg.get((positionOfSubject-1)%6), sub, strTime, 3130004, "MNP"));
//                            slotRvAdapters[positionOfDay-1].notifyDataSetChanged();

                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("template").setValue((positionOfSubject-1)%6);
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subject").setValue(sub);
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subcode").setValue("3130004");
                            dbRefList.child(slotDaysList.get(positionOfDay-1)).child("slot"+slotList.get(positionOfDay-1).size()+1).child("subteacher").setValue("MNP");
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

    public void getAllSlots(){
        Toast.makeText(this, "In getAllSlots", Toast.LENGTH_SHORT).show();
        try {
            slotList.clear();
            for (dayIndex=0; dayIndex<slotDaysList.size(); dayIndex++){
                slotList.add(new ArrayList<>());
                dbRefList.child(slotDaysList.get(dayIndex)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            FbData fbData = snapshot.child("slot1").getValue(FbData.class);
                            slotList.get(dayIndex).add(new SlotModel(slotTemplateImg.get(fbData.template), fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
                            Toast.makeText(MainActivity.this, "after adding slot", Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, fbData.subject+fbData.subcode+fbData.subteacher+fbData.subtime, Toast.LENGTH_SHORT).show();
                            int i = 2;
                            while (fbData.subject != null){
                                fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                slotList.get(dayIndex).add(new SlotModel(fbData.template, fbData.subject, fbData.subtime, fbData.subcode, fbData.subteacher));
                                Toast.makeText(MainActivity.this, fbData.subject+fbData.subcode+fbData.subteacher+fbData.subtime, Toast.LENGTH_SHORT).show();
                                i++;
                            }
                            slotRvAdapters[dayIndex].notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}