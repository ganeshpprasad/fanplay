package com.demoproject

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

class MainPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules = mutableListOf<NativeModule>()
        modules.add(LoginModule(reactContext))
        modules.add(FanEngageModule(reactContext))
        modules.add(DevicesModule(reactContext))
        modules.add(PulseRateModule(reactContext))
        modules.add(HealthKitModule(reactContext))
        return modules
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<View, ReactShadowNode<*>>> {
        return emptyList<ViewManager<View, ReactShadowNode<*>>>()
    }
}