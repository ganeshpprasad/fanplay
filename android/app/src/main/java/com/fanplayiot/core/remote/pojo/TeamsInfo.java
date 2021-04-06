package com.fanplayiot.core.remote.pojo;

import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TeamsInfo extends BaseData<TeamsInfo> {
    private List<Team> teams;
    private String jsonString;

    @Nullable
    public List<Team> getTeams() {
        return teams;
    }

    @Nullable
    public String getJsonString() {
        return jsonString;
    }

    @Override
    @Nullable
    public JSONObject getJSONObject() {
        return null;
    }

    @Override
    @Nullable
    public TeamsInfo fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONObject response = jsonObject.optJSONObject("response");
        if (response == null) return null;
        JSONArray array = response.optJSONArray("result");
        if (array == null) return null;
        if (array.length() > 0) {
            jsonString = array.toString();
            List<Team> teamList = new ArrayList<>(array.length());

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                    Team team = new Team();
                    team.setId(i);
                    team.setTeamName(obj.optString("teamname"));
                    team.setTeamIdServer((long) obj.optInt("teamid", i));
                    team.setTeamShortName(obj.getString("teamshortname"));
                    team.setTeamPriority(obj.optInt("teampriority", i));
                    team.setTeamLogoUrl(obj.optString("teamlogourl", ""));
                    team.setTeamBackgroundImage(obj.optString("teambackgroundimage", ""));
                    team.setTournamentId(obj.optInt("tournamentid", -1));
                    team.setTournamentName(obj.optString("tournamentname", ""));
                    String storeUrl = obj.optString("teamstoreurl", "");
                    if (storeUrl.equalsIgnoreCase("null")) storeUrl = null;
                    team.setTeamStoreUrl(storeUrl);
                    teamList.add(team);
            }
            this.teams = teamList;
            return this;
        }

        return null;
    }
}
