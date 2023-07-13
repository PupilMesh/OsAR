import cv2
import numpy as np

# Load images and extract features
img_paths = ['./Images/img9.png', './Images/img10.png', './Images/img11.png']
images = [cv2.imread(p) for p in img_paths]
gray_images = [cv2.cvtColor(img, cv2.COLOR_BGR2GRAY) for img in images]
features = []
sift = cv2.SIFT_create()
for gray in gray_images:
    kp, des = sift.detectAndCompute(gray, None)
    features.append((kp, des))

# Estimate camera parameters
K = np.zeros((3, 3))
dist_coef = np.zeros((5, 1))
# print(f[0] for f in features)
print(features[0][0][0].pt)
ret, K, dist_coef, rvecs, tvecs = cv2.calibrateCamera(
    [np.float32(f[0]) for f in features], 
    [np.float32(f[1]) for f in features], 
    gray_images[0].shape[::-1], K, dist_coef
)

# Match features between images
matcher = cv2.BFMatcher(cv2.NORM_L2)
matches = []
for i in range(len(features)-1):
    match = matcher.match(features[i][1], features[i+1][1])
    matches.append(match)

# Estimate camera poses
points3d = []
cameras = []
for i, match in enumerate(matches):
    p1 = np.float32([features[i][0][m.queryIdx].pt for m in match]).reshape(-1, 1, 2)
    p2 = np.float32([features[i+1][0][m.trainIdx].pt for m in match]).reshape(-1, 1, 2)
    E, mask = cv2.findEssentialMat(p1, p2, K)
    _, R, t, mask = cv2.recoverPose(E, p1, p2, K)
    cameras.append((R, t))
    
    # Triangulate 3D points
    p1_norm = cv2.undistortPoints(p1, K, dist_coef)
    p2_norm = cv2.undistortPoints(p2, K, dist_coef)
    proj_mat1 = np.hstack((np.eye(3), np.zeros((3, 1))))
    proj_mat2 = np.hstack((R, t))
    points4d = cv2.triangulatePoints(proj_mat1, proj_mat2, p1_norm, p2_norm)
    points4d /= points4d[3]
    points3d.append(points4d[:3].T)

# Bundle adjustment
points3d = np.vstack(points3d)
cameras = np.array(cameras)
_, K_opt, dist_coef_opt, R_opt, t_opt, points3d_opt = cv2.fisheye.bundleAdjuster(
    points3d, cameras, K, dist_coef, rvecs, tvecs, 
    cv2.fisheye.FISHEYE_ESTIMATOR_P3P, 
    (cv2.TERM_CRITERIA_EPS+cv2.TERM_CRITERIA_MAX_ITER, 30, 1e-6)
)

# Save results
np.savetxt("points3d.txt", points3d_opt)
np.savetxt("K.txt", K_opt)
np.savetxt("dist_coef.txt", dist_coef_opt)
