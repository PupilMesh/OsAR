import cv2
import pickle
import numpy as np
import plotly.graph_objects as go

def readIm(pathToIm):
    rFac = 6
    im = cv2.imread(pathToIm)
    im = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
    im = np.array(im)
    im = cv2.GaussianBlur(im, (5,5), 0)
    im =  cv2.resize(im, (
        int(im.shape[1] / rFac), # width
        int(im.shape[0] / rFac), # height
    ))
    print(im.shape)
    return im

def retUndistortedIm(im, camMtx, nCamMtx, distCoeff):
    imUndistorted = cv2.undistort(im, camMtx, None, None, nCamMtx)
    return imUndistorted

def retCamMtx(pathToCamMtx):
    with open(f'{pathToCamMtx}', 'rb') as f:
        return pickle.load(f)

def retDistCoeff(pathToDistCoeff):
    with open(f'{pathToDistCoeff}', 'rb') as f:
        return np.array(pickle.load(f))

def ORB_detector(im1, im2, limiter):
    detect = cv2.ORB_create(nfeatures=limiter)
    kp1, des1 = detect.detectAndCompute(im1, None)
    kp2, des2 = detect.detectAndCompute(im2, None)
    # print(kp1[0].pt)
    return (kp1, des1, kp2, des2)

def BRISK_detector(im1, im2):
    brisk = cv2.BRISK_create(thresh=100)
    kp1, des1 = brisk.detectAndCompute(im1, None)
    kp2, des2 = brisk.detectAndCompute(im2, None)
    return (kp1, des1, kp2, des2)

def retKpList(kp):
    kpL = []
    for i in kp:
        kpL.append(i.pt)
    return np.array(kpL)

def bruteForceMatcher(des1, des2):
    bfm = cv2.BFMatcher_create(cv2.NORM_HAMMING2, crossCheck=True)
    numMatches = bfm.match(des1, des2)
    numMatches = sorted(numMatches,key=lambda x:x.distance)
    # print(numMatches[0].imgIdx)
    return numMatches

def bruteForceMatcherkNN(des1, des2):
    bfm = cv2.BFMatcher_create(cv2.NORM_HAMMING2, crossCheck=True)
    matches = bfm.knnMatch(des1, des2, k=1)
    return matches

def retGoodPoints(kps, bfMatches):
    points = np.float32([kps[m.queryIdx].pt for m in bfMatches]).reshape(-1, 1, 2)

def retEssentialMat(kpL1, kpL2, camMtx, dist):
    # print(type(kpL1))
    # kpL1 = np.array(kpL1) # convert to np.array just before operation
    # kpL2 = np.array(kpL2)
    # dist = np.array(dist)
    # NOTE : we convert them to np.array because cv2 expects a 
    # NOTE <cv::UMat> format 
    c =  cv2.findEssentialMat(kpL1, kpL2, camMtx, None, None, None, None)
    return c

def retPoseRecovery(essMtx, kpL1, kpL2):
    c = cv2.recoverPose(essMtx, kpL1, kpL2)
    return c

def retTriangulation(_R, _t, kpL1, kpL2, limiter):
    pts_3d = []
    proj_matrix = np.hstack((_R, _t))
    # print(kpL2[0])
    for i in range(len(kpL1)):
        pts_4d = cv2.triangulatePoints(np.eye(3, 4), proj_matrix, kpL1[i], kpL2[i])
        pts = cv2.convertPointsFromHomogeneous(pts_4d.T)
        # print(pts)
        pts_3d.append([pts[0][0][0], pts[0][0][1], pts[0][0][2]])
    return pts_3d
    # return -1

def display2D(img1, kp1, img2, kp2, numMatches):
    out = cv2.drawMatches(img1, kp1, img2, kp2, numMatches, None)
    cv2.imshow('Image', out)

def display3D(pointCloud):
    x = []
    y = []
    z = []

    for i in pointCloud:
        x.append(i[0])
        y.append(i[1])
        z.append(i[2])

    print(z)

    markerData = go.Scatter3d(
        x = x,
        y = y,
        z = z,
        marker=go.scatter3d.Marker(size=3), 
        opacity=0.8, 
        mode='markers'
    )

    fig = go.Figure(data=markerData)
    fig.show()

    # print(x)
    # print(pointCloud)