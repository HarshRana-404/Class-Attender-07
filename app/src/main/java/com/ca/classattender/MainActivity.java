package com.ca.classattender;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabAddSlot;
    ImageButton imgClose, imgAddSlot;

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

                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bsAddSlot.dismiss();
                    }
                });

                imgAddSlot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                bsAddSlot.show();
            }
        });
    }
}