import cv2
import numpy as np

RUN_COUNT = 0
KEYFRAME_CAPTURE_COUNT = 5 # capture keyframes every x frames
frameHistory = []

vid = cv2.VideoCapture(0)

imDir = './Images/'

while True:
    RUN_COUNT += 1
    ret, frame = vid.read()

    if (RUN_COUNT % KEYFRAME_CAPTURE_COUNT == 0):
        frameHistory.append(frame)  # capture every 5 iterations
    
    


