# OSAR
## Open Source AR SDK for Plug and Play Smart Glasses

OSAR is an open source Augmented Reality SDK designed for plug and play compatibility with smart glasses. It utilizes the Babylon Native 3D engine, which runs based on feedback from Camera and IMU through USB.

#### Why cloud 
- The network performance is better than local compute for intensive detection and CV algos
- The cloud can be easily extened to any platform (Windows, Android, iOS)


#### Implementation Proposal

- To start with, an React-Native x Java Android app, that reads the USB sensor values and sends to a server endpoint (IMU, Camera Frame, Device-ID)  [here the RN bridge is replaced] 
- Initial implemntation can be a Python Flask monolith, that works similar to the Java code in babylon-application branch, it sends extracted information from the sensor values  
- A websocket or webRTC connection is maintained on the Rn part to recieved the polished IMU values and model events like, new model added to set for the BabylonJS scene
- Here, the code from RN is reused and th sync issue from Java is avoided
