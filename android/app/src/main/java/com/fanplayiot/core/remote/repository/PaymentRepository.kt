package com.fanplayiot.core.remote.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.db.local.entity.json.OrderDetails
import com.fanplayiot.core.remote.RESPONSE
import com.fanplayiot.core.remote.STATUS_CODE
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.pojo.PaymentStatus
import com.fanplayiot.core.utils.Constant
import org.json.JSONException
import java.util.*

class PaymentRepository(private val context: Context) {

    private val mPaymentStatus = MutableLiveData<String?>(null)
    val paymentStatus: LiveData<String?>
        get() = mPaymentStatus

    companion object {
        private const val TAG = "PaymentRepository"
    }

    fun resetPaymentStatus() {
        mPaymentStatus.postValue(null)
    }

    //curl -X GET "https://fanplaygurudevapi.azurewebsites.net/api/FanSocial/CreateRazorpayOrder?challengeid=50&sid=81&amount=9900" -H "accept: text/plain"
    fun getCreateRazorpayOrder(challengeId: Long, sid: Long, amount: Double, onOrderIdCreated: (String?) -> Unit) {
        val url: Uri? = try {
            Uri.parse(Constant.BASEURL + Constant.GET_CREATE_RAZORPAYORDER)
                    .buildUpon().apply {
                        appendQueryParameter("challengeid", challengeId.toString())
                        appendQueryParameter("sid", sid.toString())
                        appendQueryParameter("amount", amount.toInt().toString())
                    }.build()
        } catch (uoe: UnsupportedOperationException) {
            null
        }
        if (url == null) return
        Log.d(TAG, url.toString())

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                url.toString(), null,
                Response.Listener { response ->
                    //Log.d(TAG, response.toString())
                    try {
                        val statusCode = response.getInt(STATUS_CODE)
                        val responseStr: String? = response.getString(RESPONSE)
                        if (statusCode == 200) {
                            onOrderIdCreated(responseStr)
                        } else {
                            onOrderIdCreated(null)
                        }
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
            onOrderIdCreated(null)

        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["accept"] = "text/plain"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }

    fun postPaymentStatus(tokenId: String, orderDetails: OrderDetails, paymentId: String?, paymentMessage: String?,
            onPostPayment: (String?) -> Unit) {
        val paymentStatus = PaymentStatus()
        paymentStatus.orderDetails = orderDetails
        paymentStatus.paymentId = paymentId
        paymentStatus.message = paymentMessage
        val postJson = paymentStatus.getJSONObject() ?: run {
            Log.d(TAG, "Payment status is null")
            return
        }
        Log.d(TAG, postJson.toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_PAYMENT, postJson,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val code = response.getInt(STATUS_CODE)
                        Log.d(TAG, "Status code postPaymentStatus $code")
                        if (code == 200) {
                            onPostPayment("success")
                            Log.d(TAG, "postPaymentStatus success")
                            return@Listener
                        } else {
                            Log.d(TAG, "postPaymentStatus failed")
                        }
                        //result.postValue(code == 200);
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                    onPostPayment("failure")
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
            VolleySingleton.getInstance(context).handleError(error)
            onPostPayment("failure")
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                val authValue = "Bearer $tokenId"
                headers["Authorization"] = authValue
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }
}