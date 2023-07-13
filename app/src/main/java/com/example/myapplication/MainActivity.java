package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MobileIMUData mIMU;
    TextView tvA;
    TextView tvV;
    TextView tvP;
    Timer textUpdateTimer = new Timer();
    int delay = 0;
    int period = 100;
    boolean isRunning = false;

    GraphView gv_AMAG;
    LineGraphSeries<DataPoint> magAcc;
    LineGraphSeries<DataPoint> magAccSmooth;
    // call the text update function every 100ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvA = findViewById(R.id.IMUVALA);
        tvV = findViewById(R.id.IMUVALV);
        tvP = findViewById(R.id.IMUVALP);

        gv_AMAG = findViewById(R.id.A_MAG);

        Viewport vp = gv_AMAG.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setMinX(0);
        vp.setMaxX(1000);

        vp.setYAxisBoundsManual(true);
        vp.setMinY(-3);
        vp.setMaxY(3);

        gv_AMAG.setTitle("A_mag");

        magAcc = new LineGraphSeries<>();
        magAccSmooth = new LineGraphSeries<>();
        magAcc.setColor(Color.RED);
        magAccSmooth.setColor(Color.GREEN);

        gv_AMAG.addSeries(magAcc);
        gv_AMAG.addSeries(magAccSmooth);

        // mIMU = new MobileIMUData(this);
    }

    public void buttonClickFunc(View view) {
        mIMU.close();
        Log.i("DESTROYING", "Window is destroyed");
        magAcc = new LineGraphSeries<>();
        isRunning = false;
    }

    public void buttonClickFuncStart(View view) {
        Log.i("CREATING", "Window is created");
        isRunning = true;
        try {
            textUpdateTimer.scheduleAtFixedRate(new TimerTask()
            {
                public void run()
                {
                    updateSensorValText();
                }
            }, delay, period);
            mIMU = new MobileIMUData(this);
        } catch (Exception e) {
            Log.i("ERR_START", String.valueOf(e));
        }
    }

    @SuppressLint("DefaultLocale")
        public void updateSensorValText() {
        if (!isRunning) { return; }
        try {
            String stA = String.format(
                    "Xa: %f\nYa: %f\nZa: %f\nDt: %f",
                    mIMU.accValues.get(0) * 100,
                    mIMU.accValues.get(1) * 100,
                    mIMU.accValues.get(2) * 100,
                    mIMU.dT
            );
            String stV = String.format(
                    "Xv: %f\nYv: %f\nZv: %f",
                    mIMU.velValues.get(0) * 100,
                    mIMU.velValues.get(1) * 100,
                    mIMU.velValues.get(2) * 100
            );
            String stP = String.format(
                    "Xp: %f\nYp: %f\nZp: %f",
                    mIMU.posValues.get(0) * 100,
                    mIMU.posValues.get(1) * 100,
                    mIMU.posValues.get(2) * 100
            );

            Log.i("SENS_VAL", "\n" + stA);
            tvA.setText(stA);
            tvV.setText(stV);
            tvP.setText(stP);
            updGraph();

        } catch (Exception e) {
            Log.i("ERR", String.valueOf(e));
            // set the frequency to 1000ms if we haven't started
        }

    }

    public void updGraph() {
        magAcc.appendData(new DataPoint(mIMU.timeAlive, mIMU.magAcc.get(mIMU.magAcc.size() - 1)), true, 400);
        magAccSmooth.appendData(new DataPoint(mIMU.timeAlive, mIMU.smoothMagAcc), true, 400);
    }



//    Callback cb = new Callback() {
//        @Override
//        public int hashCode() {
//            return super.hashCode();
//        }
//    };
}