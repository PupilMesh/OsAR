import React, {memo, useCallback, useEffect, useRef, useState} from 'react';
import {IProps} from '.';
import {useWindowDimensions} from 'react-native';

import {Engine} from '@babylonjs/core';
//import {Logger} from '@babylonjs/core';

const GLRenderer = ({onCreateEngine}: IProps) => {
  //Logger.LogLevels = Logger.NoneLogLevel;

  const engineRef = useRef<Engine>();
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const {width, height} = useWindowDimensions();

  const [lastPixelRatio, setNewPixelRatio] = useState(window.devicePixelRatio);

  const handleResize = useCallback(() => {
    if (engineRef.current) {
      (canvasRef.current as HTMLCanvasElement).width = width;
      (canvasRef.current as HTMLCanvasElement).height = height;
      (canvasRef.current as HTMLCanvasElement).style.width = '100%';
      (canvasRef.current as HTMLCanvasElement).style.height = '100%';
      (canvasRef.current as HTMLCanvasElement).style.outline = 'none';

      engineRef.current.setSize(width, height);
      engineRef.current.resize();

      if (lastPixelRatio != window.devicePixelRatio) setNewPixelRatio(window.devicePixelRatio);
    }
  }, [engineRef, width, height]);

  useEffect(() => {
    handleResize();
  }, [handleResize]);

  useEffect(() => {
    if (canvasRef.current) {
      engineRef.current = new Engine(canvasRef.current, true, {
        antialias: true,
        adaptToDeviceRatio: true,
        powerPreference: 'high-performance'
      });

      handleResize();

      onCreateEngine(engineRef.current);
    }

    return () => {
      engineRef.current?.dispose();
    };
  }, [canvasRef, onCreateEngine, devicePixelRatio]);

  return <canvas ref={canvasRef} />;
};

export default memo(GLRenderer);
