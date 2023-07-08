# OSAR
## Open Source AR SDK for Plug and Play Smart Glasses

OSAR is an open source Augmented Reality SDK designed for plug and play compatibility with smart glasses. It utilizes the Babylon Native 3D engine, which runs based on feedback from Camera and IMU through USB.

#### TODO

- [ ] Allow 3D loading of model / marker from local storage
- [ ] Write function to map with camera
- [ ] Load model from JSON in python and in App.js
- [ ] Set model parameters such as scale, position from JSOn


### Getting Started

Follow these instructions to set up and run the OSAR project on your local machine.

#### Prerequisites

1. Make sure you have Node.js and npm installed.
2. Set up your Android device for USB debugging.

#### Installation

1. Clone the repository.
2. Navigate to the `/packages/mobile` directory and run `npm install`.

#### Configuration

1. The Babylon Native 3D engine can be found in `/packages/mobile/src/BabylonFunctions/BabylonScene.js`.
2. Update the VendorID and ProductID of your USB camera in `packages/mobile/android/app/src/main/java/com/cubedemo/MainActivity.java`.
3. Update the JPEG compression quality in `MainActivity.java` based on device capablity.
4. The frame buffer size from `/packages/mobile/src/BabylonFunctions/BabylonScene.js` can also be updated based on need. 
5. Update the python executable path for buildPython property in `\packages\mobile\android\app\build.gradle`  


#### Running the Project

1. Connect your Android device to your computer via USB in File Transfer mode.
2. Enable USB debugging on your Android device.
3. Run `npm run android` from the `/packages/mobile` directory to start the project on your device.
