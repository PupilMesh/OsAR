/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-unused-vars */
import { accelerometer, gyroscope, setUpdateIntervalForType, SensorTypes } from "react-native-sensors";

import React, { useState, useEffect } from 'react';

const IMU = ({camera}) => {
  const [accelerometerData, setAccelerometerData] = useState({
    x: 0,
    y: 0,
    z: 0
  });

  useEffect(() => {
    const subscription = accelerometer.subscribe(({ x, y, z }) =>
      setAccelerometerData({ x, y, z })
    );

    return () => {
      subscription.unsubscribe();
    };
  }, []);

  useEffect(() => {
    if (camera) {  // make sure the camera object is available
      const { x, y, z } = accelerometerData;
      camera.rotation.x = x;
      camera.rotation.y = y;
      camera.rotation.z = z;
    }
  }, [accelerometerData, camera]);  // if accelerometerData or camera changes, update the camera rotation

  return (
    <></>
  );
};

export default IMU;
