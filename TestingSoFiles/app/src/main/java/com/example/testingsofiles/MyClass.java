package com.example.testingsofiles;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.util.List;

public class MyClass {
    static {
        System.loadLibrary("mylibrary");
    }
    public native String stringFromJNI();
    public native List<Bitmap> findMarker(byte[] buffer, int width, int height);

}