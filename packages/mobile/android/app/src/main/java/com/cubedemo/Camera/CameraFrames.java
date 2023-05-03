package com.cubedemo.Camera;

import android.graphics.Bitmap;

public interface CameraFrames {
    void onCameraFrame(Bitmap bitmap, long timestamp);
    void onCameraFrame(byte[] bytes , String format,int height, int width);

}
