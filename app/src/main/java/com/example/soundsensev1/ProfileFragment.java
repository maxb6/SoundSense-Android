package com.example.soundsensev1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private EditText editName;
    private EditText editEmail;
    private Button saveButton;

    private FirebaseUser user;
    private static DatabaseReference reference;
    private String userID;

    private SharedPreferencesHelper spHelper;

    private static DatabaseReference userReference;
    private static DatabaseReference sensorControlReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.profile_fragment, container, false);

        setHasOptionsMenu(true);

        Toolbar toolbar = root.findViewById(R.id.mainToolbar);
        getActivity().setActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Profile");

        spHelper = new SharedPreferencesHelper(getActivity());

        //initializing editTexts and button
        editName = root.findViewById(R.id.editProfileName);
        editEmail = root.findViewById(R.id.editProfileEmail);
        saveButton = root.findViewById(R.id.saveButton);


        //By default have the editTexts not be editable by user
        editName.setEnabled(false);
        editEmail.setEnabled(false);


        //firebase authentication

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        sensorControlReference = FirebaseDatabase.getInstance().getReference().child("Device").child("ON&OFF");

        //get user info from firebase

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String name = userProfile.name;
                    String email = userProfile.email;

                    editName.setText(name);
                    editEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),"User info error!",Toast.LENGTH_LONG).show();

            }
        });

        exitEditMode();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.editItem:
                enterEditMode();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void enterEditMode(){
        //reveal the save button
        saveButton.setVisibility(View.VISIBLE);
        //when editMode is active, allow user to input in editTexts
        //By default have the editTexts not be editable by user
        editName.setEnabled(true);
        editEmail.setEnabled(true);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                user = FirebaseAuth.getInstance().getCurrentUser();

                user.updateEmail(String.valueOf(editEmail.getText()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    userReference.child("name").setValue(editName.getText().toString());
                                    userReference.child("email").setValue(editEmail.getText().toString());
                                    final String TAG = "EmailUpdate";
                                    Log.d(TAG, "User email address updated.");
                                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                    Toast.makeText(getActivity().getApplicationContext(), "Re-login to edit your profile!", Toast.LENGTH_LONG).show();
                                    goToLoginActivity();
                                }
                            }
                        });
                exitEditMode();

            }
        });
    }

    protected void exitEditMode(){
        //when we exit editMode, we return to displayMode
        //make editTexts uneditable
        //hide the save button
        editName.setEnabled(false);
        editEmail.setEnabled(false);
        saveButton.setVisibility(View.INVISIBLE);
    }

    protected void goToLoginActivity(){
        Intent intent = new Intent (getActivity(),LoginActivity.class);
        startActivity(intent);
    }
}
