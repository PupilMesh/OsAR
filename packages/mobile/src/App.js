/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useCallback, useEffect, useRef } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Scene, StandardMaterial, UniversalCamera, Vector3 } from '@babylonjs/core';
import { StatusBar, View, Text, Platform, NativeEventEmitter, NativeModules, Image } from 'react-native';
import GLRenderer from './components/GLRenderer';
import useStyles from './styles';
import { accelerometer, gyroscope, setUpdateIntervalForType, SensorTypes } from "react-native-sensors";
import { useState } from 'react';
import { LogBox } from 'react-native';
import FastImage from 'react-native-fast-image';

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
  const currentFrame = useRef(0);
  let lastUpdateTime = Date.now();

  useEffect(() => {
    const subscription = cameraEmitter.addListener('cameraFrame', event => {
      const url = 'data:image/jpeg;base64,' + event;
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

  const onCreateEngine = useCallback((engine) => {
    if (!engine) return;

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString(`#000000`);

    const camera = new UniversalCamera('camera', new Vector3(0, 3, -5), scene);
    camera.setTarget(Vector3.Zero());

    const subscription = gyroscope.subscribe(
      ({ x, y, z }) => {
        camera.rotation.set(x * 0.1, y * 0.1, z * 0.1);
      },
      error => {
        console.log("The sensor is not available");
      }
    );

    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);

    const box = MeshBuilder.CreateBox('Cube', { size: 1 });
    const boxMaterial = new StandardMaterial('CubeMaterial', scene);
    boxMaterial.diffuseColor = Color3.FromHexString('#0081fe');
    box.material = boxMaterial;

box.position.set(0, 0, 0);
box.addRotation(0.1, 0.5, 0);

engine.runRenderLoop(function () {
  if (scene && scene.activeCamera) scene.render();
});

return () => {
  scene.dispose();
  camera.dispose();
  engine.dispose();
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
  <GLRenderer onCreateEngine={onCreateEngine} />
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
        />
      )
    ))}

    <Text numberOfLines={0} allowFontScaling={false} style={styles.Overlay_Text}>
      {"Frame : " + frameCount}
    </Text>
    <Text numberOfLines={0} allowFontScaling={false} style={styles.Overlay_Text}>
      {"FPS : " + fps}
    </Text>
  </View>
</View>
);
}
