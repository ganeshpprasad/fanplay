/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */


import React, {useState, useEffect} from 'react';
import type {Node} from 'react';

import {
  NativeEventEmitter,
  SafeAreaView,
  StatusBar,
  Button,
  NativeModules,
  Image,
  Text,
  StyleSheet,
  View,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import band from './band.jpg';

import {DeviceEventEmitter} from 'react-native';

const {CalendarModule} = NativeModules;

const App = () => {
  const [heartRate, setheartRate] = useState('Press Start HR');
  const [distance, setdistance] = useState('NA');
  const [calorie, setcalorie] = useState('NA');
  const [steps, setsteps] = useState('NA');
  const [scannedDevices, setIsScanning] = useState([]);
  const [initDone, setinitDone] = useState(false);
  const [isConnected, setConnected] = useState(false);
  let eventEmitter;

  useEffect(() => {
    setinitDone(true);
    eventEmitter = new NativeEventEmitter(NativeModules.CalendarModule);
    eventEmitter.addListener('devicesScanEvent', devicesScanEvent);
    CalendarModule.init();
  }, []);

  // useEffect(() => {
  //   CalendarModule.getDevices();
  // }, []);

  // useEffect(() => {
  //   eventEmitter.addListener('devicesScanEvent', devicesScanEvent);
  // }, []); // Runs only once at the beginning and when heartRate change

  const devicesScanEvent = (event) => {
    console.log(event.address); // "someValue"
    let devices = scannedDevices.map((i) => i);
    devices.push(event.address);
    setIsScanning(devices);
  };

  const onScan = () => {
    console.log('on scan');
    // eventEmitter.addListener('devicesScanEvent', devicesScanEvent);
    CalendarModule.scanDevices();
  };

  // const onPress = () => {
  //   console.log('on Press');
  //   CalendarModule.startConnect();
  // };

  const onHr = () => {
    console.log('on hr');
    const hR = CalendarModule.startHeartRate((hr, step, cal, dist) => {
      console.log(hr);
      console.log(step);
      console.log(cal);
      console.log(dist);
      setheartRate(hr.hr);
      setsteps(step.step);
      setcalorie(cal.cal);
      setdistance(dist.dist);
    });
  };

  const startConnect = (device) => {
    CalendarModule.startConnectToDevice(device, (flag) => {
        setConnected(flag);
    });
    console.log('init ' , initDone);
    // console.log('isconnected ' , CalendarModule.isConnected());
  };

  const renderScannedDevices = () => {
    if (scannedDevices.length < 1) {
      return <Text>Please scan devices</Text>;
    }
    return scannedDevices.map((device, index) => (
      <Text onPress={() => startConnect(scannedDevices[index])}>{device}</Text>
    ));
  };

  const onCameraPress = () => {
    console.log('We will invoke the native module here!');
  };

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
        <Button title="Scan" onPress={onScan} />
        <Button title="Click to The Camera View" onPress={onCameraPress} />
        {scannedDevices.length >= 1 && (
          <View>
            <Text>Connect to device</Text>
            {renderScannedDevices()}
            {/* <Button title="Connect" onPress={onPress} /> */}
          </View>
        )}
        {initDone && isConnected && (
          <>
            <Text>Daily Stats</Text>
            <Text style={style.textStyle}>Steps: {steps}</Text>
            <Text style={style.textStyle}>Calories: {calorie}</Text>
            <Text style={style.textStyle}>Distance : {distance}</Text>
            <Button title="Count heart rate" onPress={onHr} />
            <Text style={style.textStyle}>Heart Rate : {heartRate}</Text>
          </>
        )}
      </SafeAreaView>
    </>
  );
};

const style = StyleSheet.create({
  textStyle: {
    padding: 10,
    fontSize: 20,
  },
});

export default App;


// const {CalendarModule} = NativeModules;

// const Section = ({children, title}): Node => {
//   const isDarkMode = useColorScheme() === 'dark';
//   return (
//     <View style={styles.sectionContainer}>
//       <Text
//         style={[
//           styles.sectionTitle,
//           {
//             color: isDarkMode ? Colors.white : Colors.black,
//           },
//         ]}>
//         {title}
//       </Text>
//       <Text
//         style={[
//           styles.sectionDescription,
//           {
//             color: isDarkMode ? Colors.light : Colors.dark,
//           },
//         ]}>
//         {children}
//       </Text>
//     </View>
//   );
// };

// const App: () => Node = () => {
//   const isDarkMode = useColorScheme() === 'dark';

//   const backgroundStyle = {
//     backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
//   };

//   return (
//     <SafeAreaView style={backgroundStyle}>
//       <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
//       <ScrollView
//         contentInsetAdjustmentBehavior="automatic"
//         style={backgroundStyle}>
//         <Header />
//         <View
//           style={{
//             backgroundColor: isDarkMode ? Colors.black : Colors.white,
//           }}>
//           <Section title="Step One">
//             Edit <Text style={styles.highlight}>App.js</Text> to change this
//             screen and then come back to see your edits.
//           </Section>
//           <Section title="See Your Changes">
//             <ReloadInstructions />
//           </Section>
//           <Section title="Debug">
//             <DebugInstructions />
//           </Section>
//           <Section title="Learn More">
//             Read the docs to discover what to do next:
//           </Section>
//           <LearnMoreLinks />
//         </View>
//       </ScrollView>
//     </SafeAreaView>
//   );
// };

// const styles = StyleSheet.create({
//   sectionContainer: {
//     marginTop: 32,
//     paddingHorizontal: 24,
//   },
//   sectionTitle: {
//     fontSize: 24,
//     fontWeight: '600',
//   },
//   sectionDescription: {
//     marginTop: 8,
//     fontSize: 18,
//     fontWeight: '400',
//   },
//   highlight: {
//     fontWeight: '700',
//   },
// });

// export default App;
