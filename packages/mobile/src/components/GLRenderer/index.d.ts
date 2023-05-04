import { Engine } from '@babylonjs/core/Engines/engine';

export interface IProps {
  onCreateEngine: (engine?: Engine) => void;
}

declare const GLRenderer: (props: IProps) => JSX.Element;
export default GLRenderer;
