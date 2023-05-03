package com.cubedemo.Camera;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
public class R100PermissionManager {
    final boolean DEBUG = false;
    public static int TYPE_CAMERA_PERMISSION=0;
    public static int TYPE_SENSOR_PERMISSION=1;
    public static int TYPE_DISPLAY_PERMISSION=2;

    String ACTION_CAMERA_PERMISSION="com.R100.USB_CAMERA_PERMISSION";
    String ACTION_SENSOR_PERMISSION="com.R100.USB_SENSOR_PERMISSION";
    String ACTION_DISPLAY_PERMISSION="com.R100.USB_DISPLAY_PERMISSION";
    String TAG = "Permission";
    Context context;
    Activity activity;
    UsbManager usbManager;
    UsbDevice usbDevice;
    R100PermissionListener listener;
    IntentFilter intentFilter;
  public R100PermissionManager(Activity activity, R100PermissionListener listener){
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.listener = listener;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        intentFilter= new IntentFilter();
        intentFilter.addAction(ACTION_CAMERA_PERMISSION);
        intentFilter.addAction(ACTION_SENSOR_PERMISSION);
        intentFilter.addAction(ACTION_DISPLAY_PERMISSION);
        context.registerReceiver(receiver, intentFilter);
    }
    public   void requestCameraPermission(int vendorid,int productid) {

        sendRequest(ACTION_CAMERA_PERMISSION,vendorid,productid,TYPE_CAMERA_PERMISSION);
    }
    public void requestSensorPermission(){
        sendRequest(ACTION_SENSOR_PERMISSION,0x04B8,0x0C0F,TYPE_SENSOR_PERMISSION);
    }
    public void requestDisplayPermission(){
        sendRequest(ACTION_DISPLAY_PERMISSION,0x04B8,0x0C0F,TYPE_DISPLAY_PERMISSION);
    }
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("OnReceived Method called");
            if (intent.getAction().equals(ACTION_CAMERA_PERMISSION)) {
                log("Receive Intent");
                sendResponse(intent,TYPE_CAMERA_PERMISSION);
            }
            if (intent.getAction().equals(ACTION_SENSOR_PERMISSION)) {
                log("Receive Intent");
                sendResponse(intent,TYPE_SENSOR_PERMISSION);
            }
            if (intent.getAction().equals(ACTION_DISPLAY_PERMISSION)) {
                log("Receive Intent");
                sendResponse(intent,TYPE_DISPLAY_PERMISSION);
            }
        }
    };
    void sendRequest(String name,int vendorId,int productId,int type){
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Log.i(TAG,"devic List"+deviceList.toString());
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        usbDevice = null;
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if(device.getProductId() == productId && device.getVendorId() == vendorId) {
                usbDevice = device; // This is H ID/CDC device
                if (usbDevice == null) {
                    return;
                }
            }
        }
        if (usbDevice != null) {
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context,type , new Intent(name), PendingIntent.FLAG_MUTABLE);
            usbManager.requestPermission(usbDevice, mPermissionIntent);
        }
    }
    void sendResponse(Intent intent,int code){
        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            listener.permissionGranted(true,code);
            log("Permission Granted");

        } else {
            listener.permissionGranted(false,code);
            log("Permission Not Granted");
        }
    }
    void log(String message){
        if(DEBUG)
            Log.i(TAG,message);
    }
}
