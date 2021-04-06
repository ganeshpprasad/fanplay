package com.fanplayiot.core.googlefit

interface ServiceCallback {
    fun getIntervalMinutes() : Int
    fun getType(): Int
    fun onPreExecute(taskId: Int)
    fun onPostExecute(taskId: Int)
    fun onSuccessTask(taskId: Int)
    fun onFailureTask(taskId: Int)
    fun updateHeartRate(hr: Int, type: Int)
    fun updateActivity(step: Int, calorie: Float, distance: Float, type: Int)
}

const val ACTIVITY_TASK = 1
const val HEART_RATE_TASK = 2