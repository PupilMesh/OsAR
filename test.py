import cv2
import numpy as np
import matplotlib.pyplot as plt

# Load the input image
img1 = cv2.imread('imL.jpg')
img2 = cv2.imread('imR.jpg')

# Convert the input image to grayscale
gray1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
gray2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)

# Compute the disparity map using the StereoBM algorithm
stereo = cv2.StereoSGBM_create(numDisparities=96, blockSize=23)
disparity = stereo.compute(gray1, gray2)
disparity = cv2.convertScaleAbs(disparity)

# Convert the disparity map to a depth map using the focal length and baseline
focal_length = 0.8 # in pixels
baseline = 0.1 # distance between the cameras in meters. get this from the tVEC
depth_map = np.zeros_like(disparity).astype(np.float32)
depth_map[disparity > 0] = focal_length * baseline / disparity[disparity > 0]

# Normalize the depth map for display
depth_map_norm = cv2.normalize(depth_map, None, 0, 255, cv2.NORM_MINMAX)

# Display the depth map
# plt.imshow('Image', disparity)
# plt.show()

cv2.imshow('Image', disparity)
cv2.waitKey(0)

# cv2.waitKey(0)
# cv2.destroyAllWindows()
