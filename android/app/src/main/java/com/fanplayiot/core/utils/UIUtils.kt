package com.fanplayiot.core.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.fanplayiot.core.utils.Constant.REQUEST_IMAGE_CAPTURE
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val FANENGAGE_TAB_INDEX = 0
const val FANFIT_TAB_INDEX = 1
const val PROFILE_TAB_INDEX = 2
const val LEADERBOARD_TAB_INDEX = 3
const val FANSOCIAL_TAB_INDEX = 4

open class OneTimeEvent<out T>(private val content: T? = null) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T? = content
}

fun <T> T.toOneTimeEvent() =
        OneTimeEvent(this)

fun <T> LiveData<out OneTimeEvent<T>>.observeEvent(owner: LifecycleOwner, onEventUnhandled: (T) -> Unit) {
    observe(owner, Observer { it?.getContentIfNotHandled()?.let(onEventUnhandled) })
}

fun <T> LiveData<T>.toReactEvent(owner: LifecycleOwner, eventName: String, reactContext: ReactContext) {
    observe(owner, {
        reactContext
                .getJSModule(RCTDeviceEventEmitter::class.java)
                .emit(eventName, it)
    })
}

@SuppressLint("MissingPermission")
fun FragmentActivity.registerInternetListener(onAvailableCallback: () -> Unit): ConnectivityManager.NetworkCallback {
    val connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = ActivityNetworkCallback(onAvailableCallback)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        connMgr.registerDefaultNetworkCallback(callback)
        return callback
    } else {
        val builder = NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET)
        connMgr.registerNetworkCallback(builder.build(), callback)
        return callback
    }
}

fun FragmentActivity.unregisterInternetListener(networkCallback: ConnectivityManager.NetworkCallback) {
    val connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    connMgr.unregisterNetworkCallback(networkCallback)
}

class ActivityNetworkCallback(private val onAvailableCallback: () -> Unit) : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onAvailableCallback.invoke()
    }
}

fun FragmentActivity.dispatchTakePictureIntent(): String? {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    try {
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                // Create an image file name
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
                File.createTempFile(
                        "JPEG_${timeStamp}_", /* prefix */
                        ".jpg", /* suffix */
                        storageDir /* directory */
                )
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.d(TAG, "IOException")
                null
            }
            // Continue only if the File was successfully created

            return photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.fanplayiot.core.provider",
                        it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                it.absolutePath
            }
        }
    } catch (e: ActivityNotFoundException) {
        // display error state to the user
        Log.d(TAG, "ActivityNotFoundException")
    }
    return null
}

fun FragmentActivity.dispatchTakePictureIntentForFansocial(): String? {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    try {
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                // Create an image file name
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
                File.createTempFile(
                        "JPEG_${timeStamp}_", /* prefix */
                        ".jpg", /* suffix */
                        storageDir /* directory */
                )
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.d(TAG, "IOException")
                null
            }
            // Continue only if the File was successfully created

            return photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.fanplayiot.core.provider",
                        it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                startActivityForResult(takePictureIntent, 13001)
                it.absolutePath
            }
        }
    } catch (e: ActivityNotFoundException) {
        // display error state to the user
        Log.d(TAG, "ActivityNotFoundException")
    }
    return null
}

private const val TAG = "UIUtils"