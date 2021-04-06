/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useState, useEffect} from 'react';
import {
  SafeAreaView,
  NativeModules,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
  TextInput,
} from 'react-native';


const { LoginModule } = NativeModules;

const LoginTest = () => {
  const [email, onChangeEmail] = useState('')
  const [password, onChangePassword] = useState('')
  const [tokenId, onTokenIdChange] = useState('')

  const onLogin = async() => {
    try {
      const loginResult = await LoginModule.loginUser(
        email,
        password
      );
      if (loginResult !== null) {
        console.log(loginResult);
      }
    } catch (error) {
      console.log(error);
    }
  }

  const onLoginToken = async() => {
    try {
      var now = new Date();
      now.setHours(now.getHours() + 1)
  
      const result = await LoginModule.insertOrUpdate(
        tokenId,
        now.getTime(),  
        'User name',
        1
      );
      console.log(result);
      }
    } catch (error) {
      console.log(error);
    }
  }

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
        <ScrollView
          contentInsetAdjustmentBehavior="automatic"
          style={styles.scrollView}>
          <View>
            <Text>Login</Text>
            <TextInput style={{borderColor: 'black', borderWidth: 1}} onChangeText={onChangeEmail} value={email}/>
            <TextInput style={{borderColor: 'black', borderWidth: 1}} onChangeText={onChangePassword} onSubmitEditing={onLogin} value={password}/>
            <TextInput style={{borderColor: 'black', borderWidth: 1}} onChangeText={onTokenIdChange} onSubmitEditing={onLoginToken} value={tokenId}/>
            <Text>{tokenId}</Text>
          </View>
        </ScrollView>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  
});

export default LoginTest;
