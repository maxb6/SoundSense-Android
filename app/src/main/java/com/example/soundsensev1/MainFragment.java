/*
References:
Google, Read and Write Data on Android. [Online]. Available: https://firebase.google.com/docs/database/android/read-and-write. [Accessed: 01-Apr-2021].
Educatree, How to retrieve data from Firebase in Android || Retrieve data from firebase || Android Firebase #4. [Video] Available: https://www.youtube.com/watch?v=LpWhAz3e1sI. [Accessed: 03-Apr-2021].
 */

package com.example.soundsensev1;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.soundsensev1.SharedPreferencesHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class MainFragment extends Fragment {

    private TextView thresholdView;
    private Button mainButton;
    private TextView minuteCountTV;
    private TextView hourCountTV;
    private TextView dayCountTV;

    private static FirebaseUser user;
    private static String userID;

    private static DatabaseReference sensorControlReference;
    private static DatabaseReference reference;
    private static DatabaseReference userReference;
    private static DatabaseReference analogReference;
    private static DatabaseReference userCountReference;
    private static DatabaseReference sensorRangeReference;

    private SharedPreferencesHelper spHelper;

    private boolean buttonOn;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    private String minuteCountString;
    private String hourCountString;
    private String dayCountString;

    private String mode;

    private static final String TAG = "MainActivity";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.main_fragment, container, false);

        Toolbar toolbar = root.findViewById(R.id.mainToolbar);
        getActivity().setActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Home");


        thresholdView = root.findViewById(R.id.thresholdView);
        mainButton = root.findViewById(R.id.mainButton);
        minuteCountTV = root.findViewById(R.id.minuteCountTV);

        hourCountTV = root.findViewById(R.id.hourCountTV);
        dayCountTV = root.findViewById(R.id.dayCountTV);
        spHelper = new SharedPreferencesHelper(getActivity());

        //firebase authentication
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        if(user!=null) {
            userID = user.getUid();
        }

        //reference to firebase to retrieve input sensor data
        analogReference = FirebaseDatabase.getInstance().getReference().child("Sensor").child("Analog");

        userCountReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("thresholdCounts");

        //button settings
        userReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("sensorValues");
        sensorControlReference = FirebaseDatabase.getInstance().getReference().child("Device").child("ON&OFF");

        sensorRangeReference = FirebaseDatabase.getInstance().getReference().child("Range").child("Number");

        //controls for the button
        analogReference.setValue(0);

        //setControlSwitchValue();
        storeUserSensorValues();
        setButtonValue();
        setThresholdCounts();

        Intent intent = new Intent();
        String minuteTV = intent.getStringExtra("minuteTV");
        minuteCountTV.setText(minuteTV);
        String hourTV = intent.getStringExtra("hourTV");
        hourCountTV.setText(hourTV);
        String dayTV = intent.getStringExtra("dayTV");
        dayCountTV.setText(dayTV);

        //get user info from firebase
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String name = userProfile.name;
                    if (name!="" || name!=null){
                        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Hello, "+name+"!");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),"User info error!",Toast.LENGTH_LONG).show();
            }
        });

        sensorRangeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Integer.parseInt(snapshot.getValue().toString())==1){
                    thresholdView.setText("Selected Mode: Sensitive");
                    mode = "Sensitive";
                }
                else if (Integer.parseInt(snapshot.getValue().toString())==2){
                    thresholdView.setText("Selected Mode: Loud");
                    mode = "Loud";
                }
                else{
                    sensorRangeReference.setValue(1);
                    thresholdView.setText("Selected Mode: Sensitive");
                    mode = "Sensitive";
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        //user can turn on or off using the button depending on the firebase value
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonOn){
                    sensorControlReference.setValue(0);
                    buttonOFF();
                }
                else{
                    sensorControlReference.setValue(1);
                    buttonON();
                }
            }
        });

        return root;
    }

    protected void setButtonValue(){

        mainButton.setText("Connecting\n...");

        sensorControlReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //read the current firebase value of device and convert it to int
                String controlString = snapshot.getValue().toString();
                int controlInt = Integer.parseInt(controlString);
                Log.i("MainActivity Switch","control switch: "+controlInt);

                //set the button to whatever the current firebase value is
                if(controlInt==0){
                    buttonOFF();
                }
                else if (controlInt==1){
                    buttonON();
                }
                else{
                    Toast.makeText(getActivity(), "ON/OFF error!", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Button error!", Toast.LENGTH_LONG).show();
            }
        });
    }


    protected void displayWarning() {

        //retrieve sensorvalues for specific user from firebase
        //display warnings values in activity button
        analogReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int currentSensorValue = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                new CountDownTimer(2000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if (currentSensorValue!=0){
                            mainButton.setClickable(false);
                            mainButton.setText("Too loud!");
                            mainButton.setBackgroundResource(R.drawable.circular_button_red);
                        }
                    }
                    public void onFinish() {
                        mainButton.setClickable(true);
                        mainButton.setText("All Good!");
                        mainButton.setBackgroundResource(R.drawable.circular_button_green);
                    }
                }.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Warning error!", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void buttonOFF(){
        analogReference.setValue(0);
        mainButton.setText("Tap to\nturn\nON");
        mainButton.setBackgroundResource(R.drawable.circular_button);
        buttonOn = false;
    }

    protected void buttonON(){

        mainButton.setClickable(false);
        displayWarning();
        mainButton.setBackgroundResource(R.drawable.circular_button);
        mainButton.setText("Connecting\n...");

        buttonOn = true;
    }

    protected void storeUserSensorValues() {
        //reference to firebase to retrieve sensor data
        analogReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //timeStamp initialization
                calendar = Calendar.getInstance();
                dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                date = dateFormat.format(calendar.getTime());
                String dateString = "    \nDate/Time: "+ date;

                String value = Objects.requireNonNull(snapshot.getValue()).toString();

                if(spHelper.getRecentSensorValue()==null){
                    spHelper.setRecentSensorValue("0");
                    Log.i("MainActivity", "recent value: " + spHelper.getRecentSensorValue());
                }

                //if sensor value isnt the same as recent value, upload the value to the firebase database
                if (!spHelper.getRecentSensorValue().equals(value) && Integer.parseInt(value)!=0) {
                    userReference.push().setValue("Selected Mode: "+mode+dateString);

                    //store recent sensor value in shared prefs
                    spHelper.setRecentSensorValue(value);
                    Log.i("MainActivity", "recent value: " + spHelper.getRecentSensorValue());

                    incrementThresholdCounts();

                    //start service to send notification
                    if(spHelper.getNotification()){
                        startService();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Sensor Value error!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void setThresholdCounts(){

        //minutesCount
        userCountReference.child("minuteCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    userCountReference.child("minuteCount").setValue(0);
                    minuteCountTV.setText("0");
                    spHelper.setMinuteThresholdCount(0);
                }
                else {
                    minuteCountString = snapshot.getValue().toString();
                    spHelper.setMinuteThresholdCount(Integer.valueOf(minuteCountString));
                    minuteCountTV.setText(minuteCountString);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //hourCount
        userCountReference.child("hourCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    userCountReference.child("hourCount").setValue(0);
                    hourCountTV.setText("0");
                    spHelper.setHourlyThresholdCount(0);
                }
                else {
                    hourCountString = snapshot.getValue().toString();
                    spHelper.setHourlyThresholdCount(Integer.valueOf(hourCountString));
                    hourCountTV.setText(hourCountString);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //dayCount
        userCountReference.child("dayCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    userCountReference.child("dayCount").setValue(0);
                    dayCountTV.setText("0");
                    spHelper.setDailyThresholdCount(0);
                }
                else {
                    dayCountString = snapshot.getValue().toString();
                    spHelper.setDailyThresholdCount(Integer.valueOf(dayCountString));
                    dayCountTV.setText(dayCountString);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void incrementThresholdCounts(){

        int minuteCountInt = spHelper.getMinuteThresholdCount();
        ++minuteCountInt;
        userCountReference.child("minuteCount").setValue(minuteCountInt);
        Log.i(TAG,String.valueOf(minuteCountInt));
        minuteCountTV.setText(String.valueOf(minuteCountInt));

        //minute count: get firebase value, increment the value, print the value
        userCountReference.child("minuteCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                minuteCountString =  snapshot.getValue().toString();
                spHelper.setMinuteThresholdCount(Integer.valueOf(minuteCountString));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        int hourCountInt = spHelper.getHourlyThresholdCount();
        ++hourCountInt;
        userCountReference.child("hourCount").setValue(hourCountInt);
        Log.i(TAG,String.valueOf(hourCountInt));
        minuteCountTV.setText(String.valueOf(hourCountInt));

        userCountReference.child("hourCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hourCountString =  snapshot.getValue().toString();
                spHelper.setHourlyThresholdCount(Integer.valueOf(hourCountString));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        int dayCountInt = spHelper.getDailyThresholdCount();
        ++dayCountInt;
        userCountReference.child("dayCount").setValue(dayCountInt);
        Log.i(TAG,String.valueOf(dayCountInt));
        minuteCountTV.setText(String.valueOf(dayCountInt));

        userCountReference.child("dayCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dayCountString =  snapshot.getValue().toString();
                spHelper.setDailyThresholdCount(Integer.valueOf(dayCountString));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //method to start foreground service for sending notifications
    protected void startService(){
        Intent serviceIntent = new Intent(getActivity(), MyService.class);
        ContextCompat.startForegroundService(getActivity(),serviceIntent);
    }

}
