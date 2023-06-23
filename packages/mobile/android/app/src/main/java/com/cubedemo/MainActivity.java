package com.cubedemo;


import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.os.Bundle;
import android.util.Base64;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cubedemo.Camera.CameraFrames;
import com.cubedemo.Camera.ManageExternalCamera;
import com.cubedemo.Camera.R100PermissionListener;
import com.cubedemo.Camera.R100PermissionManager;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class MainActivity extends ReactActivity {

  String TAG = "CheckCamera";
  boolean hasPermission = false;
  R100PermissionManager permissionManager;
  boolean hasCameraPermission = false;
  ManageExternalCamera externalCamera;
  String ARUCO_TYPE = "DICT_4X4_50";
  private Python py;
  private PyObject markerDetectionFunction;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    
    super.onCreate(savedInstanceState);
    Log.i(TAG, "On create called");
    if (!Python.isStarted()) {
      Python.start(new AndroidPlatform(this));
    }
  py=Python.getInstance();
  markerDetectionFunction=py.getModule("aruco").get("detect_marker");
  }

  @Override
  protected void onStart() {
    super.onStart();
    permissionManager = new R100PermissionManager(this, listener);
    externalCamera = new ManageExternalCamera(this, this);
    externalCamera.setFrameListner(frameListner);
  }

  CameraFrames frameListner = new CameraFrames() {
    private static final long FRAME_INTERVAL_MS = 1000 / 24; // 24 fps
    private long lastFrameTime = System.currentTimeMillis();

    @Override
    public void onCameraFrame(Bitmap bitmap, long timestamp) {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
      byte[] bytes = outputStream.toByteArray();

      // String data = Base64.encodeToString(bytes,Base64.DEFAULT);
      // Log.i("PATH", getFilesDir().getPath());

      PyObject result = markerDetectionFunction.call(bytes,ARUCO_TYPE);
      String jsonString = result.toString();

      // Log.i("JSON",jsonString);
      // bytes = result.toJava(byte[].class);
      // Bitmap resultBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

      // // Compress the Bitmap
      // ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
      // resultBitmap.compress(Bitmap.CompressFormat.JPEG, 50, compressedOutputStream);
      // bytes = compressedOutputStream.toByteArray();

      // To Test the frame rate
      // int staticValue = 42; 
      // int byteArraySize = 1; 

      // byte[] bytes = new byte[byteArraySize];
      // Arrays.fill(bytes, (byte) staticValue);

      CameraFrameModule.sendCameraFrame(jsonString);
      // CameraFrameModule.sendCameraFrame(data);
    }

    @Override
    public void onCameraFrame(byte[] bytes, String format, int height, int width) {
      Log.i(TAG, "Byte arrary coming");
    }
  };
  R100PermissionListener listener = new R100PermissionListener() {
    @Override
    public void permissionGranted(Boolean bool, int code) {
      Log.i(TAG, "Permission Granted");
      hasCameraPermission = true;
      externalCamera.openCamera();
    }
  };

  @Override
  protected void onResume() {
    super.onResume();
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 20);
    } else if (!hasPermission) {
      Log.i(TAG, "Sent for permission");
      permissionManager.requestCameraPermission(1008, 2393);
    }
  }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "CubeDemo";
  }

  /**
   * Returns the instance of the {@link ReactActivityDelegate}. There the RootView is created and
   * you can specify the renderer you wish to use - the new renderer (Fabric) or the old renderer
   * (Paper).
   */
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new MainActivityDelegate(this, getMainComponentName());
  }

  public static class MainActivityDelegate extends ReactActivityDelegate {
    public MainActivityDelegate(ReactActivity activity, String mainComponentName) {
      super(activity, mainComponentName);
    }

    @Override
    protected ReactRootView createRootView() {
      ReactRootView reactRootView = new ReactRootView(getContext());
      // If you opted-in for the New Architecture, we enable the Fabric Renderer.
      reactRootView.setIsFabric(BuildConfig.IS_NEW_ARCHITECTURE_ENABLED);
      return reactRootView;
    }

    @Override
    protected boolean isConcurrentRootEnabled() {
      // If you opted-in for the New Architecture, we enable Concurrent Root (i.e. React 18).
      // More on this on https://reactjs.org/blog/2022/03/29/react-v18.html
      return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
    }
  }
}
