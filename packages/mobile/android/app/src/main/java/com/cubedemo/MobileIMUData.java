package com.cubedemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import com.cubedemo.MobileIMUData;
import com.cubedemo.Callback;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class MobileIMUData implements SensorEventListener {
    android.hardware.SensorManager mSensorManager;
    long startTime=0;
    Context context ;
    private Callback callback;
    // ArrayList<String> velocityValues;
    String TAG="MobileIMU";
    Sensor accelerometer;
    public MobileIMUData(Context context,Callback callback) {
        this.callback = callback;
        this.context = context;
        mSensorManager = (android.hardware.SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Log.i(TAG,"Constructur called");
        mSensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      Log.i(TAG,""+sensorEvent.values+" "+sensorEvent.sensor.getType());
        startTime = System.currentTimeMillis();
        callback.onCallback(sensorEvent.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    public boolean close(){
        try {
            mSensorManager.unregisterListener(this);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
