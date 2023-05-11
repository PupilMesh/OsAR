import cv2
import os

from utils import *

imDIR = './data/images/'
limiter = 1200 # number of matches. cv2 errors out if I utilize more than 1200. Why ??
imgs = os.listdir(imDIR)
imSEQ = list(zip(imgs, imgs[1:]))
pts_3d = []

camMtx = retCamMtx("./data/calibration/cameraMatrix.pkl")
nCamMtx = retCamMtx("./data/calibration/newCameraMatrix.pkl")
distCoeff = retDistCoeff("./data/calibration/dist.pkl")

for i in imSEQ:
    img1 = readIm(f'{imDIR}{i[0]}')
    img2 = readIm(f'{imDIR}{i[1]}')
    img1 = retUndistortedIm(img1, camMtx, nCamMtx, distCoeff)
    img2 = retUndistortedIm(img2, camMtx, nCamMtx, distCoeff)

    kp1, des1, kp2, des2 = ORB_detector(img1, img2, limiter)
    # kp1, des1, kp2, des2 = BRISK_detector(img1, img2)
    kpL1 = retKpList(kp1)
    kpL2 = retKpList(kp2)
    numMatches = bruteForceMatcher(des1, des2)

    essMtx, _ = retEssentialMat(kpL1, kpL2, camMtx, distCoeff)
    _, R, t, mask = retPoseRecovery(essMtx, kpL1, kpL2)
    pts_3d.extend(retTriangulation(R, t, kpL1, kpL2, limiter))
    # print(f'{pts_3d}')
    display2D(img1, kp1, img2, kp2, numMatches)
    cv2.waitKey(1)

# FIXME: almost no depth in the images, might be caused
# FIXME: by incorrect calibration matrix.
display3D(pts_3d)

cv2.destroyAllWindows()

