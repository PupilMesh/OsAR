/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */


import React, { useCallback, useState, useEffect } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Quaternion, Scene, StandardMaterial, UniversalCamera, Vector3 } from '@babylonjs/core';
import GLRenderer from '../GLRenderer';
import { SceneLoader } from "@babylonjs/core/Loading/sceneLoader";
import "@babylonjs/loaders/glTF";
import { gyroscope } from 'react-native-sensors';

export default function BabylonScene({modelUrls}) {
  const [rootMesh, setRootMesh] = useState(null);

  useEffect(() => {
    const subscription = gyroscope.subscribe(({ x, y, z }) => {
      if(rootMesh) {
        rootMesh.rotationQuaternion = Quaternion.RotationYawPitchRoll(y, x, z);
      }
    });

    return () => {
      subscription.unsubscribe();
    }
  }, [rootMesh]);

  const onCreateEngine = useCallback((engine) => {
    if (!engine) return;

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString(`#000000`);

    const camera = new UniversalCamera('camera', new Vector3(0, 3, -5), scene);
    camera.setTarget(Vector3.Zero());

    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);

    modelUrls.forEach(modelUrl => {
      SceneLoader.ImportMesh("", modelUrl, "", scene, function (newMeshes) {
        const root = newMeshes[0];
        root.position.set(0, -10, 10);
        root.scaling = new Vector3(0.8, 0.8, 0.8); // Adjust the scaling if needed
        setRootMesh(root);
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
