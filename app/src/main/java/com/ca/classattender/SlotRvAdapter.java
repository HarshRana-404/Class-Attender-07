package com.ca.classattender;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SlotRvAdapter extends RecyclerView.Adapter<SlotRvAdapter.ViewHolder> {

    Context context;
    ArrayList<SlotModel> slotList;

    public SlotRvAdapter(Context context, ArrayList<SlotModel> slotList){
        this.context = context;
        this.slotList = slotList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View slotItemView = LayoutInflater.from(context).inflate(R.layout.slot_rv_layout, parent, false);
        ViewHolder slotViewHolder = new ViewHolder(slotItemView);
        return slotViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.ivSlotTemplate.setImageResource(slotList.get(position).slotTemplate);
            holder.tvSubName.setText(slotList.get(position).subName);
            holder.tvSlotTime.setText(slotList.get(position).slotTime);
            holder.tvSubCode.setText(slotList.get(position).subCode);
            holder.tvSubTeacher.setText(slotList.get(position).subTeacher);

            holder.ivSlotTemplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(context);
                    adb.setTitle("OTP");
                    adb.setCancelable(true);
                    adb.setPositiveButton("GENERATE OTP", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    adb.show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
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
