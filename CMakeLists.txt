cmake_minimum_required(VERSION 3.0.0)
project(marker-tracking VERSION 0.1.0)

include(CTest)
enable_testing()

add_executable(marker-tracking main.cpp)

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)

# LIBS

find_package(OpenCV CONFIG REQUIRED)
target_link_libraries(${PROJECT_NAME} PRIVATE opencv_videoio opencv_video opencv_stitching opencv_photo opencv_objdetect opencv_ml opencv_imgproc opencv_imgcodecs opencv_highgui opencv_imgcodecs opencv_imgproc opencv_calib3d opencv_core opencv_features2d opencv_ml opencv_dnn opencv_core opencv_flann)


