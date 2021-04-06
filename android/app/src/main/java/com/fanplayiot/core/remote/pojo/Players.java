package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import com.fanplayiot.core.db.local.entity.Player;
import com.fanplayiot.core.db.local.entity.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Players extends BaseData<Players> {
    private static final String TAG = "Players";
    private Player[] playerList;
    private Long teamIdServer;
    private String teamName;

    public Player[] getPlayerList() {
        return playerList;
    }

    public Long getTeamIdServer() {
        return teamIdServer;
    }

    public String getTeamName() {
        return teamName;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    protected Players fromJSONObject(JSONObject jsonObject) throws JSONException {
        Player[] playersArray;
        JSONObject teamObj = jsonObject.getJSONObject("response")
                .getJSONArray("playersdata")
                .getJSONObject(0);
        teamIdServer = teamObj.getLong("teamid");
        teamName = teamObj.getString("teamname");
        JSONArray array = jsonObject.getJSONObject("response")
                .getJSONArray("playersdata")
                .getJSONObject(0)
                .getJSONArray("players");
        int teamId = 1;
        playersArray = new Player[array.length()];
        for(int i = 0; i < array.length(); i++) {
            JSONObject playerItem = array.getJSONObject(i);
            int playerId = playerItem.getInt("playerid");
            Player player = new Player(playerId, teamId, playerId);
            player.setPlayerName(playerItem.getString("playername"));
            player.setUrl(playerItem.getString("playerimagepath"));
            player.setPlaying(playerItem.getBoolean("isplaying"));
            player.setPlayerActive(playerItem.getBoolean("isplayeractive"));
            playersArray[i] = player;
        }
        Log.d(TAG, "Total players " + playersArray.length);
        playerList = playersArray;
        return this;
    }
}
