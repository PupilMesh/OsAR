package com.cubedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraFrameModule extends ReactContextBaseJavaModule {
   static ReactApplicationContext context;
    public CameraFrameModule(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "CameraFrame";
    }
    // @ReactMethod
    // public void sayHellow(String name, Callback callback) {
    //     try {
    //         String message = "hellow" + name;
    //         URL url = new URL(
    //                 "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/800px-Image_created_with_a_mobile_phone.png");
    //         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    //         connection.setDoInput(true);
    //         connection.connect();
    //         InputStream input = connection.getInputStream();
    //         Bitmap bitmap = BitmapFactory.decodeStream(input);
    //         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //         bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    //         byte[] bytes = outputStream.toByteArray();
    //         //sendCameraFrame(bytes);
    //         callback.invoke(null, "send data through Emitor");
    //     } catch (Exception e) {
    //         callback.invoke(e, null);
    //     }
    // }
    

  static  public void sendCameraFrame(String frame) {
        // String data = Base64.encodeToString(frame,Base64.DEFAULT);
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("cameraFrame", frame);
    }
    @ReactMethod
    public void addListener(String eventName) {

    }

    @ReactMethod
    public void removeListeners(Integer count) {

    }
}
