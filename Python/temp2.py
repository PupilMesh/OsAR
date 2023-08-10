import cv2
import numpy as np
import os


def find_image_in_image(larger_image_path, smaller_image_path, ratio_threshold=0.7):
    img = cv2.imread(larger_image_path, cv2.IMREAD_GRAYSCALE)
    template = cv2.imread(smaller_image_path, cv2.IMREAD_GRAYSCALE)

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
        print("Image not found")
        return None

if __name__ == "__main__":
    curr_dir= os.getcwd()
    larger_image_path = curr_dir+"\Python\source.jpeg"
    smaller_image_path = curr_dir+"\Python\marker.jpeg"

    result = find_image_in_image(larger_image_path, smaller_image_path)

    if result is not None:
        cv2.imshow("Result", result)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
