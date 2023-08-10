# Precompute


Precompute is a Python application that detects and recognizes smaller images within a larger image captured from a USB camera. It uses the AKAZE feature detector and descriptor for image matching and the Brute-Force Matcher for finding correspondences between features. Detected smaller images are highlighted with a bounding polygon and labeled with their respective names.

## Getting Started

Follow these instructions to set up and run Precompute on your local machine.

### Prerequisites

1. Python 3.x installed.
2. OpenCV library installed (version 4.x recommended). You can install it via pip:
   ```
   pip install opencv-python
   ```

### Usage

1. Clone the repository or copy the provided code to a new Python file (e.g., `precompute.py`).
2. Place the smaller images you want to detect in the same directory as the Python script or adjust the file paths in the code. Add or remove images in the `smaller_images` dictionary accordingly.
3. Run the Python script:
   ```
   python precompute.py
   ```
4. The application will open a new window displaying the camera feed with detected smaller images highlighted and labeled. Press 'q' to exit the program.

## How It Works

1. The script initializes the USB camera and sets its properties, such as resolution and frame rate.
2. It loads the smaller images and precomputes their keypoints and descriptors using the AKAZE feature detector and descriptor.
3. For each frame captured from the USB camera, the script converts the frame to grayscale and searches for the smaller images.
4. The search process involves matching keypoints and descriptors, filtering out poor matches, and estimating a homography transformation. If enough good matches are found, the script highlights the smaller image with a bounding polygon and labels it with the image's name.
5. The processed frame is displayed in a window. The program continues to process new frames until the user presses 'q' to exit.
