import {Engine} from '@babylonjs/core/Engines/engine';

/* eslint-disable @typescript-eslint/no-unused-vars */
const GLRenderer: ({onCreateEngine}: IProps) => JSX.Element;

export interface IProps {
  onCreateEngine: (onCreateEngine?: Engine) => void;
}

export default GLRenderer;
