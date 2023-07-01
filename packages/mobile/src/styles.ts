import { StyleSheet, Platform, Dimensions } from 'react-native';
import useUnits from 'rxn-units';

const useStyles = () => {
  const { vmin } = useUnits();
const screenWidth = Dimensions.get('window').width;
const screenHeight = Dimensions.get('window').height;

  return StyleSheet.create({
    Parent : {
    transform: [{ rotateZ: '90deg'}], // Add this line to rotate view

    },
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
      position: 'absolute',
      top: 0,
      left: 0,
      width: screenWidth,
      height: screenHeight,
      opacity: 0.05,
    },
  });
};


export default useStyles;
