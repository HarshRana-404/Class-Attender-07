package com.ca.classattender;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabAddSlot;
    ImageButton imgClose, imgAddSlot;
    Button btnAddTime;
    TextView tvShowTime;
    Spinner spDay, spSubject;
    int hr, min;
    String strTime="";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAddSlot = findViewById(R.id.fab_add_slot);

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
                        String dy = spDay.getSelectedItem().toString();
                        String sub = spSubject.getSelectedItem().toString();
                        if(dy.equals("Select Day")){
                            Toast.makeText(MainActivity.this, "Select Day!", Toast.LENGTH_SHORT).show();
                        }else if(sub.equals("Select Subject")){
                            Toast.makeText(MainActivity.this, "Select Subject!", Toast.LENGTH_SHORT).show();
                        }else if(strTime.equals("")){
                            Toast.makeText(MainActivity.this, "Select Time!", Toast.LENGTH_SHORT).show();
                        }else {

                        }
                    }
                });

                btnAddTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TimePickerDialog tpd = new TimePickerDialog(
                                MainActivity.this, android.R.style.Theme_Material_Dialog_MinWidth,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                        strTime = String.valueOf(i)+":"+String.valueOf(i1);
                                        tvShowTime.setText("Time Selected-"+strTime);
                                    }
                                }, 10, 30, true
                        );
                        tpd.show();
                    }
                });
            }
        });
    }
}