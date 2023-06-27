#include <iostream>
#include <opencv2/aruco.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgcodecs.hpp>
#include "aruco.hpp"

using namespace cv;
using namespace std;

int arucoDect::callingCheck(int abc){
    return abc;
}
Mat arucoDect::generateGrid(vector<Mat> images,int rows,int cols,int margin,int padding){
    int img_count = 0;
    int width = images[0].cols;
    int height = images[0].rows;
    int grid_width = width*cols + padding*(cols-1) + margin*2;
    int grid_height = height*rows + padding*(rows-1) + margin*2;
    cv::Mat grid(grid_height, grid_width, images[0].type(), cv::Scalar(255, 255, 255));
    for (int r = 0; r < rows; ++r)
    {
        for (int c = 0; c < cols; ++c)
        {
            if (img_count >= images.size())
            {
                break;
            }
            cv::Rect roi(margin + c*(width+padding), margin + r*(height+padding), width, height);
            cv::Mat target = grid(roi);
            images[img_count].copyTo(target);
            ++img_count;
        }
    }
    return grid;
}
vector<Mat> arucoDect::generateMarker(int numMarkers,int markerSize,aruco::PREDEFINED_DICTIONARY_NAME name){
    vector<Mat> images;
    Ptr<aruco::Dictionary> dictionary = aruco::getPredefinedDictionary(name);    
    // Loop through and generate each marker
    for (int i = 0; i < numMarkers; i++)
    {
        // Generate the marker
        Mat markerImage;
        aruco::drawMarker(dictionary, i, markerSize, markerImage, 1);
        // Save the marker image to a file
        images.push_back(markerImage);
        //imwrite("D:\\C++\\testing\\ArucoMarker\\" + filename, markerImage);
    }
    return images;
}
void arucoDect::detectMarker(Mat frame){
    Ptr<aruco::Dictionary> dict= aruco::getPredefinedDictionary(aruco::DICT_6X6_250);
     // create a window to show the camera feed
    
    vector<vector<Point2f>> corner;
    vector<int> ids;
    aruco::detectMarkers(frame,dict,corner,ids);
    if(ids.size()>0){
        // aruco::drawDetectedMarkers(frame,corner,ids);
        LOG_D("marker","vecto id %d corner %d %d %d %d",ids[0],corner[0][0],corner[0][1],corner[0][2],corner[0][3]);
    }
    else{
        LOG_D("marker","Maker not detected");
    }
}
/* int main()
{
    // load the image
    Mat img = imread("D:\\C++\\testing\\image.jpeg");
    if (img.empty()) {
        std::cerr << "Error: could not load image" << std::endl;
        return -1;
    }

    // define the cube's 8 vertices
    std::vector<Point3f> vertices {
        {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0},
        {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}
    };

    // define the cube's 6 faces (as vertex indices)
    std::vector<std::vector<int>> faces {
        {0, 1, 2, 3}, {0, 4, 5, 1}, {1, 5, 6, 2},
        {2, 6, 7, 3}, {3, 7, 4, 0}, {4, 7, 6, 5}
    };

    // define the cube's colors (one for each face)
    std::vector<Scalar> colors {
        {255, 0, 0}, {0, 255, 0}, {0, 0, 255},
        {255, 255, 0}, {0, 255, 255}, {255, 0, 255}
    };

    // project the cube's vertices onto the image plane
    Matx33f K(100, 0, img.cols/2, 0, 100, img.rows/2, 0, 0, 1); // intrinsic matrix (assume focal length of 100)
    Matx41f distortion(0, 0, 0, 0); // distortion coefficients (none)
    std::vector<Point2f> image_points;
    projectPoints(vertices, Vec3f(0, 0, 0), Vec3f(0, 0, 1), K, distortion, image_points);

    // draw the cube onto the image
    for (int i = 0; i < faces.size(); i++) {
        std::vector<Point> face_points;
        for (int j = 0; j < faces[i].size(); j++) {
            face_points.push_back(Point(image_points[faces[i][j]]));
        }
        fillConvexPoly(img, face_points, colors[i]);
    }

    // display the result
    imshow("image with cube", img);
    waitKey(0);

    return 0;
}

*/