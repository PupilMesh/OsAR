import cv2
import os

from utils import *

imDIR = './Images/'
limiter = 1000 # number of matches
imgs = os.listdir(imDIR)
imSEQ = list(zip(imgs, imgs[1:]))
iterNum = 1
pts_3d = []

camMtx = retCamMtx("./cameraMatrix.pkl")
nCamMtx = retCamMtx("./newCameraMatrix.pkl")
distCoeff = retDistCoeff("./dist.pkl")

for i in imSEQ:
    print(iterNum)
    img1 = readIm(f'{imDIR}{i[0]}')
    img2 = readIm(f'{imDIR}{i[1]}')
    img1 = retUndistortedIm(img1, camMtx, camMtx, distCoeff)
    img2 = retUndistortedIm(img2, camMtx, camMtx, distCoeff)

    kp1, des1, kp2, des2 = ORB_detector(img1, img2, limiter)
    # kp1, des1, kp2, des2 = BRISK_detector(img1, img2)
    # kpL1 = retKpList(kp1)
    # kpL2 = retKpList(kp2)

    numMatches = bruteForceMatcherkNN(des1, des2)
    kp1, kp2 = retGoodPoints(kp1, kp2, numMatches)
    kp1 = cv2.undistortPoints(kp1, camMtx, distCoeff, camMtx)
    kp2 = cv2.undistortPoints(kp2, camMtx, distCoeff, camMtx)

    essMtx, mask = retEssentialMat(kp1, kp2, camMtx, distCoeff)
    _, R, t, mask = retPoseRecovery(essMtx, kp1, kp2, camMtx, mask)
    kp1, kp2 = retGoodGeometricPoints(mask, kp1, kp2)

    pts_3d.extend(retTriangulation(R, t, kp1, kp2, camMtx))
    # print(f'R: {R}\nt: {t}')
    # display2DKNN(img1, kp1, img2, kp2, numMatches)
    
    iterNum += 1
    cv2.waitKey(1)

display3D(pts_3d)

cv2.destroyAllWindows()

