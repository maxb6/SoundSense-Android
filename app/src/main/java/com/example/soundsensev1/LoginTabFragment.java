package com.example.soundsensev1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginTabFragment extends Fragment {

    private EditText emailET;
    private EditText passwordET;
    private TextView forgotPasswordTV;
    private Button loginButton;
    private ProgressBar loginProgressBar;

    private FirebaseAuth mAuth;

    private SharedPreferencesHelper spHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.login_tab_fragment, container, false);

        emailET = root.findViewById(R.id.emailSignInEditText);
        passwordET = root.findViewById(R.id.passwordSignInEditText);
        forgotPasswordTV = root.findViewById(R.id.forgotPasswordSignInTextView);
        loginButton = root.findViewById(R.id.loginSignInButton);
        loginProgressBar = root.findViewById(R.id.loginProgressBar);

        emailET.setTranslationX(800);
        passwordET.setTranslationX(800);
        forgotPasswordTV.setTranslationX(800);
        loginButton.setTranslationX(800);

        emailET.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        passwordET.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        forgotPasswordTV.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        loginButton.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();

        spHelper = new SharedPreferencesHelper(container.getContext());
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPasswordActivity();
            }
        });

        return root;
    }

    protected void userLogin(){
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        //check if fields are empty
        if(email.isEmpty()){
            emailET.setError("Email is required!");
            emailET.requestFocus();
            return;
        }

        if(password.isEmpty()){
            passwordET.setError("Password is required!");
            passwordET.requestFocus();
            return;
        }

        //check if email is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError("Please provide valid email.");
            emailET.requestFocus();
            return;
        }
        //check if password length is sufficient
        if(password.length() < 6){
            passwordET.setError("Password must be at least 6 characters!");
            passwordET.requestFocus();
            return;
        }

        loginProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    //edit shared preferences to set activity_executed to true
                    spHelper.setUserLogIn(true);
                    //redirect to main activity
                    goToMainActivity();

                }
                else{
                    try{
                        FirebaseAuthException e = (FirebaseAuthException)task.getException();
                        Toast.makeText(getContext(),"Failed to login! Please check your credentials. ", Toast.LENGTH_LONG).show();
                    }
                    catch(Exception e){
                        Toast.makeText(getContext(),"Connection Failure! ", Toast.LENGTH_LONG).show();
                    }

                }
                loginProgressBar.setVisibility(View.GONE);
            }
        });

    }

    protected void goToMainActivity(){
        Intent intent = new Intent (getContext(),MainActivity.class);
        startActivity(intent);
    }

    protected void goToPasswordActivity(){
        Intent intent = new Intent (getContext(),PasswordActivity.class);
        startActivity(intent);
    }

}
