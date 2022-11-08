package com.ca.classattender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SlotRvAdapter extends RecyclerView.Adapter<SlotRvAdapter.ViewHolder> {

    Context context;
    ArrayList<SlotModel> slotList;
    int pos;
    HashMap<String, String> hmDay= new HashMap<>();
    String subDayFull="", otpGen="";
    DatabaseReference dbRefIT = FirebaseDatabase.getInstance().getReference("class_attender/otps/it");
    int n = 0;


    public SlotRvAdapter(Context context, ArrayList<SlotModel> slotList){
        this.context = context;
        this.slotList = slotList;
        hmDay.put("mon", "Monday");
        hmDay.put("tue", "Tuesday");
        hmDay.put("wed", "Wednesday");
        hmDay.put("thu", "Thursday");
        hmDay.put("fri", "Friday");
        hmDay.put("sat", "Saturday");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View slotItemView = LayoutInflater.from(context).inflate(R.layout.slot_rv_layout, parent, false);
        ViewHolder slotViewHolder = new ViewHolder(slotItemView);
        return slotViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            holder.ivSlotTemplate.setImageResource(slotList.get(position).slotTemplate);
            holder.tvSubName.setText(slotList.get(position).subName);
            holder.tvSlotTime.setText(slotList.get(position).slotTime);
            holder.tvSubCode.setText(slotList.get(position).subCode);
            holder.tvSubTeacher.setText(slotList.get(position).subTeacher);

            holder.ivSlotTemplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pos = position;

                    dbRefIT.child(slotList.get(position).subDay).addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("MissingInflatedId")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                subDayFull = hmDay.get(slotList.get(pos).subDay.toLowerCase());
                                FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                                if(fbData.subject.toUpperCase().equals(slotList.get(position).subName) && fbData.subteacher.toUpperCase().equals(slotList.get(position).subTeacher) && fbData.subtime.toUpperCase().equals(slotList.get(position).slotTime)){
                                    String otpGen = fbData.otp;
                                    if(!otpGen.equals("") || fbData.presentcnt>0){
                                        AlertDialog.Builder adb = new AlertDialog.Builder(context);
                                        adb.setTitle(slotList.get(position).subName+" - " + slotList.get(position).slotTime+" on "+subDayFull);
                                        adb.setCancelable(true);
                                        adb.setMessage("OTP: "+otpGen);
                                        adb.setPositiveButton("VIEW IN FULLSCREEN", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent inFUllScreenOTP = new Intent(context, FullScreenOTP.class);
                                                inFUllScreenOTP.putExtra("otp", otpGen);
                                                context.startActivity(inFUllScreenOTP);
                                            }
                                        });
                                        adb.setNeutralButton("VIEW PRESENCE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent inViewPresence = new Intent(context, ViewPresence.class);
                                                inViewPresence.putExtra("slotno", "slot"+1);
                                                inViewPresence.putExtra("subday", slotList.get(position).subDay);
                                                String title = slotList.get(position).subName+" - " + slotList.get(position).slotTime+" on "+subDayFull;
                                                inViewPresence.putExtra("subtitle", title);
                                                context.startActivity(inViewPresence);
                                            }
                                        });
                                        adb.show();
                                    }else{
                                        otpIsNeeded(position);
                                    }
                                }
                                int sn = 2;
                                while (fbData != null){
                                    n = sn;
                                    fbData = snapshot.child("slot"+sn).getValue(FbData.class);
                                    if(fbData.subject.toUpperCase().equals(slotList.get(position).subName) && fbData.subteacher.toUpperCase().equals(slotList.get(position).subTeacher) && fbData.subtime.toUpperCase().equals(slotList.get(position).slotTime)){
                                        otpGen = fbData.otp;
                                        if(!otpGen.equals("") || fbData.presentcnt>0){
                                            AlertDialog.Builder adb = new AlertDialog.Builder(context);

                                            adb.setTitle(slotList.get(position).subName+" - " + slotList.get(position).slotTime+" on "+subDayFull);
                                            adb.setCancelable(true);
                                            adb.setMessage("OTP: "+otpGen);

                                            adb.setPositiveButton("VIEW IN FULLSCREEN", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent inFUllScreenOTP = new Intent(context, FullScreenOTP.class);
                                                    inFUllScreenOTP.putExtra("otp", otpGen);
                                                    context.startActivity(inFUllScreenOTP);
                                                }
                                            });
                                            int finalSn = sn;
                                            adb.setNeutralButton("VIEW PRESENCE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent inViewPresence = new Intent(context, ViewPresence.class);
                                                    inViewPresence.putExtra("slotno", "slot"+ finalSn);
                                                    inViewPresence.putExtra("subday", slotList.get(position).subDay);
                                                    String title = slotList.get(position).subName+" - " + slotList.get(position).slotTime+" on "+subDayFull;
                                                    inViewPresence.putExtra("subtitle", title);
                                                    context.startActivity(inViewPresence);
                                                }
                                            });
                                            adb.show();
                                        }else{
                                            otpIsNeeded(position);
                                        }
                                    }
                                    sn++;
                                }
                            } catch (Exception e) {

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @SuppressLint("MissingInflatedId")
    public void otpIsNeeded(int position){
        TextView tvSubName, tvSubCode, tvSubTime, tvSubTeacher;
        Button cancelBtn, generateOtpBtn;
        ImageView ivSlotTemplate;

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        View customDialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, (ViewGroup) null);
        adb.setView(customDialogView);
        adb.setCancelable(true);
        AlertDialog alertDialog = adb.create();

        tvSubName = customDialogView.findViewById(R.id.tv_sub_name_dialog);
        tvSubCode = customDialogView.findViewById(R.id.tv_sub_code_dialog);
        tvSubTime = customDialogView.findViewById(R.id.tv_slot_time_dialog);
        tvSubTeacher = customDialogView.findViewById(R.id.tv_sub_teacher_dialog);
        ivSlotTemplate = customDialogView.findViewById(R.id.iv_slot_template_dialog);
        cancelBtn = customDialogView.findViewById(R.id.cancel_btn);
        generateOtpBtn = customDialogView.findViewById(R.id.generate_otp_btn);

        tvSubName.setText(slotList.get(position).subName);
        tvSubCode.setText(slotList.get(position).subCode);
        tvSubTime.setText(slotList.get(position).slotTime);
        tvSubTeacher.setText(slotList.get(position).subTeacher);
        ivSlotTemplate.setBackgroundResource(slotList.get(position).slotTemplate);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        generateOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateOTP go = new GenerateOTP(context);
                go.generateNewOTP(slotList.get(position).subDay, slotList.get(position).subName, slotList.get(position).slotTime, slotList.get(position).subTeacher);
            }
        });
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSlotTemplate;
        TextView tvSubName, tvSlotTime, tvSubCode, tvSubTeacher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                ivSlotTemplate = itemView.findViewById(R.id.iv_slot_template);
                tvSubName = itemView.findViewById(R.id.tv_sub_name);
                tvSlotTime = itemView.findViewById(R.id.tv_slot_time);
                tvSubCode = itemView.findViewById(R.id.tv_sub_code);
                tvSubTeacher = itemView.findViewById(R.id.tv_sub_teacher);
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
