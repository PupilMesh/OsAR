import cv2
import numpy as np
import os
def find_image_in_image(img, template, ratio_threshold=0.7):
    akaze = cv2.AKAZE_create()
    kp1, des1 = akaze.detectAndCompute(img, None)
    kp2, des2 = akaze.detectAndCompute(template, None)

    bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
    matches = bf.knnMatch(des1, des2, k=2)

    good_matches = []
    for m, n in matches:
        if m.distance < ratio_threshold * n.distance:
            good_matches.append(m)

    if len(good_matches) > 10:
        src_pts = np.float32([kp1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
        dst_pts = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

        M, _ = cv2.findHomography(dst_pts, src_pts, cv2.RANSAC, 5.0)
        h, w = template.shape

        points = np.float32([[0, 0], [0, h - 1], [w - 1, h - 1], [w - 1, 0]]).reshape(-1, 1, 2)
        dst = cv2.perspectiveTransform(points, M)

        img_bgr = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)

        # Draw the bounding polygon on the larger image
        img_with_bbox = cv2.polylines(img_bgr, [np.int32(dst)], True, (0, 255, 0), 3, cv2.LINE_AA)

        return img_with_bbox
    else:
        return None

# Initialize the USB camera
cap = cv2.VideoCapture(cv2.CAP_DSHOW + 0)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
cap.set(cv2.CAP_PROP_FPS, 30)
cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
cap.set(cv2.CAP_PROP_CONVERT_RGB, 1)

# Load the smaller image
curr_dir= os.getcwd()
smaller_image_path = curr_dir+"\Python\marker.jpeg"
smaller_image = cv2.imread(smaller_image_path, cv2.IMREAD_GRAYSCALE)

while True:
    # Capture a frame from the USB camera
    ret, frame = cap.read()
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    # Find the smaller image in the captured frame
    result = find_image_in_image(gray_frame, smaller_image)

    if result is not None:
        cv2.imshow('USB Camera', result)
    else:
        cv2.imshow('USB Camera', frame)

    # Exit the program if the 'q' key is pressed
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release the USB camera and close the window
cap.release()
cv2.destroyAllWindows()
