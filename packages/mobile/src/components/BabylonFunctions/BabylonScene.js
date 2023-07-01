/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useCallback, useState, useEffect } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Quaternion, Scene, UniversalCamera, Vector3, PhotoDome, Vector4 } from '@babylonjs/core';
import GLRenderer from '../GLRenderer';
import { SceneLoader } from "@babylonjs/core/Loading/sceneLoader";
import "@babylonjs/loaders/glTF";
import useStyles from '../../styles';
import { Button, View, Text, NativeEventEmitter, NativeModules } from 'react-native';
const Imu  = NativeModules.Imu;
const imuEmitter = new NativeEventEmitter(Imu);

export default function BabylonScene({modelUrls}) {
  const [camera, setCamera] = useState(null);
// TODO Change the IMU from the current init value and not default values
  // Add multipler for position distance, the distance value is in same units as the marker size given in aruco.py code, so add multiplier for it
  useEffect(() => {
  imuEmitter.addListener('Imu', (event) => {
      let array = event.split(",")
    if (camera) {
        const quaternion = new Quaternion(parseFloat(array[0]),parseFloat(array[1]),parseFloat(array[2]),parseFloat(array[3])); // use the values from the rotation vector sensor
        const euler = quaternion.toEulerAngles();   
        euler._x=-euler._x
        euler._y=-euler._y
        euler._z=-euler._z
        camera.rotation = euler;  
      }
    });

    return () => {
    }
  }, [camera]);
  const setCameraToZero= ()=>{
    if (camera) {
      camera.rotation = new Vector3(0, 0, 0);
    }
  }
  const onCreateEngine = useCallback((engine) => {
    if (!engine) return;

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString(`#000000`);

    const camera = new UniversalCamera('camera', new Vector3(0, 3, -5), scene);
    camera.setTarget(Vector3.Zero());
    setCamera(camera);

    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);
    var dome = new PhotoDome(
        "testdome",
        "https://res.cloudinary.com/doblnhena/image/upload/v1688242133/360-panorama-matador-seo_dsfsee.jpg",
        {
            resolution: 32,
            size: 1000
        },
        scene
    );
    console.log("ModelURLs" +modelUrls)
    modelUrls.forEach(model => {
    SceneLoader.ImportMesh("", model.modelUrl, "", scene, function (newMeshes) {
      const root = newMeshes[0];
      root.position.set(model.position[0], model.position[1], model.position[2]); 
      root.scaling = new Vector3(model.scale[0], model.scale[1], model.scale[2]); 
      root.rotationQuaternion = new Quaternion(model.rotation[0], model.rotation[1], model.rotation[2],model.rotation[3]);
      
      // root.rotationQuaternion = Quaternion.FromEulerAngles(model.rotation[0], model.rotation[1], model.rotation[2]);
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
  }, [modelUrls]); 
  const styles = useStyles();
  return (
    <>
      <GLRenderer onCreateEngine={onCreateEngine} />
      <View style={styles.Overlay_Root}>
          <View style={{ marginHorizontal:40,padding: 20 }}>
            <Button title="ReCalibrate" onPress={setCameraToZero} />
          </View>
      </View>
    </>
  )
}   