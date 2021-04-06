package com.fanplayiot.core.googlefit

import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityTask(private val historyClient: HistoryClient, private val callback: ServiceCallback) : BaseTask(historyClient, callback) {
    private var steps: Int = 0
    private var calories: Float = 0f
    private var distance: Float = 0f

    suspend fun fetchActivity() {
        fetch()
    }

    override fun getTaskId() = ACTIVITY_TASK

    override fun getReadRequest(): DataReadRequest {
        // Setting a start and end date using a range of 1 week before this moment.
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DATE)

        val endTime = cal.timeInMillis
        cal.set(year, month, day, 0, 0, 0)
        //cal.add(Calendar.SECOND, -1)
        val startTime = cal.timeInMillis

        //Log.i(TAG, "Range Start: ${dateFormat.format(startTime)}")
        //Log.i(TAG, "Range End: ${dateFormat.format(endTime)}")
        steps = 0
        calories = 0f
        distance = 0f

        return DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_DISTANCE_DELTA)
                .enableServerQueries()
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
    }

    override fun sendDataSet(dataSet: DataSet): Boolean {

        for (dp in dataSet.dataPoints) {
            //Log.i(TAG, "\tType: ${dp.dataType.name} time: ${dp.getTimestamp(TimeUnit.MILLISECONDS)}")
            for (field in dp.dataType.fields) {
                //Log.i(TAG, "\tfield: ${field.name} format: ${field.format}" )
                readDataPoints(dp, field)
            }
        }
        if (steps > 0 || calories > 0 || distance > 0) {
            return true
        }
        return false
    }

    private fun readDataPoints(dp: DataPoint, field: Field) {
        if (dp.dataType == DataType.AGGREGATE_STEP_COUNT_DELTA && field.name.equals(Field.FIELD_STEPS.name, ignoreCase = true)) {
            val value = dp.getValue(field).takeIf { it.asInt() > 0 }
            steps = value?.asInt() ?: 0
        } else if (dp.dataType == DataType.AGGREGATE_CALORIES_EXPENDED && field.name.equals(Field.FIELD_CALORIES.name, ignoreCase = true)) {
            val value = dp.getValue(field).takeIf { it.asFloat().toInt() > 0 }
            calories = value?.asFloat() ?: 0f
        } else if (dp.dataType == DataType.AGGREGATE_DISTANCE_DELTA && field.name.equals(Field.FIELD_DISTANCE.name, ignoreCase = true)) {
            val value = dp.getValue(field).takeIf { it.asFloat() > 0 }
            value?.let { distance = (value.asFloat()  / 1000) }
        }
    }

    override fun onComplete() {
        if (steps > 0) {
            callback.updateActivity(steps, calories, distance, callback.getType())
        }
    }

    companion object {
        private const val TAG = "ActivityTask"
    }

}