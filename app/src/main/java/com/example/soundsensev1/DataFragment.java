package com.example.soundsensev1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DataFragment extends Fragment {

    protected ListView sensorListView;
    private DatabaseReference userReference;
    private ArrayList<String> fbSensorValues = new ArrayList<>();

    private Button deleteListButton;
    ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.data_fragment, container, false);

        Toolbar toolbar = root.findViewById(R.id.mainToolbar);
        getActivity().setActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("History");

        //adapter
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_simple_list_item_1, fbSensorValues);

        //load sensor value
        sensorListView = root.findViewById(R.id.sensorListView);
        //reference to firebase for user sensor data
        userReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("sensorValues");

        printUserSensorValues();

        deleteListButton = root.findViewById(R.id.deleteListButton);
        deleteListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userReference.removeValue();
                adapter.notifyDataSetChanged();
                adapter.clear();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    protected void printUserSensorValues(){

        //retrieve sensor values for specific user from firebase
        //display these values in a list view

        userReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String currentSensorValue = snapshot.getValue(String.class);
                Log.i("data activity", "recent value from fb: " + currentSensorValue);
                fbSensorValues.add(currentSensorValue);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        sensorListView.setAdapter(adapter);
    }
}
