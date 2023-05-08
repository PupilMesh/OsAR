/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */


import React, { useCallback,useState,useEffect } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Scene, StandardMaterial, UniversalCamera, Vector3 } from '@babylonjs/core';
import GLRenderer from '../GLRenderer';
import { SceneLoader } from "@babylonjs/core/Loading/sceneLoader";

export default function BabylonScene() {
  const [engine, setEngine] = useState(null);

  const loadModel = (url, position) => {
    if (!engine) return;

    SceneLoader.ImportMesh('', url, '', engine.scenes[0], function (newMeshes) {
      const root = newMeshes[0];
      root.position.set(position.x, position.y, position.z);
      root.scaling = new Vector3(0.01, 0.01, 0.01); // Adjust the scaling if needed
    });
  };

  const onCreateEngine = (engine) => {
    if (!engine) return;

    setEngine(engine);

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString('#000000');

    const camera = new UniversalCamera('camera', new Vector3(0, 1.6, -5), scene);
    camera.setTarget(Vector3.Zero());
    camera.attachControl(engine.getRenderingCanvas(), true);

    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);

    engine.runRenderLoop(function () {
      if (scene && scene.activeCamera) scene.render();
    });

    return () => {
      scene.dispose();
      camera.dispose();
      engine.dispose();
    };
  };

  // Load initial model
  useEffect(() => {
    if (engine) {
      const modelUrl = 'https://res.cloudinary.com/doblnhena/image/upload/v1683485043/2CylinderEngine_lfapqb.glb';
      loadModel(modelUrl, new Vector3(0, 0, 0));
    }
  }, [engine]);

  // Example of how to add a new model to the scene dynamically after 5 seconds
  useEffect(() => {
    const timer = setTimeout(() => {
      if (engine) {
        const modelUrl = 'https://res.cloudinary.com/doblnhena/image/upload/v1683485043/2CylinderEngine_lfapqb.glb';
        loadModel(modelUrl, new Vector3(1, 0, 0));
      }
    }, 5000);

    return () => clearTimeout(timer);
  }, [engine]);

  return (
    <>
      <GLRenderer onCreateEngine={onCreateEngine} />
    </>
  );
}
