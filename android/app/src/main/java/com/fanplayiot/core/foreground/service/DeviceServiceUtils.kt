package com.fanplayiot.core.foreground.service

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fanplayiot.core.db.local.entity.DEVICE_BAND
import com.fanplayiot.core.db.local.entity.FitnessDeviceType
import com.fanplayiot.core.db.local.entity.GOOGLE_FIT
import com.fanplayiot.core.db.local.entity.PHONE
import com.fanplayiot.core.db.local.entity.json.SessionSummary
import com.fanplayiot.core.utils.NotificationUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Suppress("unused")
@ExperimentalCoroutinesApi
object DeviceServiceUtils {

    private const val TAG = "DeviceServiceUtils"

    enum class Command {
        INIT, STOP, REBIND_STOP
    }

    enum class State {
        UNKNOWN, STARTED, STOPPING
    }

    private lateinit var mService: SCDService
    private lateinit var mHRService: HRService
    private var mFanFitListener: FanFitListener? = null
    private var mFanEngageListener: FanEngageListener? = null

    val currentCommand = MutableLiveData(Command.INIT)

    private val mServiceRunning = MutableLiveData(false)
    val serviceRunning: LiveData<Boolean>
        get() = mServiceRunning

    private val mMessage = MutableLiveData<String>(null)
    val message: LiveData<String>
        get() = mMessage

    private var mType = -1
    private var mBandAddress: String? = null
    private var mState = State.UNKNOWN
    private var reBind = false
    private var mSessionId: Long = -1L

    var progressLive : LiveData<SessionSummary>? = null
        get() = if (field == null) MutableLiveData<SessionSummary>(null) else field

    // Setter
    fun setBandAddress(address: String) {
        mBandAddress = address
    }

    fun setFanFitListener(fanFitListener: FanFitListener) {
        mFanFitListener = fanFitListener
    }

    fun setFanEngageListener(fanEngageListener: FanEngageListener) {
        mFanEngageListener = fanEngageListener
    }

    fun setSessionId(id: Long) {
        mSessionId = id
        if (this::mHRService.isInitialized) {
            mHRService.sessionId = mSessionId
            progressLive = mHRService.progressLive
        }
    }

    fun getReBind() = reBind

    fun observerDeviceType(context: Context, fanFitListener: FanFitListener,
                           @FitnessDeviceType type: Int) {
        unBindServices(context)
        mFanFitListener = fanFitListener
        mType = type
        bindServices(context, type)
    }

    fun bindForFanEngage(context: Context, fanEngageListener: FanEngageListener, @FitnessDeviceType type: Int) {
        unBindServices(context)
        mFanEngageListener = fanEngageListener
        mType = type
        bindServices(context, type)
    }

    fun stopObserver(context: Context, fanFitListener: FanFitListener) {
        mFanFitListener = fanFitListener
        mFanFitListener?.let {
            if (this@DeviceServiceUtils::mService.isInitialized &&
                    mService.getType() == DEVICE_BAND) {
                mService.getDeviceBandSCD(it)
            }
        }
        if (mSessionId != -1L) {
            fanFitListener.onStopSession()
        }
        unBindServices(context)
        mSessionId = -1L
    }

    fun onDestroy(context: Context) {
        unBindServices(context)
        unBindSCDService(context)
    }

    private fun bindServices(context: Context, type: Int): Boolean {
        return context.let {
            mState = State.UNKNOWN
            HRService.bindService(it, serviceConnection, type)
        }
    }

    fun reBindServices(context: Context, type: Int): Boolean {
        return context.let {
            mState = State.STOPPING
            reBind = true
            HRService.bindService(it, serviceConnection, type)
        }
    }

    fun bindSCDServices(context: Context, type: Int): Boolean {
        return context.let {
            SCDService.bindService(it, serviceConnection, type)
        }
    }

    fun startHr(caller: FitnessService.Caller) {
        if (reBind) {
            mState = State.STOPPING
            Log.d(TAG, "startHr called with reBind")
            return
        }
        if (!this::mHRService.isInitialized) throw Exception()

        mHRService.sessionId = mSessionId
        if (caller == FitnessService.Caller.FanFit && mFanFitListener != null) {

            if (mHRService.getType() == GOOGLE_FIT) {
                mHRService.initHeartRateFanFitGoogleFit(mFanFitListener!!)
            } else if (mHRService.getType() == DEVICE_BAND) {
                mHRService.initHeartRateFanFitDeviceBand(mFanFitListener!!)
            } else if (mHRService.getType() == PHONE) {
                mHRService.sessionId = mSessionId
                // call phone empty fixed rate scheduler
                mHRService.initSessionFanFitPhone()
            }
        } else if (caller == FitnessService.Caller.FanEngage && mFanEngageListener != null) {
            if (mHRService.getType() == GOOGLE_FIT) {
                mHRService.initHeartRateFanEngageGoogleFit(mFanEngageListener!!)
            } else if (mHRService.getType() == DEVICE_BAND) {
                mHRService.initHeartRateFanEngageDeviceBand(mFanEngageListener!!)
            }
        }
        mState = State.STARTED
    }

    fun startConnectionFlow(fanFitListener: FanFitListener) {
        if (mService.getType() == DEVICE_BAND && mBandAddress != null) {
            fanFitListener.setReconnectFlow(mService.initDeviceBandSCD(mBandAddress!!))
        }
    }

    private fun unBindServices(context: Context) {
        context.run {
            if (this@DeviceServiceUtils::mHRService.isInitialized) {
                mHRService.stopHRService()
            }
            HRService.unBindService(this, serviceConnection)
        }
        reBind = false
    }

    fun unBindSCDService(context: Context) {
        if (this@DeviceServiceUtils::mService.isInitialized) {
            mService.stopService()
        }
        SCDService.unBindService(context, serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            if (className?.className == SCDService::class.java.name) {
                val binder = service as SCDService.LocalBinder
                mService = binder.getService()
                if (mService.getType() == GOOGLE_FIT) {
                    mFanFitListener?.let { mService.initGoogleFitSCD(it) } ?: run {
                        mMessage.postValue("error")
                    }
                } else if (mService.getType() == PHONE) {
                    mFanFitListener?.let { mService.initPhonePedometer(it) } ?: run {
                        mMessage.postValue("error")
                    }
                } else if (mService.getType() == DEVICE_BAND) {
                    mFanFitListener?.let { mService.getDeviceBandSCD(it) }
                }

            } else if (className?.className == HRService::class.java.name) {
                val binder = service as HRService.LocalBinder
                mHRService = binder.getService()
                mServiceRunning.postValue(true)
            }

        }

        override fun onServiceDisconnected(className: ComponentName?) {
            if (className?.className == HRService::class.java.name) {
                mServiceRunning.postValue(false)
            }
        }

        override fun onBindingDied(className: ComponentName?) {
            //super.onBindingDied(className)
            if (className?.className == HRService::class.java.name) {
                mServiceRunning.postValue(false)
            }
        }

        override fun onNullBinding(className: ComponentName?) {
            //super.onNullBinding(className)
            if (className?.className == HRService::class.java.name) {
                mServiceRunning.postValue(false)
            }
        }
    }

    fun onIntent(action: String?, bundle: Bundle?) {
        val sessionId: Long = bundle?.getLong(FitnessService.EXTRA_SESSION_ID, -1) ?: -1
        val caller: String = bundle?.getString(FitnessService.EXTRA_CALLER, FitnessService.Caller.FanEngage.name)
                ?: "UnKnown"
        Log.d(TAG, "onIntent $caller $sessionId")
        action?.let {
            if (NotificationUtils.DEVICE_SERVICE_ACTION == it) {
                currentCommand.postValue(
                        if (sessionId ?: -1 > 0 ||
                                caller == FitnessService.Caller.FanFit.name) Command.STOP
                        else Command.REBIND_STOP)
            }
        }
    }
}
