#include <iostream>
#include <opencv2/aruco.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgcodecs.hpp>
#include "log.hpp"
using namespace cv;
using namespace std;

class arucoDect
{
private:
    /* data */
public:
    Mat generateGrid(vector<Mat> images,int rows,int cols,int margin,int padding);
    vector<Mat> generateMarker(int numMarkers = 20,int markerSize = 180,aruco::PREDEFINED_DICTIONARY_NAME name=aruco::DICT_6X6_250);
    void detectMarker(Mat);
    int callingCheck(int);
};


