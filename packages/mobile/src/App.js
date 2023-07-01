/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useCallback, useEffect, useRef } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Scene, StandardMaterial, UniversalCamera, Vector3 } from '@babylonjs/core';
import { StatusBar, View, Text, Platform, NativeEventEmitter, NativeModules, Image } from 'react-native';
import useStyles from './styles';
import { useState } from 'react';
import { LogBox } from 'react-native';
import FastImage from 'react-native-fast-image';
import BabylonScene from './components/BabylonFunctions/BabylonScene';

import MarkerToModel from './MarkerMapping'


LogBox.ignoreLogs([
  'Require cycle:',
]);

const frameModule = NativeModules.CameraFrame;
const cameraEmitter = new NativeEventEmitter(frameModule);

const BUFFER_SIZE = 10;
StatusBar.setHidden(true);

export default function App() {

  const [imageUris, setImageUris] = useState(Array(BUFFER_SIZE).fill(""));
  const [visibleFrames, setVisibleFrames] = useState(Array(BUFFER_SIZE).fill(false));
  const [frameCount, setFrameCount] = useState(0);
  const [fps, setFps] = useState(0);
  const [debug, setDebug] = useState("0");
  const [modelUrls, setModelUrls] = useState([]);
  const type = "MARKERBASED"
  
  useEffect(() => {
    if (type == "MARKERLESS"){
      for (let i in MarkerToModel) {
        let newModel = MarkerToModel[i]
        setModelUrls(prevModels => {
          const newModels = [newModel, ...prevModels];
          if (newModels.length > 5) newModels.pop(); // keep only the last 5 models
          return newModels;
        });
      }
    }
  },[])

  const currentFrame = useRef(0);
  let lastUpdateTime = Date.now();

  useEffect(() => {
    const subscription = cameraEmitter.addListener('cameraFrame', event => {

      event = JSON.parse(event)

      for (let i = 0; i < event["marker_ids"].length; i++){

        let id = event["marker_ids"][i];
        let distance = event["distances"][i];
        let rotation = event["quaternions"][i];
        const newModel = MarkerToModel[id];
        if (newModel) {
          setDebug("Model Added")
          newModel.position = distance
          newModel.rotation = rotation
          setDebug("Model Added "+newModel.id+" "+newModel.position)
          setModelUrls(prevModels => {
            if (!prevModels.find(model => model.modelUrl === newModel.modelUrl)) {
              const newModels = [newModel, ...prevModels];
              if (newModels.length > 5) newModels.pop(); // keep only the last 5 models
              return newModels;
            }
            return prevModels; // return the previous state if the model is already in the array
          });
        }
      }
      

      const url = 'data:image/jpeg;base64,' + event["image"];
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

 

      const intervalId = setInterval(() => {
          FastImage.clearDiskCache();
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