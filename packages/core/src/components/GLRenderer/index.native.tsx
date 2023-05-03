import React, {memo, useCallback, useEffect} from 'react';
import {EngineView, useEngine} from '@babylonjs/react-native';
import {IProps} from '.';
import {useWindowDimensions} from 'react-native';
//import {Logger} from '@babylonjs/core';

const GLRenderer = ({onCreateEngine}: IProps) => {
  //Logger.LogLevels = Logger.NoneLogLevel;

  const engine = useEngine();
  const {width, height} = useWindowDimensions();

  const handleResize = useCallback(() => {
    if (engine) {
      engine.setSize(width, height);
      engine.resize();
    }
  }, [engine, width, height]);

  useEffect(() => {
    handleResize();
  }, [handleResize]);

  useEffect(() => {
    if (engine) {
      engine.doNotHandleContextLost = true;

      onCreateEngine(engine);
      handleResize();
    }
  }, [engine, onCreateEngine]);

  return <EngineView />;
};

export default memo(GLRenderer);
