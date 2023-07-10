import cv2
import numpy as np
import os
import urllib.request
import base64
import json
import io 

matrix_coefficients_url = "https://res.cloudinary.com/doblnhena/raw/upload/v1687004428/calibration_matrix_gknrrd.npy"
distortion_coefficients_url = "https://res.cloudinary.com/doblnhena/raw/upload/v1687004428/distortion_coefficients_eyshch.npy"

# def download_image(url):
#     resp = urllib.request.urlopen(url)
#     image = np.asarray(bytearray(resp.read()), dtype="uint8")
#     image = cv2.imdecode(image, cv2.IMREAD_GRAYSCALE)
#     return image

# image_url="https://res.cloudinary.com/doblnhena/image/upload/v1687517879/screenshot2_evezeg.png"
# default_frame = download_image(image_url)

ARUCO_DICT = {
	"DICT_4X4_50": cv2.aruco.DICT_4X4_50,
	"DICT_4X4_100": cv2.aruco.DICT_4X4_100,
	"DICT_4X4_250": cv2.aruco.DICT_4X4_250,
	"DICT_4X4_1000": cv2.aruco.DICT_4X4_1000,
	"DICT_5X5_50": cv2.aruco.DICT_5X5_50,
	"DICT_5X5_100": cv2.aruco.DICT_5X5_100,
	"DICT_5X5_250": cv2.aruco.DICT_5X5_250,
	"DICT_5X5_1000": cv2.aruco.DICT_5X5_1000,
	"DICT_6X6_50": cv2.aruco.DICT_6X6_50,
	"DICT_6X6_100": cv2.aruco.DICT_6X6_100,
	"DICT_6X6_250": cv2.aruco.DICT_6X6_250,
	"DICT_6X6_1000": cv2.aruco.DICT_6X6_1000,
	"DICT_7X7_50": cv2.aruco.DICT_7X7_50,
	"DICT_7X7_100": cv2.aruco.DICT_7X7_100,
	"DICT_7X7_250": cv2.aruco.DICT_7X7_250,
	"DICT_7X7_1000": cv2.aruco.DICT_7X7_1000,
	"DICT_ARUCO_ORIGINAL": cv2.aruco.DICT_ARUCO_ORIGINAL,
	"DICT_APRILTAG_16h5": cv2.aruco.DICT_APRILTAG_16h5,
	"DICT_APRILTAG_25h9": cv2.aruco.DICT_APRILTAG_25h9,
	"DICT_APRILTAG_36h10": cv2.aruco.DICT_APRILTAG_36h10,
	"DICT_APRILTAG_36h11": cv2.aruco.DICT_APRILTAG_36h11
}

# loop_count = 0 
error =""
try:
    with urllib.request.urlopen(matrix_coefficients_url) as u:
        f = io.BytesIO(u.read())
        matrix_coefficients = np.load(f)

    with urllib.request.urlopen(distortion_coefficients_url) as u:
        f = io.BytesIO(u.read())
        distortion_coefficients = np.load(f)
except urllib.error.URLError as e:
    error = f"URL Error: {e}"
except Exception as e:
    error= f"Error occurred: {e}"

def rotation_matrix_to_quaternion(R):
    q0 = np.sqrt(1 + R[0, 0] + R[1, 1] + R[2, 2]) / 2
    q1 = (R[2, 1] - R[1, 2]) / (4 * q0)
    q2 = (R[0, 2] - R[2, 0]) / (4 * q0)
    q3 = (R[1, 0] - R[0, 1]) / (4 * q0)
    return [float(q) for q in [q1, q2, q3, q0]]  

def detect_and_estimate_marker_pose(frame, aruco_dict_type, matrix_coefficients, distortion_coefficients):
    # global loop_count
    # loop_count = 0 
    gray = frame
    if len(frame.shape) > 2 and frame.shape[2] > 1:
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    aruco_dict = cv2.aruco.Dictionary_get(aruco_dict_type)
    parameters = cv2.aruco.DetectorParameters_create()
    corners, ids, _ = cv2.aruco.detectMarkers(gray, aruco_dict, parameters=parameters)
    marker_info = []  
    if len(corners) > 0:
        # loop_count+=1
        for i in range(0, len(ids)):
            cv2.aruco.drawDetectedMarkers(frame, corners)
            # cv2.drawFrameAxes(frame, matrix_coefficients, distortion_coefficients, rvec, tvec, 0.01)
            rvec, tvec, _ = cv2.aruco.estimatePoseSingleMarkers(corners[i], 0.02, matrix_coefficients, distortion_coefficients)


            angle = cv2.norm(rvec)

            rotation_matrix, _ = cv2.Rodrigues(rvec)

            quaternion = rotation_matrix_to_quaternion(rotation_matrix)


            marker_info.append({
                "marker_id": int(ids[i][0]),  # converting numpy int32 to python int
                "distance": tvec[0][0].tolist(),  # assuming tvec[0][0] is numpy array
                "rotation_angle": float(angle),  # converting numpy float to python float
                "quaternion": [float(q) for q in quaternion],  # converting numpy floats to python floats
            })


    return frame, marker_info

def detect_marker(image_bytes, aruco_type):
    try:
        global matrix_coefficients
        global distortion_coefficients
        # global loop_count
        global default_frame
        global error
        aruco_dict_type = ARUCO_DICT[aruco_type]

        image_np = np.frombuffer(image_bytes, dtype=np.uint8)

        frame = cv2.imdecode(image_np, flags=cv2.IMREAD_COLOR)
        output, marker_info = detect_and_estimate_marker_pose(frame, aruco_dict_type, matrix_coefficients, distortion_coefficients)  # Pass color image instead of grayscale
        marker_ids = []
        distances = []
        rotations = []
        quaternions = []
        
        for info in marker_info:
            marker_ids.append(info["marker_id"])
            distances.append(info["distance"])
            rotations.append(info["rotation_angle"])
            quaternions.append(info["quaternion"])

        retval, buffer = cv2.imencode('.jpeg', output)
        img_base64 = base64.b64encode(buffer).decode('utf-8')
        result = {
                "marker_ids": marker_ids,
                "distances": distances,
                # "rotations": rotations,
                "quaternions": quaternions,
                # "loop":loop_count,
                "error":error,
                "image": img_base64,
                }
        result_json_string = json.dumps(result)
        return result_json_string
    except Exception as e:
        result = {
                "marker_ids": str(e),
                "image": str(e),
                }
        result_json_string = json.dumps(result)
        return result_json_string
