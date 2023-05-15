# OsAR

Open Source AR SDK for USB Powered Smart AR/MR Glasses.


# Installation

 - Clone the repository.
 - Make sure that [vcpkg](https://blog.elijahlopez.ca/posts/vcpkg-cmake-tutorial/) is installed and configured.
 - Run the command `vcpkg.exe "@install_dependency.txt`.
 - Build and run `main.cpp`.
 

# TODOs
```mermaid
graph TD

A:::done
C:::notDone
D:::notDone

A[Pose Estimation]
C[3D object display on marker]
D[Implement IMU from android]

A -.-> B[Figure out universal camera matrices]
A ---> C
C --> D
