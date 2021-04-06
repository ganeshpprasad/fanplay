package com.fanplayiot.core.googlefit

import android.util.Log
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse

abstract class BaseTask(private val historyClient: HistoryClient, private val callback: ServiceCallback) {

    protected suspend fun fetch() {
        callback.onPreExecute(getTaskId())

        try {
            val readRequest = getReadRequest()

            historyClient.readData(readRequest)
                    .addOnSuccessListener { dataReadResponse ->
                        dataReadResponse?.let { getData(it) }
                    }.addOnFailureListener {
                        Log.d(TAG, "failed ", it)
                        callback.onFailureTask(getTaskId())
                    }
        } catch (e: Exception) {
            Log.e(TAG, "Error msg: " + e.message, e)
        }

        callback.onPostExecute(getTaskId())
    }

    suspend fun fetchDailyTotal(dataType: DataType) {
        callback.onPreExecute(getTaskId())

        try {
            historyClient.readDailyTotal(dataType).addOnSuccessListener { dataSet ->
                sendDataSet(dataSet)
            }.addOnFailureListener {
                Log.d(TAG, "failed ", it)
                callback.onFailureTask(getTaskId())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error msg: " + e.message, e)
        }

        callback.onPostExecute(getTaskId())
    }
    private fun getData(response: DataReadResponse) {
        try {
            var result = false
            if (response.buckets.size > 0) {
                //Log.i(TAG, "Number of returned bucket DataSets is: ${response.buckets.size}")
                for (bucket in response.buckets) {
                    val dataSets = bucket.dataSets
                    for (dataSet in dataSets) {
                        result = sendDataSet(dataSet)
                    }
                }
            } else if (response.dataSets.size > 0) {
                //Log.i(TAG, "Number of returned DataSets is: ${response.dataSets.size}")
                for (dataSet in response.dataSets) {
                    result = sendDataSet(dataSet)
                }
            }
            if (!result) {
                callback.onFailureTask(getTaskId())
            } else {
                onComplete()
                callback.onSuccessTask(getTaskId())
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "error in getData ", e)
            callback.onFailureTask(getTaskId())
        }
    }

    abstract fun getTaskId(): Int

    abstract fun getReadRequest(): DataReadRequest

    abstract fun sendDataSet(dataSet: DataSet): Boolean

    abstract fun onComplete()

    companion object {
        private const val TAG = "HeartRateTask"
    }
}