import {StyleSheet, Platform} from 'react-native';
import useUnits from 'rxn-units';

const useStyles = () => {
  const { vmin } = useUnits();

  return StyleSheet.create({
    Overlay_Root: {
      position: 'absolute',
      top: 0,
      left: 0,
      flexDirection: 'column',
    },
    Overlay_Text: {
      color: 'white',
      fontSize: vmin(1.75),
      fontWeight: '600',
      textShadowColor: 'rgba(0, 0, 0, 0.45)',
      textShadowOffset: { width: vmin(0.25), height: vmin(0.2) },
      textShadowRadius: vmin(0.01),
      padding: 50,
      paddingVertical: 5,
    },
    image: {
      position:'absolute',
      width: 200,
      height: 200,
    },
  });
};


export default useStyles;
