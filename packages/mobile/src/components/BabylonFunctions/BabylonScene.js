/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */


import React, { useCallback } from 'react';
import { Behavior, Color3, Color4, Engine, HemisphericLight, Mesh, MeshBuilder, Node, Nullable, Observer, Scene, StandardMaterial, UniversalCamera, Vector3 } from '@babylonjs/core';
import GLRenderer from '../GLRenderer';
import { SceneLoader } from "@babylonjs/core/Loading/sceneLoader";
export default function BabylonScene() {

  const onCreateEngine = useCallback((engine) => {
    if (!engine) return;

    const scene = new Scene(engine);

    scene.clearColor = Color4.FromHexString(`#000000`);

    const camera = new UniversalCamera('camera', new Vector3(0, 3, -5), scene);
    camera.setTarget(Vector3.Zero());

    const light = new HemisphericLight('HemiLight', new Vector3(0, 9, -5), scene);

    // const box = MeshBuilder.CreateBox('Cube', { size: 1 });
    // const boxMaterial = new StandardMaterial('CubeMaterial', scene);
    // boxMaterial.diffuseColor = Color3.FromHexString('#0081fe');
    // box.material = boxMaterial;

    // box.position.set(0, 0, 0);
    // box.addRotation(0.1, 0.5, 0);

  const modelUrl = "https://res.cloudinary.com/doblnhena/image/upload/v1683485043/2CylinderEngine_lfapqb.glb"; // Replace with your glb file path
  SceneLoader.ImportMesh("", modelUrl, "", scene, function (newMeshes) {
    const root = newMeshes[0];
    root.position.set(0, 0, 0);
    root.scaling = new Vector3(0.01, 0.01, 0.01); // Adjust the scaling if needed
  });

    engine.runRenderLoop(function () {
      if (scene && scene.activeCamera) scene.render();
    });

    return () => {
      scene.dispose();
      camera.dispose();
      engine.dispose();
    };
  }, []);

  return (
    <>
      <GLRenderer onCreateEngine={onCreateEngine} />
    </>
)
}