#include <opencv2/opencv.hpp>
#include <iostream>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <queue>

using namespace cv;
using namespace std;

// camMtx
// [[561.01030475   0.         292.32242872]
//  [  0.         564.29763955 272.53754954]
//  [  0.           0.           1.        ]]

// distCoeff
// [[-0.23613754  0.2154634   0.01353568 -0.01290092  0.02468145]]

int frameTime = 1;
cv::Mat image, imageCopy, imageOut;
queue<cv::Mat> frameQueue;

bool isRunning = true;

void printLog(int msg)
{
    std::cout << "=> :: " << msg << std::endl;
}

void checkForAruco(cv::Mat cameraMatrix, cv::Mat distCoeffs, aruco::ArucoDetector detector, cv::Mat objPoints, std::vector<int> ids, std::vector<std::vector<cv::Point2f>> corners)
{
    while (isRunning)
    {
        if (frameTime % 100 == 1)
        {
            detector.detectMarkers(image, corners, ids);
            if (ids.size() > 0)
            {
                cv::aruco::drawDetectedMarkers(imageCopy, corners, ids);
                int nMarkers = corners.size();
                std::vector<cv::Vec3d> rvecs(nMarkers), tvecs(nMarkers);
                // Calculate pose for each marker
                for (int i = 0; i < nMarkers; i++)
                {
                    solvePnP(objPoints, corners.at(i), cameraMatrix, distCoeffs, rvecs.at(i), tvecs.at(i));
                }
                // Draw axis for each marker
                for (unsigned int i = 0; i < ids.size(); i++)
                {
                    cv::drawFrameAxes(imageCopy, cameraMatrix, distCoeffs, rvecs[i], tvecs[i], (float)0.1);
                }
            }
            printLog(frameTime);
            imageCopy.copyTo(imageOut);
        }
        // Show resulting image and close window
        // std::cout << (frameTime % 3) << " : " << std::endl;
        frameTime += 1;
    }

    // If at least one marker detected
}

int main()
{
    cv::VideoCapture inputVideo;
    inputVideo.open(1);

    cv::Mat cameraMatrix = (Mat1d(3, 3) << 561.01030475, 0, 292.32242872,
                            0, 564.29763955, 272.53754954,
                            0, 0, 1);
    cv::Mat distCoeffs = (Mat1d(1, 5) << -0.23613754, 0.2154634, 0.01353568, -0.01290092, 0.02468145);

    float markerLength = 0.05;

    cv::Mat objPoints(4, 1, CV_32FC3);
    objPoints.ptr<cv::Vec3f>(0)[0] = cv::Vec3f(-markerLength / 2.f, markerLength / 2.f, 0);
    objPoints.ptr<cv::Vec3f>(0)[1] = cv::Vec3f(markerLength / 2.f, markerLength / 2.f, 0);
    objPoints.ptr<cv::Vec3f>(0)[2] = cv::Vec3f(markerLength / 2.f, -markerLength / 2.f, 0);
    objPoints.ptr<cv::Vec3f>(0)[3] = cv::Vec3f(-markerLength / 2.f, -markerLength / 2.f, 0);

    cv::aruco::DetectorParameters detectorParams = cv::aruco::DetectorParameters();
    cv::aruco::Dictionary dictionary = cv::aruco::getPredefinedDictionary(cv::aruco::DICT_6X6_250);
    aruco::ArucoDetector detector(dictionary, detectorParams);

    std::vector<int> ids;
    std::vector<std::vector<cv::Point2f>> corners;

    // looks like these matrices work well even for other cameras.
    // TODO: is there any 'standard' camera matrix that can be applied universally?

    //* cv::Mat image, imageCopy;
    //* std::vector<int> ids;
    //* std::vector<std::vector<cv::Point2f>> corners;

    inputVideo.retrieve(image);
    image.copyTo(imageCopy);
    image.copyTo(imageOut);

    thread th1(checkForAruco, cameraMatrix, distCoeffs, detector, objPoints, ids, corners);
    while (inputVideo.grab())
    {
        inputVideo.retrieve(image);
        image.copyTo(imageCopy);
        char key = (char)cv::waitKey(1);
        cv::imshow("out", imageOut);
        if (key == 27)
        {
            if (!isRunning)
            {
                break;
            }
            isRunning = false;
        }
    }
}

// TODO: place object on aruco
// TODO: implement phone IMU
// TODO:
