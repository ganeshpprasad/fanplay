/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useState, useEffect} from 'react';

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

export class FanplayBandModule extends React.Component {
  deviceScanListener;
  deviceHeartRateListener;
  cameraHeartRateListener;
  healthKitHeartRateListener;
  deviceStepCalorieDistanceListener;

  constructor(props) {
    super(props);
    this.state = {
      heartRate: null,
      distance: null,
      // heartRaew: null,
      // heartRate: null,
    };
    let deviceModule = new NativeEventEmitter(NativeModules.DevicesModule);

    //device scan
    this.deviceScanListener = deviceModule.addListener(
      'devicesScanEvent',
      this.deviceScan,
    );

    //device heart rate scan
    this.deviceHeartRateListener = deviceModule.addListener(
      'devicesHeartRateScanEvent',
      this.devicesHeartRateScan,
    );

    //camera based heart rate
    this.cameraHeartRateListener = deviceModule.addListener(
      'cameraHeartRateEvent',
      this.camHeartRateEvent,
    );

    //health kit
    this.healthKitHeartRateListener = deviceModule.addListener(
      'healthKitHeartRateEvent',
      this.healthKitheartRateEvent,
    );

    this.deviceStepCalorieDistanceListener = deviceModule.addListener(
      'deviceStepsCalorieDistanceDiscover',
      this.deviceStepsCalorieDistance,
    );

    DevicesModule.init();
    PulseRateModule.init();
    HealthKitModule.init();
  }

  componentWillUnmount() {
    this.deviceScanListener.remove();
    this.cameraHeartRateListener.remove();
    this.healthKitHeartRateListener.remove();
    this.deviceStepCalorieDistanceListener.remove();
  }

  macAddressCalback = callback => {};

  startScanForDevices = (callback: (macAddress: string) => void) => {
    DevicesModule.scan();
    this.macAddressCalback = callback;
  };

  devicesHeartRateScan = event => {
    console.log(event);
    console.log(event.hr);
    console.log(event.step);
    console.log(event.cal);
    console.log(event.dist);
    // setheartRate(event.hr);
    // setsteps(event.step);
    // setcalorie(event.cal);
    // setdistance(event.dist);
  };

  deviceStepsCalorieDistance = event => {
    console.log(event);
  };

  deviceScan = event => {
    console.log(event.address); // "someValue"
    if (!this.macAddressCalback) {
      throw 'No callback initilased';
    }
    this.macAddressCalback(event.address);
  };

  healthKitheartRateEvent = event => {
    console.log(event);
    console.log(event.heartRate);
    console.log(event.status);
    console.log(event.error);
  };

  camHeartRateEvent = event => {
    console.log(event);
    console.log(event.heartRate);
    console.log(event.status);
    console.log(event.error);
  };
}

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
    DevicesModule.scan();
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
