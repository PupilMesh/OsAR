import numpy as np
import cv2
import matplotlib.pyplot as plt
import pickle

capture = cv2.VideoCapture(0)

frameHistory = []

FX_DEPTH = 5.8262448167737955e+02
FY_DEPTH = 5.8269103270988637e+02
CX_DEPTH = 3.1304475870804731e+02
CY_DEPTH = 2.3844389626620386e+02

camMtx = None
nCamMtx = None
distCoff = None

pcd = []

with open('cameraMatrix.pkl', 'rb') as f:
    camMtx = pickle.load(f)
    print(type(camMtx), camMtx)
    f.close()
with open('newCameraMatrix.pkl', 'rb') as f:
    nCamMtx = pickle.load(f)
    print(type(nCamMtx), nCamMtx)
    f.close()
with open('dist.pkl', 'rb') as f:
    distCoff = pickle.load(f)
    print(type(distCoff), distCoff)
    f.close()


while True:
    ret, frame = capture.read()

    # frame = cv2.undistort(frame, camMtx, distCoff, None, nCamMtx)
    frameHistory.append(frame)
    frameHistory = frameHistory[-2:]
    
    # Undistorting the image does not provide any noticeable difference
    # to the result 

    frameCrop = int(frame.shape[1]*0.80)
    cLeft = frame[0:-1, 0:0+frameCrop]
    cRight = frame[0:-1, frame.shape[1]-frameCrop-1:-1]

    # cLeft = frame
    # cRight = frameHistory[0]

    # at this point the best way to generate stereo images is to 
    # generate camera tVEC and rVEC, which I think would be generated
    # by the feature point matching


    cLeft_8UC1 = cv2.cvtColor(cLeft, cv2.COLOR_RGB2GRAY)
    cRight_8UC1 = cv2.cvtColor(cRight, cv2.COLOR_RGB2GRAY)

    stereo = cv2.StereoSGBM_create(numDisparities=80, blockSize=25)
    # np_horizontal = np.hstack((cLeft_8UC1, cRight_8UC1))
    disparity = stereo.compute(cLeft_8UC1, cRight_8UC1)
    disparity = cv2.convertScaleAbs(disparity)
    # disparity = np.array(256 * disparity / 0x0fff,
    #                         dtype=np.uint8)
    
    # height, width = disparity.shape
    # for i in range(height):
    #     for j in range(width):
    #         z = disparity[i][j]
    #         x = (j - CX_DEPTH) * z / FX_DEPTH
    #         y = (i - CY_DEPTH) * z / FY_DEPTH
    #         pcd.append([x, y, z])

        
    #     pcd_o3d = o3d.geometry.PointCloud()  # create point cloud object
    #     pcd_o3d.points = o3d.utility.Vector3dVector(pcd)  # set pcd_np as the point cloud points
    # # Visualize:
    #     o3d.visualization.draw_geometries([pcd_o3d])
    


    # print(len(frameHistory))

    # cv2.imwrite("imL.jpg", cLeft_8UC1)
    # cv2.imwrite("imR.jpg", cRight_8UC1)

    # plt.imshow(depth_instensity)
    cv2.imshow('Image', disparity)
    # plt.show()
    cv2.imshow('Video', cLeft_8UC1)

    if cv2.waitKey(1) == 27:
        break

print(0)