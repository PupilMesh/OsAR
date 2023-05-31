package com.cubedemo;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.*;
import java.util.Base64;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MarkerDetector {
    // Initialize the AKAZE feature detector
    private final AKAZE detector = AKAZE.create();
    private final DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    // HashMap to hold smaller images
    private final HashMap<String, MatOfKeyPoint> smallerImagesKeyPoints = new HashMap<>();
    private final HashMap<String, Mat> smallerImagesDescriptors = new HashMap<>();

    public MarkerDetector(String[] urls) throws Exception {
        for (String url : urls) {
            // Download the smaller image
            Bitmap bmp = downloadImage(url);
            Mat img = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
            Utils.bitmapToMat(bmp, img);
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2GRAY);

            // Compute key points and descriptors for the smaller image
            MatOfKeyPoint keyPoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();
            detector.detectAndCompute(img, new Mat(), keyPoints, descriptors);

            // Store the key points and descriptors in the HashMap
            smallerImagesKeyPoints.put(url, keyPoints);
            smallerImagesDescriptors.put(url, descriptors);
        }
    }

    private Bitmap downloadImage(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        return BitmapFactory.decodeStream(input);
    }

    public String detectMarker(Mat frame) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray detectedMarkers = new JSONArray();

        // Convert frame to grayscale
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGBA2GRAY);

        // // Compute key points and descriptors for the frame
        // MatOfKeyPoint frameKeyPoints = new MatOfKeyPoint();
        // Mat frameDescriptors = new Mat();
        // detector.detectAndCompute(grayFrame, new Mat(), frameKeyPoints, frameDescriptors);

        // // Match the frame descriptors with the smaller images descriptors
        // for (Map.Entry<String, Mat> entry : smallerImagesDescriptors.entrySet()) {
        //     String url = entry.getKey();
        //     Mat smallerImageDescriptors = entry.getValue();

        //     MatOfDMatch matches = new MatOfDMatch();
        //     matcher.match(frameDescriptors, smallerImageDescriptors, matches);

        //     // Filter matches by distance
        //     List<DMatch> matchesList = matches.toList();
        //     List<DMatch> goodMatchesList = new ArrayList<>();
        //     for(DMatch match : matchesList) {
        //         if(match.distance < 0.7 * match.distance) {
        //             goodMatchesList.add(match);
        //         }
        //     }

        //     // If enough good matches, add to detected markers
        //     if(goodMatchesList.size() > 5) {
        //         detectedMarkers.put(url);
        //     }
        // }

        // Convert the frame to a JPEG image and then to Base64 string
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        String imageBase64 = Base64.getEncoder().encodeToString(byteArray);

        // Prepare the result as a JSON string
        result.put("detected_markers", "detectedMarkers");
        result.put("image", imageBase64);
        return result.toString();
    }
}
