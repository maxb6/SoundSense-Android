package com.example.soundsensev1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordActivity extends AppCompatActivity {

    TabLayout tabLayout;
    private EditText emailEditText;
    private Button resetPasswordButton;
    private Button cancelResetPasswordButton;
    private ProgressBar passwordProgressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        tabLayout = findViewById(R.id.tab_layout);
        emailEditText = findViewById(R.id.editTextEmailPassword);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        cancelResetPasswordButton = findViewById(R.id.cancelResetPasswordButton);
        passwordProgressBar = findViewById(R.id.passwordProgressBar);

        auth = FirebaseAuth.getInstance();

        tabLayout.addTab(tabLayout.newTab().setText("Forgot Password"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        cancelResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){ goToLoginActivity(); }
        });

    }

    protected void resetPassword(){
        String email = emailEditText.getText().toString().trim();

        //check if email field is empty
        if(email.isEmpty()){
            emailEditText.setError("Email is required!");
            emailEditText.requestFocus();
            return;
        }

        //check if email is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Please provide valid email.");
            emailEditText.requestFocus();
            return;
        }

        passwordProgressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(PasswordActivity.this,"Check your email to reset your password", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(PasswordActivity.this,"Error occured! Email not sent", Toast.LENGTH_LONG).show();
                }
                passwordProgressBar.setVisibility(View.GONE);
                goToLoginActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    protected void goToLoginActivity(){
        Intent intent = new Intent (this,LoginActivity.class);
        startActivity(intent);
    }
}