package com.example.shreyash;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordHolder> {

    private List<AttendanceRecord> attendanceRecords;

    public AttendanceRecordAdapter(List<AttendanceRecord> attendanceRecords){
        this.attendanceRecords=attendanceRecords;
    }

    @NonNull
    @Override
    public AttendanceRecordHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        return new AttendanceRecordHolder(LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceRecordHolder holder,int position){
        AttendanceRecord record=attendanceRecords.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount(){
        return attendanceRecords.size();
    }
}
