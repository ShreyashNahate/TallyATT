package com.example.shreyash;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Main2 extends AppCompatActivity {
    private boolean isAllSelected = false;

    private Spinner spinnerAddStudent, spinnerType, spinnerSubject;
    private EditText editTextName, editTextEnrollment, editTextMobile, editTextEmail;
    private TextView selectALL;
    private Button btnAddStudent, btnPickStartTime, btnPickEndTime, btnNotify, btnSearchAttendance; // Added button
    private ListView listViewStudents; // This will be replaced with RecyclerView in SearchAttendanceActivity
    private LinearLayout studentDetailsLayout;

    private DatabaseReference tpdb2 = FirebaseDatabase.getInstance().getReference("Students123");
    private DatabaseReference tpdb1 = FirebaseDatabase.getInstance().getReference("Students123");
    private List<String> studentList = new ArrayList<>();
    private ArrayAdapter<String> studentAdapter;

    public static final ArrayList<String> absentMobileList = new ArrayList<>();
    public static final ArrayList<String> absentEmailList = new ArrayList<>();

    private String selectedType = "Lecture", selectedSubject = "", startTime = "", endTime = "";
    private String[] lectureSubjects = {"M3", "CG", "DBMS", "PA", "SE"};
    private String[] labSubjects = {"CG Lab", "DBMS Lab", "PA Lab", "M3 Tutorial","PBL"};
    private static final String MESSAGE = "Hello , You are Absent for Today's Lecture . Kindly Report to the TG immedieatly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(Main2.this, LoginActivity.class));
            finish();
        }

        loadStudents();

        spinnerAddStudent = findViewById(R.id.spinnerAddStudent);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        editTextName = findViewById(R.id.editTextName);
        editTextEnrollment = findViewById(R.id.editTextEnrollment);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        listViewStudents = findViewById(R.id.listViewStudents);
        studentDetailsLayout = findViewById(R.id.studentDetailsLayout);
        btnPickStartTime = findViewById(R.id.btnPickStartTime);
        btnPickEndTime = findViewById(R.id.btnPickEndTime);
        btnNotify = findViewById(R.id.btnNotify);
        btnSearchAttendance = findViewById(R.id.btnSearchAttendance); // Initialize search button
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextEmail = findViewById(R.id.editTextEmail);
        selectALL = findViewById(R.id.all);

        // Logout functionality
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(Main2.this, LoginActivity.class)); // Redirect to Login Page
            finish();
        });

        // Spinner setup for adding students
        String[] addStudentOptions = { "No", "Yes" };
        ArrayAdapter<String> addStudentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, addStudentOptions);
        addStudentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddStudent.setAdapter(addStudentAdapter);

        // Spinner item selected listener
        spinnerAddStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                studentDetailsLayout.setVisibility(position == 1 ? View.VISIBLE : View.GONE);  // Show/Hide student details layout
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        selectALL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAllSelected) {
                    // Select all items
                    for (int i = 0; i < studentAdapter.getCount(); i++) {
                        listViewStudents.setItemChecked(i, true);
                    }
                    isAllSelected = true;
                } else {
                    // Unselect all items
                    for (int i = 0; i < studentAdapter.getCount(); i++) {
                        listViewStudents.setItemChecked(i, false);
                    }
                    isAllSelected = false;
                }
            }
        });


        // Save student data to Firebase when the save button is clicked
        btnAddStudent.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            String enrollment = editTextEnrollment.getText().toString().trim();
            if(!enrollment.contains("SEIT")){
                enrollment = "SEIT"+enrollment;
            }
            String mobile = editTextMobile.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            if (!name.isEmpty() && !enrollment.isEmpty() && !mobile.isEmpty() && !email.isEmpty()) {
                Faculty student = new Faculty(name, enrollment, mobile, email);
                tpdb1.child(enrollment).setValue(student);

                Toast.makeText(Main2.this, "Student Added", Toast.LENGTH_SHORT).show();

                // Clear input fields
                editTextName.setText("");
                editTextEnrollment.setText("");
                editTextMobile.setText("");
                editTextEmail.setText("");
            } else {
                Toast.makeText(Main2.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Notify button logic (absent students)
        btnNotify.setOnClickListener(view -> notifyAbsentStudents());

        updateSubjectSpinner();

        // Spinner setup for type (Lecture/Lab)
        String[] typeOptions = {"Lecture", "Lab"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, typeOptions);

        spinnerType.setAdapter(typeAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = typeOptions[position];
                updateSubjectSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Time picker logic for start and end time
        btnPickStartTime.setOnClickListener(view -> pickTime(btnPickStartTime, true));
        btnPickEndTime.setOnClickListener(view -> pickTime(btnPickEndTime, false));

        // Search Attendance button click listener
        btnSearchAttendance.setOnClickListener(view -> {
            Intent intent = new Intent(Main2.this, SearchAttendanceActivity.class); // Navigate to search activity
            startActivity(intent);
        });
    }

    private void notifyAbsentStudents() {
        SparseBooleanArray checkedItems = listViewStudents.getCheckedItemPositions();
        absentEmailList.clear();
        absentMobileList.clear();

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


        AtomicInteger totalAbsentStudents = new AtomicInteger(0);
        AtomicInteger processedStudents = new AtomicInteger(0);

        for (int i = 0; i < listViewStudents.getCount(); i++) {
            String studentData = studentList.get(i); //
            String enrollment = studentData.split(" - ")[1];

            boolean isAbsent = checkedItems.get(i);
            String status = isAbsent ? "Absent" : "Present";

            if (!"Present".equals(status)) {
                totalAbsentStudents.incrementAndGet();
                tpdb1.child(enrollment).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String mobile = snapshot.child("mobile").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);

                            absentMobileList.add(mobile);
                            absentEmailList.add(email);

                        }
                        if (processedStudents.incrementAndGet() == totalAbsentStudents.get()) {
                            allDataFetched();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        processedStudents.incrementAndGet();
                    }
                });
            }
            startTime = btnPickStartTime.getText().toString();
            endTime = btnPickEndTime.getText().toString();
            String timestamp= startTime + endTime;
            Map<String, Object> attendanceMap = new HashMap<>();
            attendanceMap.put("status", status);
            attendanceMap.put("timestamp", timestamp);
            tpdb1.child(enrollment).child("Subjects").child(selectedSubject).child("Attendence").child(date).child(startTime + " - "+ endTime).setValue(attendanceMap);
            tpdb2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long present = 0, absent = 0;

                    DataSnapshot subjectSnap = snapshot.child(enrollment)
                            .child("Subjects")
                            .child(selectedSubject);

                    if (subjectSnap.child("present_count").exists()) {
                        present = subjectSnap.child("present_count").getValue(Long.class);
                    }else{
                        tpdb2.child(enrollment)
                                .child("Subjects")
                                .child(selectedSubject)
                                .child("present_count")
                                .setValue(present);
                    }

                    if (subjectSnap.child("absent_count").exists()) {
                        absent = subjectSnap.child("absent_count").getValue(Long.class);
                    }else{
                        tpdb2.child(enrollment)
                                .child("Subjects")
                                .child(selectedSubject)
                                .child("absent_count")
                                .setValue(present);
                    }

                    // Update count based on status
                    if (status.equals("Present")) {
                        present += 1;
                        tpdb2.child(enrollment)
                                .child("Subjects")
                                .child(selectedSubject)
                                .child("present_count")
                                .setValue(present);
                    } else {
                        absent += 1;
                        tpdb2.child(enrollment)
                                .child("Subjects")
                                .child(selectedSubject)
                                .child("absent_count")
                                .setValue(absent);
                    }

                    // Calculate and update percentage
                    long total = present + absent;
                    double percentage = (total > 0) ? (present * 100.0) / total : 0;

                    tpdb2.child(enrollment)
                            .child("Subjects")
                            .child(selectedSubject)
                            .child("percentage")
                            .setValue(percentage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FIREBASE", "Error updating count: " + error.getMessage());
                }
            });


        }
    }

    private void updateSubjectSpinner() {
        String[] subjects = selectedType.equals("Lecture") ? lectureSubjects : labSubjects;
        ArrayAdapter<String> subjectAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        subjects);

        spinnerSubject.setAdapter(subjectAdapter);

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view,
                                       int position,
                                       long id) {
                selectedSubject= subjects[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void pickTime(Button button, boolean isStartTime) {
        Calendar calendar= Calendar.getInstance();
        int hour= calendar.get(Calendar.HOUR_OF_DAY);
        int minute= calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog= new TimePickerDialog(this,
                (view,hourOfDay,min) -> {
                    String amPm= (hourOfDay<12)? "AM": "PM";
                    int hour12= (hourOfDay==0)? 12: (hourOfDay>12? hourOfDay-12: hourOfDay);
                    String time= String.format("%02d:%02d %s", hour12,min ,amPm);

//                    button.setText("From : "+time);

                    if(isStartTime){
                        button.setText("From : "+time);
                        startTime= time;
                    } else{
                        button.setText("To : "+time);
                        endTime= time;
                    }
                }, hour ,minute ,false);

        timePickerDialog.show();
    }

    private void loadStudents() {
        tpdb1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for(DataSnapshot studentSnapshot: snapshot.getChildren()){
                    Student student= studentSnapshot.getValue(Student.class);
                    if(student != null && student.getEnrollment() != null){
                        studentList.add(student.getName()+" - "+student.getEnrollment());
                    }
                }

                studentAdapter= new ArrayAdapter<>(Main2.this,
                        android.R.layout.simple_list_item_multiple_choice ,studentList);
                listViewStudents.setAdapter(studentAdapter);
                listViewStudents.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Main2.this,"Failed to load students" ,Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void sendSmsToAll() {
        for(String phoneNumber: absentMobileList){
            try{
                SmsManager smsManager= SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber ,null ,MESSAGE ,null ,null);
            }catch(Exception e){
                Toast.makeText(this ,"SMS Failed for: "+phoneNumber ,Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    private void allDataFetched() {


        CheckBox checkboxEmail = findViewById(R.id.checkbox_email);
        CheckBox checkboxMessage = findViewById(R.id.checkbox_message);

        // Notify the user that all data has been processed
        if (absentEmailList.isEmpty()) {
            Toast.makeText(Main2.this, "No students selected for email!", Toast.LENGTH_SHORT).show();
            return;
        }



        if (checkboxEmail.isChecked() && checkboxMessage.isChecked()) {
            new SendMailTask(absentEmailList).execute();
            sendSmsToAll();
            Toast.makeText(Main2.this, "Sending Email and Message", Toast.LENGTH_SHORT).show();
        } else if (checkboxEmail.isChecked()) {
            new SendMailTask(absentEmailList).execute();
            Toast.makeText(Main2.this, "Sending Email", Toast.LENGTH_SHORT).show();
        } else if (checkboxMessage.isChecked()) {
            sendSmsToAll();
            Toast.makeText(Main2.this, "Sending Message", Toast.LENGTH_SHORT).show();
        } else {
            handleSendAction(checkboxEmail,checkboxMessage);
        }
    }
    public void handleSendAction(CheckBox checkboxEmail , CheckBox checkboxMessage) {
        if (checkboxEmail.isChecked() && checkboxMessage.isChecked()) {
            new SendMailTask(absentEmailList).execute();
            sendSmsToAll();
            Toast.makeText(Main2.this, "Sending Email and Message", Toast.LENGTH_SHORT).show();
        } else if (checkboxEmail.isChecked()) {
            new SendMailTask(absentEmailList).execute();
            Toast.makeText(Main2.this, "Sending Email", Toast.LENGTH_SHORT).show();
        } else if (checkboxMessage.isChecked()) {
            sendSmsToAll();
            Toast.makeText(Main2.this, "Sending Message", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Main2.this, "Please select at least one option", Toast.LENGTH_SHORT).show();

            // Repeat the flow (e.g., show dialog or call again)
            // Optional: add delay before retry
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleSendAction(checkboxEmail,checkboxMessage); // repeat the flow
                }
            }, 2000); // delay of 2 seconds before repeating
        }
    }




    public class SendMailTask extends AsyncTask<Void ,Void ,Void>{
        private String userEmail="shreyash4585@gmail.com";
        private String userPassword="lbfb zjyh sthh twbg";
        private List<String> emailList;

        public SendMailTask(List<String> selectedEmails){
            this.emailList= absentEmailList;
        }

        @Override
        protected Void doInBackground(Void... voids){
            if(!emailList.isEmpty()){
                sendEmailsInParallel();
            }else{
                Log.e("SendMailTask" ,"No student emails selected.");
            }
            return null;
        }

        private void sendEmailsInParallel(){
            Log.d("SendMailTask" ,"Preparing to send emails...");

            try{
                Properties props= new Properties();
                props.put("mail.smtp.auth" ,"true");
                props.put("mail.smtp.starttls.enable" ,"true");
                props.put("mail.smtp.host" ,"smtp.gmail.com");
                props.put("mail.smtp.port" ,"587");

                Session session= Session.getInstance(props,new Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication(userEmail,userPassword);
                    }
                });

                Thread emailThread= new Thread(() -> {
                    try{
                        Message message= new MimeMessage(session);

                        try{
                            message.setFrom(new InternetAddress(userEmail,"Shreyash Nahate"));
                        }catch(UnsupportedEncodingException e){
                            Log.e("SendMailTask" ,"Encoding error in sender name" ,e);
                            message.setFrom(new InternetAddress(userEmail));
                        }

                        message.setReplyTo(InternetAddress.parse("support@yourcollege.com"));

                        for(String email: emailList){
                            message.addRecipient(Message.RecipientType.BCC,new InternetAddress(email));
                        }

                        message.setHeader("Content-Type","text/html; charset=UTF-8");
                        message.setHeader("X-Priority","1");
                        message.setHeader("X-Mailer","AndroidMailer");
                        message.setHeader("Precedence","bulk");

                        String emailBody="<h2>Hello Student ,<h2>" + "<p>This is an <strong>official announcement</strong> from your college.</p>" + "<p>You are Absent for Today's Lecture . Kindly Report to Your TG .</p>" + "<br><p>Regards,<br><strong>Your College</strong></p>";

                        message.setSubject("📢 Important College Notice");
                        message.setContent(emailBody,"text/html; charset=utf-8");

                        Transport.send(message);
                        Log.d("SendMailTask" ,"✅ Email sent successfully!");

                    }catch(MessagingException e){
                        Log.e("SendMailTask" ,"❌ Error sending email" ,e);
                    }
                });

                emailThread.start();

            }catch(Exception e){
                Log.e("SendMailTask" ,"❌ Error preparing email" ,e);
            }
        }
    }
}
