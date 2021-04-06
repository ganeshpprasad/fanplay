package com.fanplayiot.core.api

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import java.util.logging.Logger

class StepCounterEventListener(private val mConsumer: IStepCounterListenerConsumer) : SensorEventListener {
    interface IStepCounterListenerConsumer {
        fun onSensorEvent(stepsDelta: Int)
    }

    private var mFirstStepsValue: Int? = null
    private var mLastDx: Int? = null

    override fun onSensorChanged(sensorEvent: SensorEvent?) {

        if (sensorEvent == null) return
        if (sensorEvent.values[0] > Int.MAX_VALUE) {
            if (BuildConfig.DEBUG) Logger.getGlobal().info("probably not a real value: " + sensorEvent.values[0])
            return
        }

        try {
            // first event - remember current sensor value
            if (mFirstStepsValue == null) {
                mFirstStepsValue = sensorEvent.values[0].toInt()
                mLastDx = 0
                return
            }
            val normalizedValue = sensorEvent.values[0].toInt() - mFirstStepsValue!!
            val currentDx = normalizedValue - mLastDx!!
            mLastDx = normalizedValue
            mConsumer.onSensorEvent(currentDx)
            /*
            m_total_today_steps += current_dx;
            m_steps_changed = true;
            sendDataToGUI();
             */
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}