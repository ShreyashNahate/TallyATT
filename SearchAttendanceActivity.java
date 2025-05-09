package com.example.shreyash;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SearchAttendanceActivity extends AppCompatActivity {

    private TextView btnStartDate, btnEndDate;
    private Button btnFetchAttendance;
    private Spinner spinnerSubjectSearch;
    private EditText editTextRollNumber; // New EditText for roll number input
    private RecyclerView recyclerViewAttendanceResults;

    private DatabaseReference attendanceRef, rootRef; // Reference to Firebase Attendance data
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private List<AttendanceRecord2> attendanceRecords2 = new ArrayList<>();
    private AttendanceRecordAdapter adapter;
    private static final int WRITE_EXTERNAL_STORAGE = 1;

    private String startDate, endDate, selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_attendance);

        btnStartDate = findViewById(R.id.editTextStartDate);
        btnEndDate = findViewById(R.id.editTextEndDate);
        spinnerSubjectSearch = findViewById(R.id.spinnerSubjectSearch);
        btnFetchAttendance = findViewById(R.id.btnFetchAttendance);
        editTextRollNumber = findViewById(R.id.editTextRollNumber); // Initialize roll number input
        recyclerViewAttendanceResults = findViewById(R.id.recycler_view_attendance_results);
        FloatingActionButton btnGenerateExcel = findViewById(R.id.btnGenerateExcel);
        // Initialize Firebase reference
        attendanceRef = FirebaseDatabase.getInstance().getReference("Students123");
        rootRef = FirebaseDatabase.getInstance().getReference("Students123");

        // Populate subject spinner
        populateSubjects();

        // Set up RecyclerView
        recyclerViewAttendanceResults.setLayoutManager(new LinearLayoutManager(this));

        // Date selection using DatePickerDialog
        btnStartDate.setOnClickListener(view -> showDatePickerDialog(true));
        btnEndDate.setOnClickListener(view -> showDatePickerDialog(false));

        // Fetch Attendance Records
        btnFetchAttendance.setOnClickListener(view -> fetchAttendanceRecords());
        btnGenerateExcel.setOnClickListener(view -> generateExcelFile());
    }

    private void populateSubjects() {
        String[] subjects = {"M3", "CG", "DBMS", "PA", "SE", "PA Lab", "DBMS Lab", "CG Lab", "M3 Tutorial","PBL"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item,
                subjects);

        spinnerSubjectSearch.setAdapter(subjectAdapter);
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    if (isStartDate) {
                        startDate = selectedDate;
                        btnStartDate.setText("Start Date: " + startDate);
                    } else {
                        endDate = selectedDate;
                        btnEndDate.setText("End Date: " + endDate);
                    }
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void fetchAttendanceRecords() {
        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedSubject = (String) spinnerSubjectSearch.getSelectedItem();
        String rollNumber = "SEIT"+editTextRollNumber.getText().toString().trim(); // Get roll number input


        attendanceRecords.clear(); // Clear previous records
//        attendanceRecords2.clear();
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(SearchAttendanceActivity.this, "No data found ", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String enrollment = studentSnapshot.child("enrollment").getValue(String.class);
                    String name = studentSnapshot.child("name").getValue(String.class);


                    if (rollNumber.isEmpty() || enrollment.contains(rollNumber)) {
                        DataSnapshot subjectSnapshot = studentSnapshot.child("Subjects").child(selectedSubject);
                        if (!subjectSnapshot.exists()) {
                            Toast.makeText(SearchAttendanceActivity.this, "No data found ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                            int present = subjectSnapshot.child("present_count").getValue(Integer.class);
                            int absent = subjectSnapshot.child("absent_count").getValue(Integer.class);
                            int totalLecture = present + absent;
                            int percentage = subjectSnapshot.child("percentage").getValue(Integer.class);
                            attendanceRecords2.add(new AttendanceRecord2(enrollment, name, selectedSubject, present, absent,totalLecture,percentage));


                        if (subjectSnapshot.exists()) {
                            DataSnapshot attendanceSnapshot = subjectSnapshot.child("Attendence");

                            for (DataSnapshot dateSnapshot : attendanceSnapshot.getChildren()) {
                                String dateKey = dateSnapshot.getKey();


                                DataSnapshot attendanceSnapshot2 = subjectSnapshot.child("Attendence").child(dateKey);

                                for(DataSnapshot dateSnapshot2 : attendanceSnapshot2.getChildren()) {

                                    String time = dateSnapshot2.getKey();


                                    DataSnapshot attendanceSnapshot3 = subjectSnapshot.child("Attendence").child(dateKey).child(time);
                                    String status = attendanceSnapshot3.child("status").getValue(String.class);
                                    boolean inRange = isDateInRange(dateKey, startDate, endDate);


                                    if (inRange) {
                                        attendanceRecords.add(new AttendanceRecord(enrollment,status, dateKey+" ( "+time+" )",selectedSubject ,name  ));
                                    }
                                }
                            }
                        }
                    }
                }

                updateRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchAttendanceActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private boolean isDateInRange(String dateKey, String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date current = sdf.parse(dateKey);
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            return current != null && !current.before(start) && !current.after(end);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateRecycler() {
        if (adapter == null) {
            adapter = new AttendanceRecordAdapter(attendanceRecords);
            recyclerViewAttendanceResults.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with generating Excel file
                generateExcelFile();
            } else {
                // Permission denied, notify user
                Toast.makeText(this, "Permission denied. Cannot save the file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateExcelFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // For Android versions below 10 (API level 29), request permission
            saveFileScopedStorage(attendanceRecords2);
        }
    }
    private void saveFileScopedStorage(List<AttendanceRecord2> attendanceRecords2) {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Summary");

        // Header 1
        Row header1 = sheet.createRow(0);
        header1.createCell(0).setCellValue("Roll No");
        header1.createCell(1).setCellValue("Name");
        header1.createCell(2).setCellValue("M3");
        header1.createCell(5).setCellValue("DBMS");
        header1.createCell(8).setCellValue("PA");
        header1.createCell(11).setCellValue("CG");
        header1.createCell(14).setCellValue("SE");
        header1.createCell(17).setCellValue("M3 Tutorial");
        header1.createCell(20).setCellValue("DBMS Lab");
        header1.createCell(23).setCellValue("PA Lab");
        header1.createCell(26).setCellValue("CG Lab");
        header1.createCell(29).setCellValue("PBL");
        header1.createCell(32).setCellValue("Total Lectures");
        header1.createCell(34).setCellValue("Total Present");
        header1.createCell(36).setCellValue("% Attendance");
        header1.createCell(38).setCellValue("");

        // Header 2 (sub-columns)
        Row header2 = sheet.createRow(1);
        for (int i = 2; i <= 31; i += 3) {
            header2.createCell(i).setCellValue("Lectures");
            header2.createCell(i + 1).setCellValue("Present");
            header2.createCell(i + 2).setCellValue("%");
        }

        // Step 1: Group by student
        Map<String, Map<String, AttendanceRecord2>> studentMap = new LinkedHashMap<>();

        for (AttendanceRecord2 record : attendanceRecords2) {
            String key = record.getEnrollmentNumber() + "_" + record.getName();
            studentMap.putIfAbsent(key, new HashMap<>());
            studentMap.get(key).put(record.getSubject(), record);
        }

        // Subject column mapping
        Map<String, Integer> subjectColumnMap = new HashMap<>();
        subjectColumnMap.put("M3", 2);
        subjectColumnMap.put("DBMS", 5);
        subjectColumnMap.put("PA", 8);
        subjectColumnMap.put("CG", 11);
        subjectColumnMap.put("SE", 14);
        subjectColumnMap.put("M3 Tutorial", 17);
        subjectColumnMap.put("DBMS Lab", 20);
        subjectColumnMap.put("PA Lab", 23);
        subjectColumnMap.put("CG Lab", 26);
        subjectColumnMap.put("PBL", 29);

        int rowIndex = 2;
        for (String studentKey : studentMap.keySet()) {
            String[] parts = studentKey.split("_");
            String enrollment = parts[0];
            String name = parts[1];

            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(enrollment);
            row.createCell(1).setCellValue(name);

            int totalLec = 0;
            int totalPre = 0;
            float aggregate = 0;

            Map<String, AttendanceRecord2> subjectMap = studentMap.get(studentKey);

            for (String subject : subjectMap.keySet()) {
                AttendanceRecord2 rec = subjectMap.get(subject);
                int col = subjectColumnMap.getOrDefault(subject, -1);

                if (col != -1) {
                    row.createCell(col).setCellValue(rec.getTotalLecture());
                    row.createCell(col + 1).setCellValue(rec.getPresentCount());
                    row.createCell(col + 2).setCellValue(rec.getPercentage());

                    totalLec += rec.getTotalLecture();
                    totalPre += rec.getPresentCount();
                }
            }

            aggregate =((100*totalPre)/totalLec);

            row.createCell(32).setCellValue(totalLec);
            row.createCell(34).setCellValue(totalPre);
            row.createCell(36).setCellValue(aggregate);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(new File(getFilesDir(), "attendance_records2new.xlsx"));
            workbook.write(fileOut);
            fileOut.close();
            Toast.makeText(this, "Excel file generated successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate Excel file", Toast.LENGTH_SHORT).show();
        }
    }

}
