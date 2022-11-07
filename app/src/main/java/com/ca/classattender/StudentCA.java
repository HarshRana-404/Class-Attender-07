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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Calendar;
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
    LinearLayout llWeekDays[] = new LinearLayout[6];
    ImageView ivWeekDays[] = new ImageView[6];


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
        Animation translateRv = AnimationUtils.loadAnimation(StudentCA.this, R.anim.translate_days_rv);
        if (rvDaysSlots[i].getVisibility() == View.VISIBLE){
            ivWeekDays[i].animate().rotation(0.0f);
            rvDaysSlots[i].animate().translationX(500.0f).scaleX(0.2f).scaleY(0.2f).setDuration(300);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rvDaysSlots[i].setVisibility(View.GONE);
                }
            }, 300);
        }else {
            rvDaysSlots[i].setVisibility(View.VISIBLE);
//            rvDaysSlots[i].startAnimation(translateRv);
            rvDaysSlots[i].animate().translationX(0.0f).scaleX(1).scaleY(1).setDuration(300);
            ivWeekDays[i].animate().rotation(90.0f);
        }
    }
}