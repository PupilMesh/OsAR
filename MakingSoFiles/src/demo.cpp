#include <jni.h>
#include <android/bitmap.h>
#include <iostream>
#include "aruco.hpp"

#include "log.hpp"

const char* TAG="Demo";
// com.example.testingsofiles
extern "C" JNIEXPORT jstring JNICALL Java_com_example_testingsofiles_MyClass_stringFromJNI(JNIEnv* env, jobject obj) {
    std::cout << "Hello from C++!" << std::endl;
    const char* message;
    arucoDect obc=arucoDect();
    if(obc.callingCheck(4)==5)
    message= "Message_is Same";
    else
    message = "Message is Different";
    return env->NewStringUTF(message);
}
extern "C" JNIEXPORT jobject JNICALL Java_com_example_testingsofiles_MyClass_findMarker(JNIEnv *env, jobject obj, jbyteArray data, jint width, jint height) {
    const char* message;
    jbyte* byteArray = env->GetByteArrayElements(data, nullptr);

    if (byteArray == nullptr) {
        // Handle error
        LOG_D(TAG,"byte is null");
        message = "Error";
        return env->NewStringUTF(message);
    }

    LOG_D(TAG, "%d %d %d", height, width, sizeof(byteArray));
    message = "success";
    // Convert the byte array to a cv::Mat object
    cv::Mat image(height, width, CV_8UC1, (uchar*)byteArray);
    arucoDect obc = arucoDect();
    vector<cv::Mat> images = obc.generateMarker(1, 200, aruco::DICT_6X6_250);
    LOG_D(TAG, "%d size", images.size());
    vector<jobject> bitmapObjects;
    for (const auto& image : images) {
        // Create a Java Bitmap object from the C++ Mat data
        if(!image.empty()){
             LOG_D(TAG, "image is not  null");
             
            jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
            if (bitmapClass == nullptr) {
                LOG_D(TAG, "bitmapClass is null");
                return nullptr;
            } 

            jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
            if(createBitmapMethod==nullptr){
                LOG_D(TAG, "createBitmapMethod class is null");
                return nullptr; 
            }
            LOG_D(TAG, "%d cols %d rows",image.cols,image.rows);
            // Find the Bitmap.Config class
            jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
            if (bitmapConfigClass == nullptr) {
                LOG_D(TAG, "Bitmap.Config class not found");
                return nullptr;
            }

// Find the static fields of the Bitmap.Config class
            jfieldID argb8888Field = env->GetStaticFieldID(bitmapConfigClass, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
            if (argb8888Field == nullptr) {
                LOG_D(TAG, "ARGB_8888 field not found");
                return nullptr;
            }

            // Get the value of the ARGB_8888 field
            jobject bitmapConfig = env->GetStaticObjectField(bitmapConfigClass, argb8888Field);
            if (bitmapConfig == nullptr) {
                LOG_D(TAG, "Failed to obtain ARGB_8888 field value");
                return nullptr;
            }

// Create the Java Bitmap object using the obtained Bitmap.Config value
            jobject javaBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, image.cols, image.rows, bitmapConfig);

            if (env->ExceptionCheck()) {
                // An exception occurred
              env->ExceptionDescribe(); // Print exception details (optional)
                env->ExceptionClear(); // Clear the exception
                // Handle the exception as needed
                 LOG_D(TAG, "Exception occurred while creating bitmap");
                return nullptr; // Or perform any other necessary actions
            }
            if(javaBitmap==nullptr){
                LOG_D(TAG, "javaBitmap class is null");
                return nullptr; 
            }
            // Copy the pixel data from the C++ Mat to the Java Bitmap
            void* bitmapPixels;
            AndroidBitmap_lockPixels(env, javaBitmap, &bitmapPixels);
            memcpy(bitmapPixels, image.data, image.total() * image.elemSize());
            AndroidBitmap_unlockPixels(env, javaBitmap);
            LOG_D(TAG, "image totale %d  size %d",image.total(),image.elemSize());
            jmethodID getWidthMethod = env->GetMethodID(bitmapClass, "getWidth", "()I");
            if (getWidthMethod == nullptr) {
                LOG_D(TAG, "getWidthMethod is null");
                return nullptr;
            }

            // Get the getHeight() method of the Bitmap class
            jmethodID getHeightMethod = env->GetMethodID(bitmapClass, "getHeight", "()I");
            if (getHeightMethod == nullptr) {
                LOG_D(TAG, "getHeightMethod is null");
                return nullptr;
            }
            if(javaBitmap != nullptr){
                jint width = env->CallIntMethod(javaBitmap, getWidthMethod);
                jint height = env->CallIntMethod(javaBitmap, getHeightMethod);

                // Print or use the width and height as needed
                LOG_D(TAG, "Bitmap size: %d x %d", width, height);
                LOG_D(TAG, "JavaBitmap is not null");
                bitmapObjects.push_back(javaBitmap);
            }
            else{
              LOG_D(TAG, "JavaBitmap is null");  
            }
            // Add the Java Bitmap to the vector
            
        }
        else{
            LOG_D(TAG, "image is  null");
        }
    }

    LOG_D(TAG, "%d size of bitmap", bitmapObjects.size());
    // Create a Java ArrayList to store the Bitmap objects
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject bitmapArrayList = env->NewObject(arrayListClass, arrayListConstructor);

    // Get the add method of the ArrayList
    jmethodID arrayListAddMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    // Add each Bitmap object to the ArrayList
    for (const auto& bitmapObject : bitmapObjects) {
        env->CallBooleanMethod(bitmapArrayList, arrayListAddMethod, bitmapObject);
    }

    return bitmapArrayList;
}

