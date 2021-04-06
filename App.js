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
  ScrollView,
  View,
} from 'react-native'; 

const {LoginModule} = NativeModules;  
const {DevicesModule} = NativeModules;
const {FanEngageModule} = NativeModules;   
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
  // let cameraHeartRateEvent; 
  let deviceStepsCalorieDistanceDiscover;

  useEffect(() => {
    setinitDone(true); 
    devicesScanEvent = new NativeEventEmitter(NativeModules.DevicesModule);
    deviceStepsCalorieDistanceDiscover = new NativeEventEmitter(NativeModules.FanEngageModule);
    devicesHeartRateScanEvent = new NativeEventEmitter(NativeModules.FanEngageModule);
    // cameraHeartRateEvent = new NativeEventEmitter(NativeModules.FanEngageModule);

    healthKitHeartRateEvent = new NativeEventEmitter(NativeModules.HealthKitModule); 

    //device scan
    devicesScanEvent.addListener('devicesScanEvent', deviceScan);

    //device heart rate scan from fanband & camera 
    devicesHeartRateScanEvent.addListener('devicesHeartRateScanEvent', devicesHeartRateScan);

    //device step
    deviceStepsCalorieDistanceDiscover.addListener('deviceStepsCalorieDistanceDiscover', deviceStepsCalorieDistance);

    //health kit
    healthKitHeartRateEvent.addListener('healthKitHeartRateEvent', healthKitheartRateEvent);

    LoginModule.init();
    DevicesModule.init();
    FanEngageModule.init(); 
    HealthKitModule.init(); 
  }, []);

  // useEffect(() => {
  //   CalendarModule.getDevices();
  // }, []);

  // useEffect(() => {
  //   eventEmitter.addListener('devicesScanEvent', devicesScanEvent);
  // }, []); // Runs only once at the beginning and when heartRate change

  const deviceStepsCalorieDistance = (event) => {
    console.log(event);
  };

  const devicesHeartRateScan = (event) => {
    console.log(event)
    console.log(event.heartRate);
    // console.log(event.step);
    // console.log(event.cal);
    // console.log(event.dist);
    setheartRate(event.heartRate);
    // setsteps(event.step);
    // setcalorie(event.cal);
    // setdistance(event.dist);
  };

  const deviceScan = (event) => {
    console.log(event.address); // "someValue"
    let devices = scannedDevices.map((i) => i);
    devices.push(event.address);
    setIsScanning(devices);
  };

  const healthKitheartRateEvent = (event) => {
    console.log(event); 
    console.log(event.heartRate); 
    console.log(event.status); 
    console.log(event.error); 
  };
 
  const onScan = () => {
    console.log('on scan'); 
    DevicesModule.scan();
  }; 

  const onHr = () => {
    console.log('on hr');
    FanEngageModule.startFanEngageHeartRate(3);
  };

  const onSteps = () => {
    console.log('onSteps');
    FanEngageModule.startFanEngageSteps();
  };

  // const onCalorie = () => {
  //   console.log('onCalorie');
  //   FanEngageModule.startSteps();
  // };
  
  // const onDistance = () => {
  //   console.log('onDistance');
  //   FanEngageModule.startSteps();
  // }; 

  const startConnect = (device) => {
    DevicesModule.connect(device, (flag) => {
        setConnected(flag);
    });
    console.log('init ' , initDone); 
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
    FanEngageModule.startFanEngageHeartRate(1);
  };

  const onHelathKitPress = () => {
    console.log('We will invoke the native module here!');
    HealthKitModule.isHealthAvailable((flag) => {
      console.log("Health Kit Available Heart ");
      console.log(flag);
      if (flag.flag) {  
        HealthKitModule.requestAuthorization((status, error) => {
          console.log("Health Kit Authorisation For Heart");
          console.log(status);
          console.log(error);
          if (status.status) {
            console.log("Getting Hear Rate ");
            HealthKitModule.startFanEngageHeart();
          } 
        }); 
      }else{
        console.log("Health Not Available");
      }
    });
  };

  const onHelathKitStepPress  = () => {
    console.log('We will invoke the native module here!');
    HealthKitModule.isHealthAvailable((flag) => {
      console.log("Health Kit Available For Steps");
      console.log(flag);
      if (flag.flag) {  
        HealthKitModule.requestAuthorization((status, error) => {
          console.log("Health Kit Authorisation For Steps");
          console.log(status);
          console.log(error);
          if (status.status) {
            console.log("Getting Step Rate ");
            HealthKitModule.startFanEngageSteps();
          } 
        }); 
      }else{
        console.log("Health Not Available");
      }
    });
  };

  const token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImY4NDY2MjEyMTQxMjQ4NzUxOWJiZjhlYWQ4ZGZiYjM3ODYwMjk5ZDciLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZmFucGxheS1kZXYiLCJhdWQiOiJmYW5wbGF5LWRldiIsImF1dGhfdGltZSI6MTYxNzA5NDk1MywidXNlcl9pZCI6IjlDazNKSUVaSnZaMjhHN3BTb1NxT1dUQzhubjIiLCJzdWIiOiI5Q2szSklFWkp2WjI4RzdwU29TcU9XVEM4bm4yIiwiaWF0IjoxNjE3MDk0OTUzLCJleHAiOjE2MTcwOTg1NTMsImVtYWlsIjoiZW1tYUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsiZW1tYUBnbWFpbC5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.ZnaVcBl0Blyh3LfeYNVPq9-P15VDA1PTuPJgYqhX98dyoAH-AafiYwPwCXcihe6_Q8RsWAR2E0unD7tkfY-JirPJNjjtQqVStT3TL4EnKIeMV4E_ZUkXzz9HfxZE2q1iu9GipNQBe0AQUsj9yKzpRGpO2tInySpYQb_pYr150cHwMgFAawFPvhy05w4RCYU2EIk5Yi7tmuM0CvRPwe9I4FoH3Cs5N4__RiBZ7tsgGbirf1S_fwQN4P0Qlb6zStbbwX9h7q7LNgCI0AmpWmEocD4jjmFniwCffVlgN9Gq-9Dqev3J3z2yxJW2Z8HWF9uy4SJzaZ-C7Oe55IDLiGlWKw";

  const onLoginModule = () => {  
    LoginModule.insertOrUpdate(token,6789897,"Manoj",3,(response) => {
      console.log(response);
    }); 
  };
  const onLoginTeamModule = () => {  
    LoginModule.insertTeam(2,(response) => {
      console.log(response);
    }); 
  };

  const onValidateSignIn = () => {
    FanEngageModule.validateSignIn((response) => {
      console.log(response);
    }); 
  };

  const onFanEmote = () => {
    FanEngageModule.getFanEmote(2,(response) => {
      console.log(response);
    }); 
  };

  const onFEDetailsByTeamId = () => {
    FanEngageModule.getFEDetailsByTeamId(2,(response) => {
      console.log(response);
    }); 
  };

  const onIncrementWhistle = () => {
    FanEngageModule.incrementWhistle(20, 1);
  };
  
  const onPedoMeter = () => {
    FanEngageModule.callPedoMeter();
  };

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <ScrollView contentInsetAdjustmentBehavior="automatic">
      <SafeAreaView>
        <Button title="Scan" onPress={onScan} />
        <Button title="Camera Based Heart Rate" onPress={onCameraPress} />
        <Button title="Health Kit Heart Rat" onPress={onHelathKitPress} />
        <Button title="Health Kit Steps" onPress={onHelathKitStepPress} />
        <Button title="Pedo Meter" onPress={onPedoMeter} />
        <Button title="Login Module" onPress={onLoginModule} />
        <Button title="Login Module Team" onPress={onLoginTeamModule} />
        <Button title="Call incrementWhistle" onPress={onIncrementWhistle} />
        <Button title="Call validateSignIn" onPress={onValidateSignIn} />
        {/* <Button title="Call getAllUserDetailsByIdToken" onPress={onAllUserDetailsByIdToken} /> */}
        {/* <Button title="Call getTeamPlayersData" onPress={onTeamPlayersData} /> */}
        <Button title="Call getFanEmote" onPress={onFanEmote} />
        <Button title="Call getFEDetailsByTeamId" onPress={onFEDetailsByTeamId} />
        <Text>*******************************</Text>
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
            {/* <Button title="Get Calorie" onPress={onCalorie} />
            <Button title="Get Distance" onPress={onDistance} /> */}
            <Text style={style.textStyle}>Heart Rate : {heartRate}</Text>
          </>
        )}
      </SafeAreaView>
      </ScrollView>
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