package com.fanplayiot.core.api

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log


object PedometerService {
    private const val TAG = "PedometerService"
    private lateinit var mSensorManager: SensorManager
    var isSensorPresent = false
        private set

    @JvmStatic
    fun init(context: Context, listener: StepCounterEventListener) {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.run {
            getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let { sensor ->
                registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                isSensorPresent = false
            }
        }
    }

    @JvmStatic
    fun close(listener: StepCounterEventListener) {
        if (this::mSensorManager.isInitialized) {
            mSensorManager.unregisterListener(listener)
            Log.d(TAG, "close unregister StepCounterEventListener")
        }
    }
}