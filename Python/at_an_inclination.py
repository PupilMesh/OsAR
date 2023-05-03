import cv2
import numpy as np
import os
def find_images_in_image(img, smaller_images, ratio_threshold=0.7):
    orb = cv2.ORB_create()
    kp1, des1 = orb.detectAndCompute(img, None)

    img_bgr = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)

    for name, (template, kp2, des2) in smaller_images.items():
        bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
        matches = bf.knnMatch(des1, des2, k=2)

        good_matches = []
        for m, n in matches:
            if m.distance < ratio_threshold * n.distance:
                good_matches.append(m)

        if len(good_matches) > 10:
            src_pts = np.float32([kp1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
            dst_pts = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

            M, _ = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC, 5.0)
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

orb = cv2.ORB_create()

curr_dir= os.getcwd()

# Load the smaller images and precompute keypoints and descriptors
smaller_images = {
    "image1": (cv2.imread(curr_dir+"/Python/marker.jpeg", cv2.IMREAD_GRAYSCALE), *orb.detectAndCompute(cv2.imread(curr_dir+"/Python/marker.jpeg", cv2.IMREAD_GRAYSCALE), None)),
    "image2": (cv2.imread(curr_dir+"/Python/marker7.jpeg", cv2.IMREAD_GRAYSCALE), *orb.detectAndCompute(cv2.imread(curr_dir+"/Python/marker7.jpeg", cv2.IMREAD_GRAYSCALE), None)),
    # Add more images as needed
}
while True:
    
        # Capture a frame from the USB camera
    ret, frame = cap.read()
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    # Find the smaller images in the captured frame
    result = find_images_in_image(gray_frame, smaller_images)

    if result is not None:
        cv2.imshow('USB Camera', result)
    else:
        cv2.imshow('USB Camera', frame)

    # Exit the program if the 'q' key is pressed
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break