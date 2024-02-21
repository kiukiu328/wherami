import * as React from 'react';
import { Text, View } from 'react-native';

import Wherami, { WheramiEmitter } from 'react-native-wherami';

export default function App() {
  const [location, setLocation] = React.useState("Waiting for location...");
  const [initStatus, setInitStatus] = React.useState("");

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

  return (
    <View>
      <Text>{location}</Text>
      <Text>{initStatus}</Text>
    </View>
  );
}
