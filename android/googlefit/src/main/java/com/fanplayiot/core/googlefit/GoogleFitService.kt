package com.fanplayiot.core.googlefit

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GoogleFitService(callback: ServiceCallback) {
    var historyClient: HistoryClient? = null
        private set
    private val callback: ServiceCallback

    private val fitnessOptions: FitnessOptions by lazy {
        FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .build()
    }

    fun stopFetching() {
        historyClient = null
    }

    /**
     * Gets a Google account for use in creating the Fitness client. This is achieved by either
     * using the last signed-in account, or if necessary, prompting the user to sign in.
     * `getAccountForExtension` is recommended over `getLastSignedInAccount` as the latter can
     * return `null` if there has been no sign in before.
     */
    private fun getGoogleAccount(context: Context) = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun buildFitnessClient(activity: FragmentActivity, requestCode: Int) {

        val account: GoogleSignInAccount = getGoogleAccount(activity.applicationContext)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,  // your activity
                    requestCode,  // e.g. 1
                    account,
                    fitnessOptions)
            historyClient = null
            Log.d(TAG, "historyClient is null")
        } else {
            historyClient = Fitness.getHistoryClient(activity, account)
            Log.d(TAG, "historyClient is set")
        }
    }

    fun subscribeToFetching() {
        if (historyClient == null) return
        //GoogleFitTask(historyClient, callback)
        GlobalScope.launch {
            HeartRateTask(historyClient!!, callback).fetchHeartRate()
        }
    }

    suspend fun subscribeToTask(task: BaseTask) {
        if (task is HeartRateTask) {
            task.fetchHeartRate()
        } else if (task is ActivityTask) {
            task.fetchActivity()
        }
    }

    fun checkPermissions(context: Context): Boolean {
        val account: GoogleSignInAccount = getGoogleAccount(context)
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun requestPermissions(activity: FragmentActivity?, requestCode: Int) {
        activity?.let {
            val account: GoogleSignInAccount = getGoogleAccount(activity.applicationContext)
            GoogleSignIn.requestPermissions(
                it,  // your activity
                requestCode,  // e.g. 1
                account,
                fitnessOptions)
        }
    }

    companion object {
        private const val TAG = "GoogleFitService"
    }

    init {
        this.callback = callback
    }
}