/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useCallback, useState, useEffect } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Quaternion, Scene, StandardMaterial,FreeCamera, UniversalCamera, Vector3, PhotoDome, Vector4, Tools } from '@babylonjs/core';
import GLRenderer from '../GLRenderer';
import { SceneLoader } from "@babylonjs/core/Loading/sceneLoader";
import "@babylonjs/loaders/glTF";
import { gyroscope } from 'react-native-sensors';
import { StatusBar, View, Text, Platform, NativeEventEmitter, NativeModules, Image } from 'react-native';
const Imu  = NativeModules.Imu;
const imuEmitter = new NativeEventEmitter(Imu);

export default function BabylonScene({modelUrls}) {
  const [camera, setCamera] = useState(null);

  useEffect(() => {
    // const subscription = gyroscope.subscribe(({ x, y, z }) => {
    //   if(camera) {
    //     camera.rotation = new Vector3(x, y, z);
    //   }
    // });
let change;
let startTime = 0;

imuEmitter.addListener('Imu', (event) => {
    let array = event.split(",");
    if (camera) {
       
      let imuQuaternion = new Quaternion(parseFloat(array[1]), parseFloat(array[2]), parseFloat(array[3]), parseFloat(array[0])); // Please note the order has been changed
        camera.rotationQuaternion = imuQuaternion;

        // Apply the inverse of the IMU rotation to the camera's rotation
        // This will rotate the camera to point in the direction that the device is pointing
      //   let invertedImuQuaternion = imuQuaternion.conjugate();
      // camera.rotationQuaternion = invertedImuQuaternion;
      
      // const quaternion = new Quaternion(parseFloat(array[0]), parseFloat(array[1]), parseFloat(array[2]), parseFloat(array[3]));
        // const euler = quaternion.toEulerAngles();    
        //  camera.rotation.set(euler.x,euler.y,euler.z);  
      //  const quat90 = new Quaternion.RotationYawPitchRoll(0, 0, -Math.PI / 2); // -90 degrees in radians
      // let quat = quat90.multiply(quaternion);
      //   quat.set(quat.y, quat.x, -quat.z, -quat.w);

        // let euler = quat.toEulerAngles();
        // let temp = euler.z;
        // euler.z = euler.x;
        // euler.x = temp;
        //  camera.rotation.set(-euler.x,-euler.y,-euler.z);  

      //   quat = new Quaternion.RotationYawPitchRoll(-euler.x, -euler.y, -euler.z);

      //   // change = new Tools.Now - startTime; // Time in milliseconds since startTime
      //   let smoothing = 1.0;
      //   camera.rotationQuaternion = quat
    }
});


    return () => {
      // subscription.unsubscribe();
    }
  }, [camera]);

  const onCreateEngine = useCallback((engine) => {
    if (!engine) return;

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString(`#000000`);

    const camera = new FreeCamera('camera', new Vector3(0, 3, -5), scene);
    camera.setTarget(Vector3.Zero());
    setCamera(camera);

    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);
    var dome = new PhotoDome(
        "testdome",
        "https://res.cloudinary.com/doblnhena/image/upload/v1684415267/360photo_mjy98u.jpg",
        {
            resolution: 32,
            size: 1000
        },
        scene
    );
    modelUrls.forEach(modelUrl => {
      SceneLoader.ImportMesh("", modelUrl, "", scene, function (newMeshes) {
        const root = newMeshes[0];
        root.position.set(0, -10, 10);
        root.scaling = new Vector3(0.8, 0.8, 0.8); // Adjust the scaling if needed
      });
    });

    engine.runRenderLoop(function () {
      if (scene && scene.activeCamera) scene.render();
    });

    return () => {
      scene.dispose();
      camera.dispose();
      engine.dispose();
    };
  }, [modelUrls]); // modelUrls is a dependency now

  return (
    <>
      <GLRenderer onCreateEngine={onCreateEngine} />
    </>
  )
}




