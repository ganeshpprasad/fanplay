package com.fanplayiot.core.background

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import com.fanplayiot.core.db.local.repository.FitnessRepository
import java.util.*
import java.util.concurrent.TimeUnit

class FitnessWork(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    var repository: FitnessRepository = FitnessRepository(context)

    override suspend fun doWork(): Result {
        try {
            repository.postFitness()
            return Result.success()
        } catch (ie: Exception) {
            Log.e(TAG, "error " + ie.message, ie)
        }
        return Result.failure()
    }


    companion object {
        const val TAG = "FitnessWork"
        const val DEFAULT_INTERVAL: Long = 15L // in minutes

        @JvmStatic
        fun startPeriodicFitnessSync(context: Context, interval: Long): UUID {
            // Create request
            val repeatInterval = if (interval > DEFAULT_INTERVAL) interval else DEFAULT_INTERVAL
            val request: PeriodicWorkRequest = PeriodicWorkRequest.Builder(FitnessWork::class.java, repeatInterval, TimeUnit.MINUTES)
                    .setConstraints(WorkerHelper.getDefaultConstraints())
                    .addTag(TAG)
                    .build()
            WorkerHelper.addToWorkManager(context, request)
            return request.id
        }

        @JvmStatic
        fun startFitnessSync(context: Context): UUID {
            // Create request
            val request = OneTimeWorkRequest.Builder(FitnessWork::class.java)
                    .setConstraints(WorkerHelper.getDefaultConstraints())
                    .build()
            WorkerHelper.addToWorkManager(context, request)
            return request.id
        }
    }

}