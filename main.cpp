#include <opencv2/opencv.hpp>
#include <iostream>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>

using namespace cv;
using namespace std;

// camMtx
// [[561.01030475   0.         292.32242872]
//  [  0.         564.29763955 272.53754954]
//  [  0.           0.           1.        ]]

// distCoeff
// [[-0.23613754  0.2154634   0.01353568 -0.01290092  0.02468145]]

int main()
{
    cv::VideoCapture inputVideo;
    inputVideo.open(1);

    cv::Mat cameraMatrix = (Mat1d(3, 3) << 
        561.01030475, 0, 292.32242872, 
        0, 564.29763955, 272.53754954,
        0, 0, 1
    );
    cv::Mat distCoeffs = (Mat1d(1,5) << 
        -0.23613754, 0.2154634, 0.01353568, -0.01290092, 0.02468145
    );
    // looks like these matrices work well even for other cameras.
    // TODO: is there any 'standard' camera matrix that can be applied universally?

    float markerLength = 0.05;

    // You can read camera parameters from tutorial_camera_params.yml
    // readCameraParameters(cameraParamsFilename, cameraMatrix, distCoeffs); // This function is implemented in aruco_samples_utility.hpp
    // Set coordinate system

    cv::Mat objPoints(4, 1, CV_32FC3);
    objPoints.ptr<cv::Vec3f>(0)[0] = cv::Vec3f(-markerLength / 2.f, markerLength / 2.f, 0);
    objPoints.ptr<cv::Vec3f>(0)[1] = cv::Vec3f(markerLength / 2.f, markerLength / 2.f, 0);
    objPoints.ptr<cv::Vec3f>(0)[2] = cv::Vec3f(markerLength / 2.f, -markerLength / 2.f, 0);
    objPoints.ptr<cv::Vec3f>(0)[3] = cv::Vec3f(-markerLength / 2.f, -markerLength / 2.f, 0);

    cv::aruco::DetectorParameters detectorParams = cv::aruco::DetectorParameters();
    cv::aruco::Dictionary dictionary = cv::aruco::getPredefinedDictionary(cv::aruco::DICT_6X6_250);
    aruco::ArucoDetector detector(dictionary, detectorParams);

    while (inputVideo.grab())
    {
        cv::Mat image, imageCopy;
        inputVideo.retrieve(image);
        image.copyTo(imageCopy);
        std::vector<int> ids;
        std::vector<std::vector<cv::Point2f>> corners;
        detector.detectMarkers(image, corners, ids);

        // If at least one marker detected
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
                cv::drawFrameAxes(imageCopy, cameraMatrix, distCoeffs, rvecs[i], tvecs[i], (float) 0.1);
            }
        }
        // Show resulting image and close window
        cv::imshow("out", imageCopy);
        char key = (char)cv::waitKey(1);
        if (key == 27)
            break;
    }
}

// TODO: place object on aruco
// TODO: implement phone IMU
// TODO: 
