import numpy as np
import cv2
import os

from utils import *

# Fisheye camera and distortion matrices
K=np.array([[455.5000196386718, 0.0, 482.65324003911945], 
            [0.0, 340.6409393462825, 254.5063795692748], 
            [0.0, 0.0, 1.0]])
D=np.array([[-0.018682808343432777], 
            [-0.044315351694893736], 
            [0.047678551616171246], 
            [-0.018283908577445218]])

K = retCamMtx("./data/calibration/cameraMatrix.pkl")
D = retDistCoeff("./data/calibration/dist.pkl")

orb = cv2.ORB_create(nfeatures=1000)
bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)

imDIR = './data/images/'
limiter = 100 # number of matches
imgs = os.listdir(imDIR)
imSEQ = list(zip(imgs, imgs[1:]))
pts_3d = np.array([0,0,0])

for i in imSEQ:
    img0 = cv2.imread(f"{imDIR}{i[0]}")
    img2 = cv2.imread(f"{imDIR}{i[1]}")

    # Find keypoints and match them up
    kp0, des0 = orb.detectAndCompute(img0, None)
    kp2, des2 = orb.detectAndCompute(img2, None)
    matches = bf.knnMatch(des0, des2, k=2)

    display2DKNN(img0, kp0, img2, kp2, matches[-20:])

    # Find good matches using the ratio test
    ratio_thresh = 0.9
    good_matches = []
    for m,n in matches:
        if m.distance < ratio_thresh * n.distance:
            good_matches.append(m)

    # Convert from keypoints to points
    pts0 = np.float32([kp0[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
    pts2 = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

    # pts0, pts2 = retGoodPoints(kp0, kp2, matches)

    # Remove the fisheye distortion from the points
    # pts0 = cv2.fisheye.undistortPoints(pts0, K, D, P=K)
    # pts2 = cv2.fisheye.undistortPoints(pts2, K, D, P=K)
    pts0 = cv2.undistortPoints(pts0, K, D, P=K)
    pts2 = cv2.undistortPoints(pts2, K, D, P=K)

    # Keep only the points that make geometric sense
    # TODO: find a more efficient way to apply the mask
    E, mask = cv2.findEssentialMat(pts0, pts2, K, cv2.RANSAC, 0.999, 1, None)
    _, R, t, mask = cv2.recoverPose(E, pts0, pts2, cameraMatrix=K, mask=mask)
    pts0_m = []
    pts2_m = []
    for i in range(len(mask)):
        print(mask[i])
        if mask[i] == 1:
            pts0_m.append(pts0[i])
            pts2_m.append(pts2[i])
    pts0 = np.array(pts0_m).T.reshape(2, -1)
    pts2 = np.array(pts2_m).T.reshape(2, -1)

    # Setup the projection matrices
    R = np.eye(3)
    t0 = np.array([[0], [0], [0]])
    t2 = t
    # t2 = np.array([[0], [0], [2]])
    P0 = np.dot(K, np.concatenate((R, t0), axis=1))
    P2 = np.dot(K, np.concatenate((R, t2), axis=1))

    # Find the keypoint world homogeneous coordinates assuming img0 is the world origin
    X = cv2.triangulatePoints(P0, P2, pts0, pts2)

    # Convert from homogeneous cooridinates
    X /= X[3]
    objPts = X.T[:,:3]
    np.append(pts_3d, objPts)

    # Find the pose of the second frame
    _, rvec, tvec, inliers = cv2.solvePnPRansac(objPts, pts2.T, K, None)
# print(rvec)
    print(tvec)
    cv2.waitKey(20)
display3D(objPts)