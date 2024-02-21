# react-native-wherami

By FYP2324

## Run test with only wherami module
clone this project then 
```sh
yarn install

yarn example android; or
cd example
npm run start
```

## Installation/Export module 
in this project
```sh
npm pack --pack-destination=/PATH/TO/pathadvisor-ar-navigation/res
```
in pathadvisor-ar-navigation
```sh
npm install ./res/THE_OUTPUT_FILE.tgz
```

## Usage
Check the "example/App.tsx"

```js
import Wherami, { WheramiEmitter } from 'react-native-wherami';
//...
React.useEffect(() => {
    Wherami.checkPermission();
    WheramiEmitter.addListener('onLocationUpdated', (event) => {
      setLocation(JSON.stringify(event));
    });
    WheramiEmitter.addListener('onInitStatusUpdated', (event) => {
      setInitStatus(JSON.stringify(event));
      if (event.isInitialized)
        Wherami.start();
    });
  }, []);
```


## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
