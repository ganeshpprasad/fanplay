package com.fanplayiot.core.remote.repository

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.db.local.entity.GET_FE_DETAILS_BY_TEAMS_ID
import com.fanplayiot.core.db.local.entity.Messages
import com.fanplayiot.core.db.local.repository.MasterRepository
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.getResultArray
import com.fanplayiot.core.utils.Constant
import kotlinx.coroutines.CoroutineScope
import java.util.*

class MasterRemoteRepository(private val context: Context,
                             private val repository: MasterRepository) {

    companion object {
        //private const val TAG = "MasterRemoteRepository"
        private const val TEAM_QUERY = "?teamId="
    }

    fun getTotalsTeam(leftTeamId: Long, rightTeamId: Long, scope: CoroutineScope) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                Constant.BASEURL + Constant.GET_FE_DETAILS_BY_TEAMS + TEAM_QUERY + leftTeamId + "," + rightTeamId,
                null,
                Response.Listener { response ->
                    //Log.d(TAG, response.toString())
                    getResultArray(response)?.let {
                    repository.insertMaster(com.fanplayiot.core.db.local.entity.Messages(
                            com.fanplayiot.core.db.local.entity.GET_FE_DETAILS_BY_TEAMS_ID,
                            System.currentTimeMillis(),
                            it.toString()), scope)
                    }
                },
                Response.ErrorListener {

                }){
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["accept"] = "text/plain"
                return headers
            }

        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }
}

/*
2021-02-25 12:56:03.695 11828-11828/com.fanplayiot.core.dev D/MasterRemoteRepository:
{"response":{"result":[{"totalwave":137,"totalhrcount":3203,"avgbpm":72,"totaltap":841,"teamname":"England","teamlogourl":"https:\/\/fangurudevstrg.blob.core.windows.net\/teamslogoandbg\/Teamslogo_4_eng_team.png"},{"totalwave":269,"totalhrcount":27807,"avgbpm":80,"totaltap":1123,"teamname":"India","teamlogourl":"https:\/\/fangurudevstrg.blob.core.windows.net\/teamslogoandbg\/Teamslogo_3_india_team.png"}]},"statuscode":200,"message":"","callstarttime":"2021-02-25 07:26","callendtime":"2021-02-25 07:26"}
2021-02-25 12:56:03.850 11828-11828/com.fanplayiot.core.dev D/MasterRemoteRepository: {"response":{"result":[{"totalwave":137,"totalhrcount":3203,"avgbpm":72,"totaltap":841,"teamname":"England","teamlogourl":"https:\/\/fangurudevstrg.blob.core.windows.net\/teamslogoandbg\/Teamslogo_4_eng_team.png"},{"totalwave":269,"totalhrcount":27807,"avgbpm":80,"totaltap":1123,"teamname":"India","teamlogourl":"https:\/\/fangurudevstrg.blob.core.windows.net\/teamslogoandbg\/Teamslogo_3_india_team.png"}]},"statuscode":200,"message":"","callstarttime":"2021-02-25 07:26","callendtime":"2021-02-25 07:26"}

 */