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
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                subDayFull = hmDay.get(slotList.get(pos).subDay.toLowerCase());
                                FbData fbData = snapshot.child("slot"+1).getValue(FbData.class);
                                if(fbData.subject.toUpperCase().equals(slotList.get(position).subName) && fbData.subteacher.toUpperCase().equals(slotList.get(position).subTeacher) && fbData.subtime.toUpperCase().equals(slotList.get(position).slotTime)){
                                    String otpGen = fbData.otp;
                                    if(!otpGen.equals("")){
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
                                        adb.show();
                                    }else{
                                        otpIsNeeded();
                                    }
                                }
                                int i = 2;
                                while (fbData != null){
                                    fbData = snapshot.child("slot"+i).getValue(FbData.class);
                                    if(fbData.subject.toUpperCase().equals(slotList.get(position).subName) && fbData.subteacher.toUpperCase().equals(slotList.get(position).subTeacher) && fbData.subtime.toUpperCase().equals(slotList.get(position).slotTime)){
                                        otpGen = fbData.otp;
                                        if(!otpGen.equals("")){
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
                                            adb.show();
                                        }else{
                                            otpIsNeeded();
                                        }
                                    }
                                    i++;
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
    
    public void otpIsNeeded(){
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(slotList.get(pos).subName+" - " + slotList.get(pos).slotTime+" on "+subDayFull);
        adb.setCancelable(true);
        adb.setMessage("");
        adb.setPositiveButton("GENERATE OTP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                GenerateOTP go = new GenerateOTP(context);
                go.generateNewOTP(slotList.get(pos).subDay, slotList.get(pos).subName, slotList.get(pos).slotTime, slotList.get(pos).subTeacher);
            }
        });
        adb.show();
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
