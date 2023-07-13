import numpy as np
import cv2
import matplotlib.pyplot as plt
import open3d as o3d
import pickle

pcd = []
FX_DEPTH = 464.26828003
FY_DEPTH = 463.49984741
CX_DEPTH = 319.64876017
CY_DEPTH = 234.05504395
baseline = 0.1

imgL = cv2.imread('imL.png', 0)
imgR = cv2.imread('imR.png', 0)

stereo = cv2.StereoSGBM_create(numDisparities=96, blockSize=5, P1=150)
disparity = stereo.compute(imgL, imgR).astype(np.float32) / 16.0

depth_map = np.zeros_like(disparity).astype(np.float32)
depth_map[disparity > 0] = FX_DEPTH * baseline / disparity[disparity > 0]

depth_map_norm = cv2.normalize(depth_map, None, 0, 255, cv2.NORM_MINMAX)

# cv2.reprojectImageTo3D(disparity)

# height, width = disparity.shape
# for i in range(height):
#     for j in range(width):
#         z = disparity[i][j]
#         x = (j - CX_DEPTH) * z / FX_DEPTH
#         y = (i - CY_DEPTH) * z / FY_DEPTH
#         pcd.append([x, y, z])

# print(imgL.shape)
# print(imgR.shape)

# pcd_o3d = o3d.geometry.PointCloud()  # create point cloud object
# pcd_o3d.points = o3d.utility.Vector3dVector(pcd)  # set pcd_np as the point cloud points
# # Visualize:
# o3d.visualization.draw_geometries([pcd_o3d])

plt.imshow(disparity)
plt.show()

