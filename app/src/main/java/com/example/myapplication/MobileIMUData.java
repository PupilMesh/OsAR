package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MobileIMUData implements SensorEventListener {
    android.hardware.SensorManager mSensorManager;
    double curTime = System.currentTimeMillis();
    double oldTime = curTime;
    double startTime = curTime;

    double timeAlive = curTime - startTime;

    public double dT = curTime - oldTime;
    Context context;
    private Callback callback;
    ArrayList<Double> accX = new ArrayList<>();
    ArrayList<Double> accY = new ArrayList<>();
    ArrayList<Double> accZ = new ArrayList<>();
    public ArrayList<Double> accValues = new ArrayList<>();
    public ArrayList<Double> velValues = new ArrayList<>();
    public ArrayList<Double> posValues = new ArrayList<>();

    public ArrayList<String> dataLines = new ArrayList<>();
    public double smoothMagAcc = 0.0d;
    public double term1, term2, term3;
    double minFilterValue = 9999;
    public int stationaryDuration = 0;

    ArrayList<Double> magAcc = new ArrayList<>();

    String TAG = "MobileIMU";
    Sensor accelerometer;

    public MobileIMUData(Activity activity) {
        this.callback = callback;

        accValues.add(0.0d);
        accValues.add(0.0d);
        accValues.add(0.0d);

        velValues.add(0.0d);
        velValues.add(0.0d);
        velValues.add(0.0d);

        posValues.add(0.0d);
        posValues.add(0.0d);
        posValues.add(0.0d);

        magAcc.add(0.0d);
        magAcc.add(0.0d);
        magAcc.add(0.0d);

        accX.add(0.0d);
        accX.add(0.0d);
        accX.add(0.0d);

        accY.add(0.0d);
        accY.add(0.0d);
        accY.add(0.0d);

        accZ.add(0.0d);
        accZ.add(0.0d);
        accZ.add(0.0d);

        context = activity.getApplicationContext();
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG, "Constructur called");
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.i(TAG, "" + sensorEvent.values[0] + " " + sensorEvent.sensor.getType());
        curTime = System.currentTimeMillis();
        timeAlive = curTime - startTime;

        accX.add(roundToPrecision(sensorEvent.values[0], 3));
        accY.add(roundToPrecision(sensorEvent.values[1], 3));
        accZ.add(roundToPrecision(sensorEvent.values[2], 3));

        accValues.set(0, rollingAverage(accX, 20));
        accValues.set(1, rollingAverage(accY, 20));
        accValues.set(2, rollingAverage(accZ, 20));

//        accUnitVec = getUnitVector(accValues);

        magAcc.add(magnitudeOf(accValues));
        smoothMagAcc = rollingAverage(magAcc, 20);
        term1 = magAcc.get(magAcc.size() - 1);
        term2 = sensorEvent.timestamp / 1e9f;
        term3 = magAcc.get(magAcc.size() - 3);

        minFilterValue = 0.03; //  filter -> set it as you like.
        //TODO: take the min filter value dynamically by averaging 20-30 samples at the start

        dT = (curTime - oldTime) / 1000.0;

        if (term1 < minFilterValue) { //
            stationaryDuration++;
            accValues.set(0, 0.0d);
            accValues.set(1, 0.0d);
            accValues.set(2, 0.0d);
            if (dT * stationaryDuration >= 0.100) { // 0.100s
                velValues.set(0, 0.0d);
                velValues.set(1, 0.0d);
                velValues.set(2, 0.0d);
                stationaryDuration = 0;
            }
        } else {
            stationaryDuration = 0;
        }

//        velValues = integrate(velValues, accValues, dT);
//        posValues = integrate(posValues, velValues, dT);

        integrateTwice();

        oldTime = curTime;
    }

    public double rollingAverage(ArrayList<Double> magAcc, int window) {
        double sum = 0.0d;
        window = Math.min(window, magAcc.size() - 1);
        for (int i = 1; i < window; i++) {
            sum += magAcc.get(magAcc.size() - i);
        }
        return sum / window;
    }
    public double magnitudeOf(ArrayList<Double> pVec) {
        return Math.sqrt(Math.pow(pVec.get(0), 2) + Math.pow(pVec.get(1), 2) + Math.pow(pVec.get(2), 2));
    }

    public ArrayList<Double> getUnitVector(ArrayList<Double> pVec) {
        double magVec = magnitudeOf(pVec);
        pVec.set(0, pVec.get(0) / magVec);
        pVec.set(1, pVec.get(1) / magVec);
        pVec.set(2, pVec.get(2) / magVec);
        return pVec;
    }

    public void integrateTwice() {
        velValues.set(0, velValues.get(0) + (accValues.get(0) * dT));
        posValues.set(0, posValues.get(0) + (velValues.get(0) * dT) + (0.5d * accValues.get(0) * dT * dT));

        velValues.set(1, velValues.get(1) + (accValues.get(1) * dT));
        posValues.set(1, posValues.get(1) + (velValues.get(1) * dT) + (0.5d * accValues.get(1) * dT * dT));

        velValues.set(2, velValues.get(2) + (accValues.get(2) * dT));
        posValues.set(2, posValues.get(2) + (velValues.get(2) * dT) + (0.5d * accValues.get(2) * dT * dT));
    }
    public ArrayList<Double> integrate(ArrayList<Double> pVal, ArrayList<Double> iVal, double dT) {
        pVal.set(0, (pVal.get(0) + (iVal.get(0) * dT)));
        pVal.set(1, (pVal.get(1) + (iVal.get(1) * dT)));
        pVal.set(2, (pVal.get(2) + (iVal.get(2) * dT)));
        return pVal;
    }

    public ArrayList<Double> differentiate(ArrayList<Double> oldV, ArrayList<Double> newV, double dT) {
        ArrayList<Double> t = new ArrayList<>();
        t.add((newV.get(0) - oldV.get(0)) / 2);
        t.add((newV.get(1) - oldV.get(1)) / 2);
        t.add((newV.get(2) - oldV.get(2)) / 2);
        return t;
    }

    public double roundToPrecision(double num, int precision) {
        return (double) Math.round(num * Math.pow(10d, precision)) / Math.pow(10d, precision);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void writeCSV(ArrayList<String> name, String fileName) {
        try {

            File file = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath() + fileName);
                Log.i(TAG, " path" + file.getAbsolutePath());
            } else {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath() + fileName);
            }
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (int x = 0; x < name.size(); x++) {
                bw.write(name.get(x));
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean close() {
        // writeCSV(velocityValues, "wakingWithMobile.csv");
        try {
            mSensorManager.unregisterListener(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
