package com.cubedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.cubedemo.IMU.DeviceIMU;
import com.cubedemo.IMU.IMUCallback;
import com.cubedemo.MobileIMUData;



public class MobileIMUData implements SensorEventListener, IMUCallback {
    android.hardware.SensorManager mSensorManager;
    long startTime=0;
    Context context ;
    private Callback callback;
    DeviceIMU DeviceImuInit;
    // ArrayList<String> velocityValues;
    String TAG="MobileIMU";
    Sensor accelerometer;
    public MobileIMUData(Context context,Callback callback) {
        this.callback = callback;
        this.context = context;
        // DeviceImuInit=new DeviceIMU(context,this);
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
//Uncomment this callback.onCallback to read mobile IMU for testing
//        callback.onCallback(sensorEvent.values);

    //   velocityValues.add(data);
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

    @Override
    public void onCallback(float[] floats, int i, int i1) {
//        variable i gives the type of value, it can be Rotation or Gyroscope, when its 11, its rotation
//        Quarterian
        Log.d(TAG,floats[0]+" "+floats[1]+" "+floats[2]+" "+floats[3]);
        if(i==11) {
            callback.onCallback(floats);
        }
    }
}