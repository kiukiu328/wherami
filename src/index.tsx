import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-wherami' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Wherami = NativeModules.Wherami
  ? NativeModules.Wherami
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );
const WheramiEmitter = new NativeEventEmitter(Wherami);

// wheramiEmitter.addListener('onLocationUpdated', (event) => {
//   console.log(event);
// });
// wheramiEmitter.addListener('onInitStatusUpdated', (event) => {
//   console.log(event);
// });
export { WheramiEmitter };
export default Wherami
