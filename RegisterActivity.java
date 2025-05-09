package com.example.shreyash;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etErpid, etMobile, etName;
    private Button btnRegister;
    private TextView textView;
    private FirebaseAuth auth;
    private DatabaseReference tpdb1 = FirebaseDatabase.getInstance().getReference("Faculty");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etErpid = findViewById(R.id.etErpid);
        etMobile = findViewById(R.id.etMobile);
        etName = findViewById(R.id.etName);
        btnRegister = findViewById(R.id.btnRegister);

        textView = findViewById(R.id.tvLogin);
        auth = FirebaseAuth.getInstance();


        textView.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String erpid = etErpid.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String name = etName.getText().toString().trim();


        if (!name.isEmpty() && !erpid.isEmpty() && !mobile.isEmpty() && !email.isEmpty()) {

            Map<String, String> subject = new HashMap<>();
            Faculty faculty = new Faculty(name, erpid, mobile, email);
            tpdb1.child(erpid).setValue(faculty);

        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(this, "Registration Successful. Verify your email!", Toast.LENGTH_LONG).show();
                            auth.signOut();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}