package com.example.testingsofiles;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MyClass myClass;
    ImageView imgView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myClass=new MyClass();
        Toast.makeText(this, myClass.stringFromJNI(), Toast.LENGTH_SHORT).show();
        imgView= findViewById(R.id.imageView);
        Bitmap bitmap = read_PNG();

// Get the width and height of the bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

// Create a byte buffer to hold the pixel data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       // bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] buffer=outputStream.toByteArray();
        List<Bitmap> bitmaps= myClass.findMarker(buffer,width,height);
        Log.i("demo","Length of image"+bitmaps.size());
        imgView.setImageBitmap(bitmaps.get(0));
    }
    Bitmap read_PNG(){
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.raw.marker_0);
        return  bitmap;
    }
}