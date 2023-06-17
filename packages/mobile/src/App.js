/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useCallback, useEffect, useRef } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Scene, StandardMaterial, UniversalCamera, Vector3 } from '@babylonjs/core';
import { StatusBar, View, Text, Platform, NativeEventEmitter, NativeModules, Image } from 'react-native';
import useStyles from './styles';
import { accelerometer, gyroscope, setUpdateIntervalForType, SensorTypes } from "react-native-sensors";
import { useState } from 'react';
import { LogBox } from 'react-native';
import FastImage from 'react-native-fast-image';
import BabylonScene from './components/BabylonFunctions/BabylonScene';



LogBox.ignoreLogs([
  'Require cycle:',
]);

const frameModule = NativeModules.CameraFrame;
const cameraEmitter = new NativeEventEmitter(frameModule);

const BUFFER_SIZE = 10;

export default function App() {
  const [imageUris, setImageUris] = useState(Array(BUFFER_SIZE).fill(""));
  const [visibleFrames, setVisibleFrames] = useState(Array(BUFFER_SIZE).fill(false));
  const [frameCount, setFrameCount] = useState(0);
  const [fps, setFps] = useState(0);
  const [debug, setDebug] = useState("0");
  const [modelUrls, setModelUrls] = useState([]);

  
  useEffect(() => {
  setModelUrls([{
    modelUrl: "https://res.cloudinary.com/doblnhena/image/upload/v1683895843/model1_yprz3d.glb",
    scale: [0.8, 0.8, 0.8],
    position: [0, -10, 10],
    rotation: [0, 0, 0] // replace with your desired rotation

  }]);
}, []); // Put your dependencies here

  const currentFrame = useRef(0);
  let lastUpdateTime = Date.now();

  useEffect(() => {
    const subscription = cameraEmitter.addListener('cameraFrame', event => {
      event = JSON.parse(event)
      let { marker_info, image } = event;
      marker_info = JSON.parse(marker_info)
      let detected_marker=marker_info['marker_id']
      // setDebug(detected_marker)
      // if (detected_marker == "image1") {
      //       setModelUrls(["https://res.cloudinary.com/doblnhena/image/upload/v1683895843/model1_yprz3d.glb",...modelUrls]);
      // } else if (detected_marker == "image2") {
      //       setModelUrls(["https://res.cloudinary.com/doblnhena/image/upload/v1683895925/model3_ufinmb.glb",...modelUrls]);
      // }
      if (detected_marker == "43") {
        const newModel = {
          modelUrl: "https://res.cloudinary.com/doblnhena/image/upload/v1683895843/model1_yprz3d.glb",
          scale: [0.8, 0.8, 0.8],
          position: [0, -10, 10],
          rotation: [0, 0, 0] // replace with your desired rotation

        };
        setModelUrls(prevModels => {
          if (!prevModels.find(model => model.modelUrl === newModel.modelUrl)) {
            const newModels = [newModel,...prevModels];
            if (newModels.length > 5) newModels.pop(); // keep only the last 5 models
            return newModels;
          }
          return prevModels; // return the previous state if the model is already in the array
        });
      } else if (detected_marker == "42") {
        const newModel = {
          modelUrl: "https://res.cloudinary.com/doblnhena/image/upload/v1683895925/model3_ufinmb.glb",
          scale: [0.8, 0.8, 0.8],
          position: [0, -10, 10],
          rotation: [0, 0, 0] // replace with your desired rotation

        };
        setModelUrls(prevModels => {
          if (!prevModels.find(model => model.modelUrl === newModel.modelUrl)) {
            const newModels = [newModel,...prevModels];
            if (newModels.length > 5) newModels.pop(); // keep only the last 5 models
            return newModels;
          }
          return prevModels; // return the previous state if the model is already in the array
        });
      }


      const url = 'data:image/jpeg;base64,' + image;
      setImageUris(prevUris => {
        const updatedUris = [...prevUris];
        updatedUris[currentFrame.current] = url;
        return updatedUris;
      });
      setVisibleFrames(prevVisible => {
        const updatedVisible = [...prevVisible];
        updatedVisible[currentFrame.current] = true;
        return updatedVisible;
      });

      setFrameCount(prevFrameCount => prevFrameCount + 1);

      const currentTime = Date.now();
      const elapsedTime = (currentTime - lastUpdateTime) / 1000;
      lastUpdateTime = currentTime;
      const currentFps = Math.floor(1 / elapsedTime);
      setFps(currentFps);

      currentFrame.current = (currentFrame.current + 1) % BUFFER_SIZE;
    });

 

    setUpdateIntervalForType(SensorTypes.gyroscope, 50);
      const intervalId = setInterval(() => {
          // Clear disk cache every 10 seconds
          FastImage.clearDiskCache();

          // Clear memory cache every 10 seconds
          FastImage.clearMemoryCache();
        }, 10000);

 
    return () => {
      subscription.remove();
      clearInterval(intervalId);
    };
  }, []);


const styles = useStyles();

return (
<View
  style={{
    flex: 1,
    backgroundColor: '#000000',
    position: 'absolute',
    width: '100%',
    height: '100%',
  }}
>
  <StatusBar backgroundColor={'black'} />
  <BabylonScene modelUrls={modelUrls}/>
    <View style={styles.Overlay_Root}>
    {imageUris.map((uri, index) => (
      visibleFrames[index] && (
        <FastImage
          key={index}
          source={{
            uri,
            priority: FastImage.priority.high,
          }}
          style={styles.image}
          resizeMode="cover" // This prop to ensure the image scales properly
        />
      )
    ))}

    <Text numberOfLines={0} allowFontScaling={false} style={styles.Overlay_Text}>
      {"Frame : " + frameCount}
    </Text>
    <Text numberOfLines={0} allowFontScaling={false} style={styles.Overlay_Text}>
      {"FPS : " + fps}
    </Text>
    <Text numberOfLines={0} allowFontScaling={false} style={styles.Overlay_Text}>
      {debug}
    </Text>
  </View>
</View>
);
}