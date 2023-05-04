# cmake_minimum_required(VERSION 3.18.1)
# project(testing VERSION 0.1.0 )
# # Set the C++ standard to use
# set(CMAKE_CXX_STANDARD 17)
# set(CMAKE_CXX_STANDARD_REQUIRED ON)
# # Add source files to the project
# enable_testing()
# include(CTest)
# find_package(OpenCV REQUIRED)
# include_directories(${OpenCV_INCLUDE_DIRS})

# add_executable(testing demo.cpp)
# target_link_libraries(testing ${OpenCV_LIBS})
# set(CPACK_PROJECT_NAME ${PROJECT_NAME})

# set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})

# include(CPack)
cmake_minimum_required(VERSION 3.15)
project(testing)

set(CMAKE_CXX_STANDARD 17)
set(OpenCV_DIR "D:\\opencv-4.6.0\\build")
find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})
add_executable(testing aruco.cpp)
target_link_libraries(testing ${OpenCV_LIBS})