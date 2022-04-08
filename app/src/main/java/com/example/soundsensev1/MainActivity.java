package com.example.soundsensev1;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener{

    private Calendar calendar;
    private SharedPreferencesHelper spHelper;

    private static final int POS_CLOSE = 0;
    private static final int POS_BUTTON = 1;
    private static final int POS_MY_PROFILE = 2;
    private static final int POS_DATA = 3;
    private static final int POS_SETTINGS = 4;
    private static final int POS_LOGOUT = 6;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    private static DatabaseReference userCountReference;
    private static DatabaseReference sensorControlReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userCountReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("thresholdCounts");

        sensorControlReference = FirebaseDatabase.getInstance().getReference().child("Device").child("ON&OFF");


        spHelper = new SharedPreferencesHelper(this);

        //toolbar settings
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SoundSense");

        resetThresholdCounts();

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(180)
                .withRootViewScale(0.75f)
                .withRootViewElevation(25)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menu)
                .inject();
        
        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_CLOSE),
                createItemFor(POS_BUTTON).setChecked(true),
                createItemFor(POS_MY_PROFILE),
                createItemFor(POS_DATA),
                createItemFor(POS_SETTINGS),
                new SpaceItem(260),
                createItemFor(POS_LOGOUT)
        ));

        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.drawer_list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_BUTTON);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_menu_24);
        toolbar.setOverflowIcon(drawable);

    }

    private DrawerItem createItemFor(int position){
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.white))
                .withTextTint(color(R.color.white))
                .withSelectedIconTint(color(R.color.white))
                .withSelectedTextTint(color(R.color.white));
    }

    @ColorInt
    private int color(@ColorRes int res){
        return ContextCompat.getColor(this, res);
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.id_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.id_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++){
            int id = ta.getResourceId(i, 0);
            if (id != 0){
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }

        ta.recycle();
        return icons;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onItemSelected(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (position == POS_BUTTON){
            MainFragment mainFragment = new MainFragment();
            transaction.replace(R.id.container, mainFragment);
        }

        else if (position == POS_MY_PROFILE){
            ProfileFragment profileFragment = new ProfileFragment();
            transaction.replace(R.id.container, profileFragment);
        }

        else if (position == POS_DATA){
            DataFragment dataFragment = new DataFragment();
            transaction.replace(R.id.container, dataFragment);
        }

        else if (position == POS_SETTINGS){
            SettingFragment settingFragment = new SettingFragment();
            transaction.replace(R.id.container, settingFragment);
        }

        else if (position == POS_LOGOUT){
            FirebaseAuth.getInstance().signOut();
            //edit shared preferences to set activity_executed to false
            spHelper.setUserLogIn(false);
            sensorControlReference.setValue(0);
            goToLoginActivity();
        }

        slidingRootNav.closeMenu();
        transaction.addToBackStack(null);
        transaction.commit();
    }

    protected void goToLoginActivity(){
        Intent intent = new Intent (this,LoginActivity.class);
        startActivity(intent);
    }

    public void resetThresholdCounts(){
        calendar = Calendar.getInstance();
        int checkSecond = calendar.get(Calendar.SECOND);
        int checkMinute = calendar.get(Calendar.MINUTE);
        int checkHour = calendar.get(Calendar.HOUR_OF_DAY);
        Intent intent = new Intent();
        if(checkSecond == 0){
            userCountReference.child("minuteCount").setValue(0);
            spHelper.setMinuteThresholdCount(0);
            intent.putExtra("minuteTV","0");
        }

        if(checkMinute == 0 && checkSecond == 0){
            userCountReference.child("hourCount").setValue(0);
            spHelper.setHourlyThresholdCount(0);
            intent.putExtra("hourTV","0");
        }

        if(checkHour == 0 && checkMinute == 0 && checkSecond == 0){
            userCountReference.child("dayCount").setValue(0);
            spHelper.setDailyThresholdCount(0);
            intent.putExtra("dayTV","0");
        }

        refreshThresholdCounts(1000);
    }

    private void refreshThresholdCounts(int milliseconds) {

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                resetThresholdCounts();
            }
        };

        handler.postDelayed(runnable, milliseconds);
    }

}