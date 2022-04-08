/*
References:
CodeWithMazn, #1 Login and Registration Android App Tutorial Using Firebase Authentication - Create User.  [Video] Available: https://www.youtube.com/watch?v=Z-RE1QuUWPg. [Accessed: 03-Apr-2021].
 */

package com.example.soundsensev1;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpTabFragment extends Fragment {

    private EditText emailET;
    private EditText passwordET;
    private EditText nameET;
    private Button signUpButton;
    private ProgressBar registerProgressBar;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.signup_tab_fragment, container, false);

        nameET = root.findViewById(R.id.nameSignUpEditText);
        emailET = root.findViewById(R.id.emailSignUpEditText);
        passwordET = root.findViewById(R.id.passwordSignUpEditText);
        signUpButton = root.findViewById(R.id.signUpButton);
        registerProgressBar = root.findViewById(R.id.signUpProgressBar);

        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        return root;
    }

    private void registerUser() {

        //convert user inputs to strings
        String name = nameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        //check if fields are empty
        if(name.isEmpty()){
            nameET.setError("Name is required!");
            nameET.requestFocus();
            return;
        }

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

        //add user to firebase and check if task has been completed
        registerProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            User user = new User(name,email);

                            //obtain id of newly registered user
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getContext(),"User has been registered successfully",Toast.LENGTH_LONG).show();
                                        //goToLoginActivity();
                                    }else{
                                        Toast.makeText(getContext(),"Failed to register! Try again. ",Toast.LENGTH_LONG).show();
                                    }
                                    registerProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                        else{
                            try {
                                FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                Toast.makeText(getContext(), "Failed to register!", Toast.LENGTH_LONG).show();
                                registerProgressBar.setVisibility(View.GONE);
                            }
                            catch(Exception e){
                                Toast.makeText(getContext(), "Connection Failure! ", Toast.LENGTH_LONG).show();
                                registerProgressBar.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

}

