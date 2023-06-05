package com.cubedemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
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

    public MobileIMUData(Context context, Callback callback) {
        this.callback = callback;
        this.context = context;
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Log.i(TAG, "Constructor called");
        mSensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.i(TAG, "" + sensorEvent.values + " " + sensorEvent.sensor.getType());
        startTime = System.currentTimeMillis();

        // Calculate the rotation matrix
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

        // Get the azimuth, pitch, and roll
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        // Convert to degrees
        // for (int i = 0; i < orientationValues.length; i++) {
        //     orientationValues[i] = (float) Math.toDegrees(orientationValues[i]);
        // }

        // At this point, orientationValues[0] is the azimuth, orientationValues[1] is
        // the pitch,
        // and orientationValues[2] is the roll.

        callback.onCallback(orientationValues);
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
