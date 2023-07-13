import cv2
import pickle
import numpy as np
import plotly.graph_objects as go

def readIm(pathToIm):
    rFac = 1
    im = cv2.imread(pathToIm)
    im = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
    im = np.array(im)
    im = cv2.GaussianBlur(im, (3,3), 0)
    im =  cv2.resize(im, (
        int(im.shape[1] / rFac), # width
        int(im.shape[0] / rFac), # height
    ))
    print(im.shape)
    return im

def retUndistortedIm(im, camMtx, nCamMtx, distCoeff):
    imUndistorted = cv2.undistort(im, camMtx, distCoeff, None, nCamMtx)
    return imUndistorted

def retUndistortedPts(kp1, kp2, camMtx, distCoeff):
    pts1 = cv2.undistortPoints(kp1, camMtx, distCoeff, P=camMtx)
    pts2 = cv2.undistortPoints(kp2, camMtx, distCoeff, P=camMtx)
    return pts1, pts2

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

def retKpList(kp):
    kpL = []
    for i in kp:
        kpL.append(i.pt)
    return np.array(kpL)

def bruteForceMatcher(des1, des2):
    bfm = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
    numMatches = bfm.match(des1, des2)
    numMatches = sorted(numMatches,key=lambda x:x.distance)
    # print(numMatches[0].imgIdx)
    return numMatches

def bruteForceMatcherkNN(des1, des2):
    bfm = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
    matches = bfm.knnMatch(des1, des2, k=2)
    return matches

def retGoodPoints(kp1, kp2, bfMatches):
    good_matches = []
    ratio = 0.8
    for m, n in bfMatches:
        if m.distance < ratio * n.distance:
            good_matches.append(m)
    pts1 = np.float32([kp1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
    pts2 = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)
    return pts1, pts2

def retEssentialMat(kpL1, kpL2, camMtx, distCoeff):
    # print(type(kpL1))
    kpL1 = np.array(kpL1) # convert to np.array just before operation
    kpL2 = np.array(kpL2)
    # NOTE : we convert them to np.array because cv2 expects a 
    # NOTE <cv::UMat> format 
    c =  cv2.findEssentialMat(kpL1, kpL2, camMtx)
    return c

def retPoseRecovery(essMtx, kpL1, kpL2, camMtx, mask):
    print(essMtx)
    c = cv2.recoverPose(essMtx, kpL1, kpL2, camMtx, mask=mask)
    return c

def retGoodGeometricPoints(mask, kp1, kp2):
    pts1 = []
    pts2 = []
    for i in range(len(mask)):
        print(mask[i])
        if mask[i] == 1: # this returns 1 only after undistortion
            pts1.append(kp1[i])
            pts2.append(kp2[i])
    pts1_good = np.array(pts1).T.reshape(2, -1)
    pts2_good = np.array(pts2).T.reshape(2, -1)
    return pts1_good, pts2_good

def retTriangulation(_R, _t, kp1, kp2, camMtx):
    pts_3d = []
    # Set up the scene such that the first image
    # is the world origin
    R = np.eye(3)
    t1 = np.array([[0], [0], [0]])
    t2 = _t
    P1 = np.dot(camMtx, np.concatenate((R, t1), axis=1))
    P2 = np.dot(camMtx, np.concatenate((R, t2), axis=1))

    X = cv2.triangulatePoints(P1, P2, kp1, kp2)
    X /= X[3]
    objPts = X.T[:,:3]

    # pts4d = cv2.triangulatePoints(proj_matrix0, proj_matrix, kpL1.T, kpL2.T).T
    # pts4d /= pts4d[:, 3:]
    # out = list(np.delete(pts4d, 3, 1))
    # pts_3d.append(out)

    return objPts
    # return -1

def display2D(img1, kp1, img2, kp2, numMatches):
    out = cv2.drawMatches(img1, kp1, img2, kp2, numMatches, None)
    cv2.imshow('Image', out)

def display2DKNN(img1, kp1, img2, kp2, numMatches):
    out = cv2.drawMatchesKnn(img1, kp1, img2, kp2, numMatches, None)
    cv2.imshow('Image', out)

def display3D(pointCloud):
    x = []
    y = []
    z = []

    print(pointCloud)

    for imgPts in pointCloud:
        x.append(imgPts[0])
        y.append(imgPts[1])
        z.append(imgPts[2])

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

    # print(pointCloud)

def linear_LS_triangulation(u1, P1, u2, P2):
    linear_LS_triangulation_C = -np.eye(2, 3)
    """
    Linear Least Squares based triangulation.
    Relative speed: 0.1
    
    (u1, P1) is the reference pair containing normalized image coordinates (x, y) and the corresponding camera matrix.
    (u2, P2) is the second pair.
    
    u1 and u2 are matrices: amount of points equals #rows and should be equal for u1 and u2.
    
    The status-vector will be True for all points.
    """
    A = np.zeros((4, 3))
    b = np.zeros((4, 1))
    
    # Create array of triangulated points
    x = np.zeros((3, len(u1)))
    
    # Initialize C matrices
    C1 = np.array(linear_LS_triangulation_C)
    C2 = np.array(linear_LS_triangulation_C)
    
    for i in range(len(u1)):
        # Derivation of matrices A and b:
        # for each camera following equations hold in case of perfect point matches:
        #     u.x * (P[2,:] * x)     =     P[0,:] * x
        #     u.y * (P[2,:] * x)     =     P[1,:] * x
        # and imposing the constraint:
        #     x = [x.x, x.y, x.z, 1]^T
        # yields:
        #     (u.x * P[2, 0:3] - P[0, 0:3]) * [x.x, x.y, x.z]^T     +     (u.x * P[2, 3] - P[0, 3]) * 1     =     0
        #     (u.y * P[2, 0:3] - P[1, 0:3]) * [x.x, x.y, x.z]^T     +     (u.y * P[2, 3] - P[1, 3]) * 1     =     0
        # and since we have to do this for 2 cameras, and since we imposed the constraint,
        # we have to solve 4 equations in 3 unknowns (in LS sense).

        # Build C matrices, to construct A and b in a concise way
        C1[:, 2] = u1[i, :]
        C2[:, 2] = u2[i, :]
        
        # Build A matrix:
        # [
        #     [ u1.x * P1[2,0] - P1[0,0],    u1.x * P1[2,1] - P1[0,1],    u1.x * P1[2,2] - P1[0,2] ],
        #     [ u1.y * P1[2,0] - P1[1,0],    u1.y * P1[2,1] - P1[1,1],    u1.y * P1[2,2] - P1[1,2] ],
        #     [ u2.x * P2[2,0] - P2[0,0],    u2.x * P2[2,1] - P2[0,1],    u2.x * P2[2,2] - P2[0,2] ],
        #     [ u2.y * P2[2,0] - P2[1,0],    u2.y * P2[2,1] - P2[1,1],    u2.y * P2[2,2] - P2[1,2] ]
        # ]
        A[0:2, :] = C1.dot(P1[0:3, 0:3])    # C1 * R1
        A[2:4, :] = C2.dot(P2[0:3, 0:3])    # C2 * R2
        
        # Build b vector:
        # [
        #     [ -(u1.x * P1[2,3] - P1[0,3]) ],
        #     [ -(u1.y * P1[2,3] - P1[1,3]) ],
        #     [ -(u2.x * P2[2,3] - P2[0,3]) ],
        #     [ -(u2.y * P2[2,3] - P2[1,3]) ]
        # ]
        b[0:2, :] = C1.dot(P1[0:3, 3:4])    # C1 * t1
        b[2:4, :] = C2.dot(P2[0:3, 3:4])    # C2 * t2
        b *= -1
        
        # Solve for x vector
        cv2.solve(A, b, x[:, i:i+1], cv2.DECOMP_SVD)
    
    return x.T.astype(float), np.ones(len(u1), dtype=bool)
