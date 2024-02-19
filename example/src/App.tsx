import * as React from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';

import Wherami from 'react-native-wherami';

export default function App() {
  const [location, setLocation] = React.useState();

  React.useEffect(() => {
    Wherami.checkPermission();
    Wherami.start();
  }, []);

  return (
    <View>
      <Button title="Location" onPress={() => Wherami.location().then(setLocation)} />
    <Text>{location}</Text>
    </View>
  );
}
