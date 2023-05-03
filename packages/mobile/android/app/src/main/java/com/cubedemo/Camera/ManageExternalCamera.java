package com.cubedemo.Camera;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ManageExternalCamera implements CameraDialog.CameraDialogParent {

    Context context;
    Activity activity;
    Surface mPreviewSurface;
    IFrameCallback mIFrameCallback;

    private final Object mSync = new Object();
    USBMonitor mUsbMonitor;
    private UVCCamera mUVCCamera;
    String TAG = "ManageExternalCamera";

    CameraFrames frameListner;

    private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }
    private final USBMonitor.OnDeviceConnectListener connectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            Log.i(TAG, "Device attached");
        }

        @Override
        public void onDettach(UsbDevice device) {
            Log.i(TAG, "Device Dettached");
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            Log.i(TAG, "On Connect Method called");
            releaseCamera();
            new Thread(() -> {
                        Log.i(TAG, "On Connect Run method" + Thread.currentThread().getName() + " " + Thread.currentThread().getId());
                        final UVCCamera camera = new UVCCamera();
                        camera.open(ctrlBlock);
                        camera.setStatusCallback((statusClass, event, selector, statusAttribute, data) -> Log.i(TAG, data.toString()));

                        try {
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final IllegalArgumentException e) {
                            // fallback to YUV mode
                            try {
                                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                            } catch (final IllegalArgumentException e1) {
                                camera.destroy();
                                return;
                            }
                        }
                            SurfaceTexture  surfaceTexture = new SurfaceTexture(2);
                            mPreviewSurface = new Surface(surfaceTexture);
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.setAutoFocus(true);
                            camera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565 /*UVCCamera.PIXEL_FORMAT_RGB565/*UVCCamera.PIXEL_FORMAT_NV21*/);
                            camera.startPreview();
                        synchronized (mSync) {
                            mUVCCamera = camera;
                        }
                    }).start();
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            Log.i(TAG, "On Disconnect Method Called");
        }

        @Override
        public void onCancel(UsbDevice device) {
            Log.i(TAG, "On Cancel Method called");

        }
    };
    public ManageExternalCamera(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        try {
            context.registerReceiver(receiver, new IntentFilter("com.serenegiant.USB_PERMISSION"));
        }
        catch (Exception e){
            Log.i(TAG,"Exception arrise");
            e.printStackTrace();
        }
        mUsbMonitor = new USBMonitor(context, connectListener);
        final Bitmap bitmap = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
        mIFrameCallback = frame -> {
            try {
                frame.clear();
                synchronized (bitmap) {
                    bitmap.copyPixelsFromBuffer(frame);
                }
                if(frameListner!= null){
                    frameListner.onCameraFrame(bitmap, 000);
                   // frameListner.onCameraFrame(convetToArray(bitmap), String.valueOf(Bitmap.Config.RGB_565),UVCCamera.DEFAULT_PREVIEW_HEIGHT,UVCCamera.DEFAULT_PREVIEW_WIDTH);
                }
            } catch (Exception e) {
                Log.i(TAG, "Exeption Arrises" + e);
            }
        };
        Log.i(TAG, "Constructor Work Fine");
    }
    public byte[] convetToArray(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        ByteBuffer buffer = ByteBuffer.allocate(width * height*2);
        bmp.copyPixelsToBuffer(buffer);

        byte[] data = buffer.array();
        return  data;
    }
    public void setFrameListner(CameraFrames listner){
        this.frameListner = listner;
    }
    public Object getSync() {
        return mSync;
    }

    public UVCCamera getUVCCamera() {
        return mUVCCamera;
    }

    public void register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mUsbMonitor.register();
        }
    }

    void cameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 0);
        } else {
            Toast.makeText(context, "All ready Camera Permission", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCamera() {
        Log.i(TAG, "I will open Camera");

        if (mUVCCamera == null) {
            // i change activity of context
            setCamera();
            Log.i(TAG, "Camera not null");
        } else {
            Log.i(TAG,"Called From openCamera");
            releaseCamera();
        }
    }
    public void setCamera() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(activity, com.serenegiant.uvccamera.R.xml.device_filter);
        List<UsbDevice> list =mUsbMonitor.getDeviceList(filter.get(0));
        Log.i(TAG,list.toString());
        if(list.size()>0) {
            UsbDevice device = list.get(0);
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.serenegiant.USB_PERMISSION"), PendingIntent.FLAG_MUTABLE);
            manager.requestPermission(device, mPermissionIntent);
        }

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "OnReceived Method called");
            if (intent.getAction().equals("com.serenegiant.USB_PERMISSION")) {
                Log.i(TAG,"Receive Intent");
                UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // get permission, call onConnect
                            mUsbMonitor.processConnect(device);
                            Log.i(TAG,"Permission Granted");
                        }
                    }else {
                        Log.i(TAG,"I Don't get permission");
                    }
            }
        }
    };
    public void setUVCMonitor(USBMonitor usbMonitor) {
        this.mUsbMonitor = usbMonitor;
    }
    public synchronized void releaseCamera() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                try {
                    mUVCCamera.setStatusCallback(null);
                    mUVCCamera.setButtonCallback(null);
                    mUVCCamera.close();
                    mUVCCamera.destroy();
                } catch (final Exception e) {
                    Log.i(TAG, " Exception Arises during camera closing");
                }
                mUVCCamera = null;
            }
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
        }
        Log.i(TAG, "Release Camera done");
    }
    @Override
    public USBMonitor getUSBMonitor() {
        Log.i(TAG, "getUSbMonitor Called");
        return mUsbMonitor;
    }
    public static Bitmap RotateBitmap(Bitmap source)
    {
        Matrix matrix = new Matrix();

        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    @Override
    public void onDialogResult(boolean canceled) {
        Log.i(TAG, "onDialogResultMethodCalled");
        if (canceled) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
           });
        }
    }

}
