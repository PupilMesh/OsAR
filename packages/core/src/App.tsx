/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */

import React, {useCallback, useEffect} from 'react';
import {Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Scene, StandardMaterial, UniversalCamera, Vector3} from '@babylonjs/core';
import {StatusBar, View, Text, Platform,NativeEventEmitter,NativeModules} from 'react-native';
import GLRenderer from './components/GLRenderer';
import useStyles from './styles';
import {
  accelerometer,
  gyroscope,
  setUpdateIntervalForType,
  SensorTypes
} from "react-native-sensors";
import { map, filter } from "rxjs/operators";
import {useState} from 'react'
const frameModule = NativeModules.CameraFrame

const cameraEmitter = new NativeEventEmitter(frameModule)
var isImageUpdated = true


export default function App() {

  //code to getting frames
  const [imageUri, setImageUri] = useState(null)

const getImage = () => {
  cameraEmitter.addListener('cameraFrame', event => {
    const url = 'data:image/png;base64,' + event
    let len = event.length;
    console.log(len)
  })
}
if(isImageUpdated){
  getImage()
  isImageUpdated=false;
}
 
  
  setUpdateIntervalForType(SensorTypes.gyroscope, 50); // defaults to 100ms
  useEffect(() => {
    if (Platform.OS == 'web') {
      window.document.body.style.backgroundColor = '#1acaeb';
    }
  }, []);

  const onCreateEngine = useCallback((engine: Engine | undefined) => {
    if (!engine) return;

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString(`#000000`);

    const camera = new UniversalCamera('camera', new Vector3(0, 3, -5), scene);
    camera.setTarget(Vector3.Zero());

    const subscription = gyroscope.subscribe(
      ({ x, y, z }) => {
        //console.log(x,y,z)
      camera.rotation.set(x*0.1, y*0.1, z*0.1);
    },
    error => {
      console.log("The sensor is not available");
    }
  );
    
    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);

    const box = MeshBuilder.CreateBox('Cube', {size: 1});
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
  },  []);

  const styles = useStyles();

  return (
    <View
      style={{
        flex: 1,
        backgroundColor: '#000000',
        position: 'absolute',
        width: '100%',
        height: '100%'
      }}
    >
      <StatusBar backgroundColor={'black'} />
      <GLRenderer onCreateEngine={onCreateEngine} />
      <View style={styles.Overlay_Root}>
        <Text numberOfLines={0} allowFontScaling={false} style={styles.Overlay_Text}>
          ROTATING-CUBE-DEMO-BABYLON-RXN
        </Text>
      </View>
    </View>
  );
}