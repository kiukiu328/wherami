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
npm pack ----pack-destination=/PATH/TO/pathadvisor-ar-navigation/res
```
in pathadvisor-ar-navigation
```sh
npm install ./res/THE_OUTPUT_FILE.tagz
```

## Usage

```js
import Wherami from 'react-native-wherami';

// for check permission and init
Wherami.checkPermission();
//...
Wherami.start();
// return a Promise with string
// Wherami.location();

//May use like this
const [location, setLocation] = React.useState();
Wherami.location().then(setLocation);
```


## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
