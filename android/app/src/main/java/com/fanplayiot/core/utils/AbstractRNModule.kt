package com.fanplayiot.core.utils

import android.content.Context
import android.util.Log
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class AbstractRNModule(protected val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {
    companion object {
        protected const val TAG = "AbstractRNModule"
    }

    protected val context: Context = reactContext.applicationContext
    val handler = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "CoroutineExceptionHandler got $exception")
    }
    private val job = SupervisorJob()
    protected val coroutineScope = CoroutineScope(Dispatchers.IO + job + handler)

    init {
        reactContext.addLifecycleEventListener(this)
    }

    override fun onHostResume() {

    }

    override fun onHostPause() {

    }

    override fun onHostDestroy() {
        job.cancel()
    }
}