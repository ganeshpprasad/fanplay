package com.fanplayiot.core.googlefit

import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.*
import java.util.concurrent.TimeUnit

class HeartRateTask(private val historyClient: HistoryClient, private val callback: ServiceCallback) : BaseTask(historyClient, callback) {
    private val dateFormat = java.text.DateFormat.getDateInstance()

    suspend fun fetchHeartRate() {
        fetch()
    }

    override fun getTaskId() = HEART_RATE_TASK

    override fun getReadRequest(): DataReadRequest {
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.MINUTE, - callback.getIntervalMinutes())
        //cal.add(Calendar.SECOND, -30)
        //cal.add(Calendar.MINUTE, -Calendar.getInstance().get(Calendar.MINUTE));
        //cal.add(Calendar.SECOND, -Calendar.getInstance().get(Calendar.SECOND));
        val startTime = cal.timeInMillis
        //Log.i(TAG, "Start Time: ${dateFormat.format(startTime)}") // + DateFormat.format("yyyy-MM-dd hh:mm:ss a", startTime))
        //Log.i(TAG, "End Time: ${dateFormat.format(endTime)}") //+ DateFormat.format("yyyy-MM-dd hh:mm:ss a", endTime))
        return DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                .enableServerQueries()
                .bucketByTime(callback.getIntervalMinutes(), TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
    }

    override fun sendDataSet(dataSet: DataSet): Boolean {
        for (dp in dataSet.dataPoints) {
            for (field in dp.dataType.fields) {
                if (dp.dataType == DataType.AGGREGATE_HEART_RATE_SUMMARY && field.name.equals(Field.FIELD_AVERAGE.name, ignoreCase = true)) {
                    val value = dp.getValue(field)
                    callback.updateHeartRate(value.asFloat().toInt(), callback.getType())
                    return true
                }
            }
        }
        return false
    }

    override fun onComplete() {
        // do nothing
    }

    companion object {
        private const val TAG = "HeartRateTask"
    }
}