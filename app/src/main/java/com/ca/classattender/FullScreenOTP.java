package com.ca.classattender;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class FullScreenOTP extends AppCompatActivity {

    ImageButton imgBack;
    TextView tvOTP;
    String OTP;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_otp);
        imgBack = findViewById(R.id.img_back);
        tvOTP = findViewById(R.id.tv_otp);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FullScreenOTP.this, MainActivity.class));
            }
        });
        Intent in = getIntent();
        OTP = in.getExtras().getString("otp");
        tvOTP.setText(OTP);
        tvOTP.setTextSize(100);
        Animation animRotate;
        animRotate = AnimationUtils.loadAnimation(FullScreenOTP.this, R.anim.otp_full_screen_rotate);
        tvOTP.startAnimation(animRotate);
    }
}