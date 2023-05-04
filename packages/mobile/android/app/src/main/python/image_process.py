import cv2
import numpy as np
import os
import urllib.request


def download_image(url):
    resp = urllib.request.urlopen(url)
    image = np.asarray(bytearray(resp.read()), dtype="uint8")
    image = cv2.imdecode(image, cv2.IMREAD_GRAYSCALE)
    return image

def find_images_in_image(img, smaller_images, ratio_threshold=0.7):
    akaze = cv2.AKAZE_create()
    kp1, des1 = akaze.detectAndCompute(img, None)

    img_bgr = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)

    for name, (template, kp2, des2) in smaller_images.items():
        bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
        matches = bf.knnMatch(des1, des2, k=2)

        good_matches = []
        for m, n in matches:
            if m.distance < ratio_threshold * n.distance:
                good_matches.append(m)

        if len(good_matches) > 5:
            src_pts = np.float32([kp1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
            dst_pts = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

            M, _ = cv2.findHomography(dst_pts, src_pts, cv2.RANSAC, 5.0)
            h, w = template.shape

            points = np.float32([[0, 0], [0, h - 1], [w - 1, h - 1], [w - 1, 0]]).reshape(-1, 1, 2)
            dst = cv2.perspectiveTransform(points, M)

            # Draw the bounding polygon and the marker name on the larger image
            img_bgr = cv2.polylines(img_bgr, [np.int32(dst)], True, (0, 255, 0), 3, cv2.LINE_AA)
            img_bgr = cv2.putText(img_bgr, name, tuple(np.int32(dst[0][0])), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2, cv2.LINE_AA)

    return img_bgr

# Initialize the USB camera
cap = cv2.VideoCapture(cv2.CAP_DSHOW + 1)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
cap.set(cv2.CAP_PROP_FPS, 30)
cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
cap.set(cv2.CAP_PROP_CONVERT_RGB, 1)

akaze = cv2.AKAZE_create()
curr_dir= os.getcwd()

# Load the smaller images and precompute keypoints and descriptors
# smaller_images = {
#     "image1": (cv2.imread("C:/Users/Krishnan/Desktop/PupilMesh/Python/marker.jpeg", cv2.IMREAD_GRAYSCALE), *akaze.detectAndCompute(cv2.imread(curr_dir+"/Python/marker.jpeg", cv2.IMREAD_GRAYSCALE), None)),
#     "image2": (cv2.imread("C:/Users/Krishnan/Desktop/PupilMesh/Python/marker7.jpeg", cv2.IMREAD_GRAYSCALE), *akaze.detectAndCompute(cv2.imread("C:/Users/Krishnan/Desktop/PupilMesh/Python/marker7.jpeg", cv2.IMREAD_GRAYSCALE), None)),
#     # Add more images as needed
# }

image1_url = "https://res.cloudinary.com/doblnhena/image/upload/v1682431189/marker_lpmhnx.jpg"
image2_url = "https://res.cloudinary.com/doblnhena/image/upload/v1682431190/marker7_e5h36e.jpg"

smaller_images = {
        "image1": (download_image(image1_url), *akaze.detectAndCompute(download_image(image1_url), None)),
        "image2": (download_image(image2_url), *akaze.detectAndCompute(download_image(image2_url), None)),
        # Add more images as needed
    }

def detect_marker(image_bytes):
    # Capture a frame from the USB camera
    # convert it to a numpy array using np.frombuffer() 
    image_np = np.frombuffer(image_bytes, dtype=np.uint8)

    # decode the numpy array into an OpenCV image using cv2.imdecode()
    frame = cv2.imdecode(image_np, flags=cv2.IMREAD_COLOR)
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    # Find the smaller images in the captured frame
    result = find_images_in_image(gray_frame, smaller_images)

    if result is not None:
        # encode the image as a ByteArray using cv2.imencode()
        retval, buffer = cv2.imencode('.png', result)
        image_bytes = np.array(buffer).tobytes()
        return image_bytes
    else:
        return image_bytes
    

