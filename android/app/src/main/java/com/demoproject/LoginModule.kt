package com.demoproject

import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.fanplayiot.core.db.local.repository.FanEngageRepository
import com.fanplayiot.core.db.local.repository.HomeRepository
import com.fanplayiot.core.utils.AbstractRNModule
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginModule(reactContext: ReactApplicationContext) : AbstractRNModule(reactContext) {
    private val homeRepository = HomeRepository(reactContext.applicationContext)
    override fun getName(): String {
        return "LoginModule"
    }

    @ReactMethod
    fun init() {

    }

    @ReactMethod
    fun loginUser(email: String, password: String, promise: Promise) {
        FirebaseApp.initializeApp(reactContext.applicationContext)
        val firebaseApp = FirebaseApp.getInstance()
        val auth = FirebaseAuth.getInstance(firebaseApp)
        Log.d(name, "Sign in with email")
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            it?.let { result ->
                Log.d(name, "result ${result.user?.displayName}")
                promise.resolve(result.user)
            }
        }.addOnFailureListener {
            promise.reject(it)
        }

    }

    @ReactMethod
    fun insertOrUpdate(tokenId: String, tokenExpires: Double, displayName: String?, type: Double, callback: Callback) {
        coroutineScope.launch {
            homeRepository.insertOrUpdateUser(tokenId, tokenExpires.toLong(), displayName, type.toInt())
            callback.invoke(true)
        }
    }

    @ReactMethod
    fun insertTeam(teamId: Double, callback: Callback) {
        coroutineScope.launch {
            homeRepository.updateTeam("KBFC", teamId.toLong())
            callback.invoke(true)
        }
    }

}