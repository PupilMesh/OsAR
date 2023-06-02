package com.cubedemo;

import com.cubedemo.Callback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImuModule extends ReactContextBaseJavaModule{
    static ReactApplicationContext context;
    MobileIMUData data;
    public ImuModule(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d("CheckingIMU", "IMUModule Const");

        this.context = reactContext;
        data = new MobileIMUData(this.context,callback);

    }

    @NonNull
    @Override
    public String getName() {
        return "Imu";
    }
    
    static public void sendImu(float[] data) {

        // String data = Base64.encodeToString(frame,Base64.DEFAULT);
        Log.d("CheckingIMU",data[0]+"");
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                // .emit("Imu",data[0]+","+ data[1]  );
                .emit("Imu",data[0]+","+ data[1] + ","+ data[2] );
                // .emit("Imu",data[0]+","+ data[1] + ","+ data[2] + ","+ data[3]  );
    }
    @ReactMethod
    public void addListener(String eventName) {

    }

    @ReactMethod
    public void removeListeners(Integer count) {

    }

    Callback callback = new Callback() {
        @Override
        public void onCallback(float[] array) {
        sendImu(array);
        }
    };
}
