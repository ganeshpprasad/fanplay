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

const {DevicesModule} = NativeModules;
const {PulseRateModule} = NativeModules;
const {HealthKitModule} = NativeModules;

const App = () => {
  const [heartRate, setheartRate] = useState('Press Start HR');
  const [distance, setdistance] = useState('NA');
  const [calorie, setcalorie] = useState('NA');
  const [steps, setsteps] = useState('NA');
  const [scannedDevices, setIsScanning] = useState([]);
  const [initDone, setinitDone] = useState(false);
  const [isConnected, setConnected] = useState(false);
  let devicesScanEvent;
  let devicesHeartRateScanEvent;
  let healthKitHeartRateEvent;
  let cameraHeartRateEvent;
  let deviceStepsCalorieDistanceDiscover;

  useEffect(() => {
    setinitDone(true);
    devicesScanEvent = new NativeEventEmitter(NativeModules.DevicesModule);
    deviceStepsCalorieDistanceDiscover = new NativeEventEmitter(
      NativeModules.DevicesModule,
    );
    devicesHeartRateScanEvent = new NativeEventEmitter(
      NativeModules.DevicesModule,
    );
    healthKitHeartRateEvent = new NativeEventEmitter(
      NativeModules.HealthKitModule,
    );
    cameraHeartRateEvent = new NativeEventEmitter(
      NativeModules.PulseRateModule,
    );

    //device scan
    devicesScanEvent.addListener('devicesScanEvent', deviceScan);

    //device heart rate scan
    devicesHeartRateScanEvent.addListener(
      'devicesHeartRateScanEvent',
      devicesHeartRateScan,
    );
    //camera based heart rate
    cameraHeartRateEvent.addListener('cameraHeartRateEvent', camHeartRateEvent);
    //health kit
    healthKitHeartRateEvent.addListener(
      'healthKitHeartRateEvent',
      healthKitheartRateEvent,
    );

    deviceStepsCalorieDistanceDiscover.addListener(
      'deviceStepsCalorieDistanceDiscover',
      deviceStepsCalorieDistance,
    );

    DevicesModule.init();
    PulseRateModule.init();
    HealthKitModule.init();
  }, []);

  // useEffect(() => {
  //   CalendarModule.getDevices();
  // }, []);

  // useEffect(() => {
  //   eventEmitter.addListener('devicesScanEvent', devicesScanEvent);
  // }, []); // Runs only once at the beginning and when heartRate change

  const deviceStepsCalorieDistance = event => {
    console.log(event);
  };

  const devicesHeartRateScan = event => {
    console.log(event);
    console.log(event.hr);
    console.log(event.step);
    console.log(event.cal);
    console.log(event.dist);
    setheartRate(event.hr);
    setsteps(event.step);
    setcalorie(event.cal);
    setdistance(event.dist);
  };

  const deviceScan = event => {
    console.log(event.address); // "someValue"
    let devices = scannedDevices.map(i => i);
    devices.push(event.address);
    setIsScanning(devices);
  };

  const healthKitheartRateEvent = event => {
    console.log(event);
    console.log(event.heartRate);
    console.log(event.status);
    console.log(event.error);
  };

  const camHeartRateEvent = event => {
    console.log(event);
    console.log(event.heartRate);
    console.log(event.status);
    console.log(event.error);
  };

  const onScan = () => {
    console.log('on scan');
    DeviceModule.scan();
  };

  // const onPress = () => {
  //   console.log('on Press');
  //   CalendarModule.startConnect();
  // };

  const onHr = () => {
    console.log('on hr');
    DevicesModule.startHeartRate();
  };

  const onSteps = () => {
    console.log('onSteps');
    DevicesModule.startSteps();
  };

  const onCalorie = () => {
    console.log('onCalorie');
    DevicesModule.startSteps();
  };

  const onDistance = () => {
    console.log('onDistance');
    DevicesModule.startSteps();
  };

  const startConnect = device => {
    DevicesModule.connect(device, flag => {
      setConnected(flag);
    });
    console.log('init ', initDone);
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
    PulseRateModule.getPulseRate();
  };

  const onHealthKitPress = () => {
    console.log('We will invoke the native module here!');
    HealthKitModule.isHealthAvailable(flag => {
      console.log('Health Kit Available ');
      console.log(flag);
      if (flag.flag) {
        HealthKitModule.requestAuthorization((status, error) => {
          console.log('Health Kit Authorisation ');
          console.log(status);
          console.log(error);
          if (status.status) {
            console.log('Getting Hear Rate ');
            HealthKitModule.getHeartBeat();
          }
        });
      } else {
        console.log('Health Not Available');
      }
    });
  };

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
        <Button title="Scan" onPress={onScan} />
        <Button title="Camera Based Heart Rate" onPress={onCameraPress} />
        <Button title="Health Kit Heart Rat" onPress={onHealthKitPress} />
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
            <Button title="Get Steps" onPress={onSteps} />
            <Button title="Get Calorie" onPress={onCalorie} />
            <Button title="Get Distance" onPress={onDistance} />
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

export {onScan, onCameraPress, onHealthKitPress, startConnect};
