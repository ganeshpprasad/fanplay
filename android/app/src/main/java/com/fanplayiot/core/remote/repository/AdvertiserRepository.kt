package com.fanplayiot.core.remote.repository

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.db.local.repository.SponsorRepository
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.pojo.AdsAnalytics
import com.fanplayiot.core.remote.pojo.BaseData.Companion.getInstance
import com.fanplayiot.core.remote.pojo.Sponsors
import com.fanplayiot.core.utils.Constant
import org.json.JSONException
import java.util.*

class AdvertiserRepository(private val context: Context, private val repository: SponsorRepository) {
    @WorkerThread
    fun postAnalyticsAndGetSponsors() {
        val user = repository.userData
        if (user == null) {
            repository.refreshSponsors(this)
            return
        }
        val tokenId = user.tokenId
        val adsAnalytics = AdsAnalytics()
        adsAnalytics.setTeamIdCheered(repository.team!!.teamIdServer ?: 1)
        val allAnalytics = repository.allSponsorAnalytics
        if (allAnalytics != null && allAnalytics.isNotEmpty()) {
            try {
                for (item in allAnalytics) {
                    if (item.noOfClicks > 0) {
                        adsAnalytics.addToAnalyticsArray(item)
                    }
                }
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "error getAdBanners", e)
            } catch (e: InstantiationException) {
                Log.e(TAG, "error getAdBanners", e)
            } catch (e: JSONException) {
                Log.e(TAG, "error getAdBanners", e)
            }
        } else {
            repository.refreshSponsors(this)
            return
        }
        val jsonObject = adsAnalytics.getJSONObject()
        if (jsonObject == null) {
            repository.refreshSponsors(this)
            return
        }
        //Log.d(TAG, jsonObject.toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_SPONSORANALYTICS, jsonObject,
                Response.Listener { response ->
                    //Log.d(TAG, response.toString())
                    try {
                        val statusCode = response.getInt("statuscode")
                        if (statusCode == 200) {
                            repository.refreshSponsors(this@AdvertiserRepository)
                        }
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
            VolleySingleton.getInstance(context).handleError(error)
            repository.refreshSponsors(this@AdvertiserRepository)
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

    //curl -X GET "https://fanplaygurudevapi.azurewebsites.net/api/Sponsor/GetSponsorsData?teamId=1" -H "accept: text/plain"
    @WorkerThread
    fun getSponsors(teamIdServer: Long) {
        Log.d(TAG, Constant.BASEURL + Constant.GET_SPONSORS + TEAM_QUERY + teamIdServer)
        val api: JsonObjectRequest = object : JsonObjectRequest(Method.GET, Constant.BASEURL + Constant.GET_SPONSORS + TEAM_QUERY + teamIdServer,
                null,
                Response.Listener { response ->
                    try {
                        //Log.d(TAG, response.toString())
                        val sponsors = getInstance(Sponsors::class.java, response.toString())
                        if (sponsors != null && sponsors.advertisers == null && sponsors.isEmptySponsors) {
                            repository.clearSponsors()
                        }
                        if (sponsors != null) {
                            val advertisers = sponsors.advertisers
                            if (advertisers != null && advertisers.isNotEmpty()) {
                                repository.addSponsors(advertisers)
                            }
                        }
                    } catch (iae: IllegalAccessException) {
                        Log.e(TAG, "error ", iae)
                    } catch (ie: InstantiationException) {
                        Log.e(TAG, "InstantiationException ", ie)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "other error ", e)
                    }
                },
                Response.ErrorListener { error -> Log.e(TAG, "error ", error) }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["accept"] = "text/plain"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(api)
    }

    companion object {
        private const val TAG = "AdvertiserRepository"
        private const val TEAM_QUERY = "?teamId="
    }
}