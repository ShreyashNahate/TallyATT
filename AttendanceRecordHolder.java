package com.example.shreyash;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceRecordHolder extends RecyclerView.ViewHolder {

    private TextView text1,text2;

    public AttendanceRecordHolder(@NonNull View itemview){
        super(itemview);
        text1=itemview.findViewById(android.R.id.text1);
        text2=itemview.findViewById(android.R.id.text2);
    }

    public void bind(AttendanceRecord record){
        text1.setText(record.getEnrollmentNumber() +" - " +record.getStatus());
        text2.setText(record.getTimestamp());
    }
}
