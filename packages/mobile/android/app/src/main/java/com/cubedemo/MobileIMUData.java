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
        // velocityValues= new ArrayList<>();
        // velocityValues.add("a_x,a_y,a_z,TimeMillisecond \n" );
        mSensorManager = (android.hardware.SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Log.i(TAG,"Constructur called");
        mSensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      Log.i(TAG,""+sensorEvent.values+" "+sensorEvent.sensor.getType());
        startTime = System.currentTimeMillis();
        // String data = "";
        // for (int x = 0; x < sensorEvent.values.length; x++) {
        //     data += sensorEvent.values[x] + ",";
        // }
        // data += startTime + "\n";
        callback.onCallback(sensorEvent.values);

    //   velocityValues.add(data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    public  void wirteCSV(ArrayList<String> name,String fileName){
        try {

            File file = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath() +fileName);
                Log.i(TAG," path"+ file.getAbsolutePath());
            }
            else {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath() +fileName);
            }
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (int x=0;x<name.size();x++) {
                bw.write(name.get(x));
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public boolean close(){
        // wirteCSV(velocityValues,"wakingWithMovile.csv");
        try {
            mSensorManager.unregisterListener(this);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}