package com.example.soundsensev1;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);   //same code used to open file
    }

    public void setUserLogIn(Boolean login) {
        SharedPreferences.Editor edt = sharedPreferences.edit();
        edt.putBoolean("activity_executed", login);
        edt.commit();
    }

    public Boolean isUserLoggedIn(){
        return sharedPreferences.getBoolean("activity_executed",false);
    }

    public void setRecentSensorValue(String value){
        SharedPreferences.Editor edt = sharedPreferences.edit();
        edt.putString("recent_sensor_value", value);
        edt.commit();
    }

    public String getRecentSensorValue(){
        return sharedPreferences.getString("recent_sensor_value",null);
    }

    public void setNotification(Boolean control){
        SharedPreferences.Editor edt = sharedPreferences.edit();
        edt.putBoolean("notif_control", control);
        edt.commit();
    }

    public Boolean getNotification(){
        return sharedPreferences.getBoolean("notif_control",true);
    }

    public void setHourlyThresholdCount(int value){
        SharedPreferences.Editor edt = sharedPreferences.edit();
        edt.putInt("hourly_threshold_count", value);
        edt.commit();
    }

    public int getHourlyThresholdCount(){
        return sharedPreferences.getInt("hourly_threshold_count",0);
    }

    public void setDailyThresholdCount(int value){
        SharedPreferences.Editor edt = sharedPreferences.edit();
        edt.putInt("daily_threshold_count", value);
        edt.commit();
    }

    public int getDailyThresholdCount(){
        return sharedPreferences.getInt("daily_threshold_count",0);
    }

    public void setMinuteThresholdCount(int value){
        SharedPreferences.Editor edt = sharedPreferences.edit();
        edt.putInt("minute_threshold_count", value);
        edt.commit();
    }

    public int getMinuteThresholdCount(){
        return sharedPreferences.getInt("minute_threshold_count",0);
    }

}
