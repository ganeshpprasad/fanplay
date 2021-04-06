package com.fanplayiot.core.remote.pojo;

import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.LeaderBoard;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LeaderBoardArray extends BaseData<LeaderBoardArray> {
    private static final String TAG = "LeaderBoardArray";
    private static final int ARRAY_SIZE = 5;
    private static final int TOP_3 = 3;
    private LeaderBoard[] leaderBoards;

    public LeaderBoard[] getLeaderBoards() {
        return leaderBoards;
    }

    // 1 is Global, 2 is FanEngage, 3 is FanFit
    public LeaderBoard[] getLeaderBoardsForType(int type) {
        if (leaderBoards != null && leaderBoards.length == ARRAY_SIZE) {
            // ((type - 1) * 5)
            LeaderBoard[] copy = new LeaderBoard[ARRAY_SIZE];
            int i = 0;
            for(LeaderBoard copyLb : leaderBoards) {
                copyLb.setId(copyLb.getId() + ((type - 1) * 5));
                copy[i] = copyLb;
                i++;
            }
            return copy;
        }
        return leaderBoards;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    protected @Nullable
    LeaderBoardArray fromJSONObject(JSONObject jsonObject) throws JSONException {
        if (jsonObject.optJSONObject("response") == null) return null;
        JSONArray array = null;
        JSONObject today = null;
        JSONObject allTime = null;
        if (jsonObject.getJSONObject("response").optJSONObject("leaderboard") != null) {
            // Outer leaderboard is Object
            JSONObject outerObj = jsonObject.getJSONObject("response").getJSONObject("leaderboard");
            if (outerObj.optJSONArray("leaderboard") == null) return null;
            array = outerObj.getJSONArray("leaderboard");
            today = outerObj.optJSONObject("userposition");
            allTime = outerObj.optJSONObject("alltimeuserposition");
        } else if (jsonObject.getJSONObject("response").optJSONArray("leaderboard") != null) {
            // Outer leaderboard is Array
            JSONArray responseArray = jsonObject.getJSONObject("response").getJSONArray("leaderboard");
            int arrayLength = responseArray.length();
            if (arrayLength == 0) return null;
            array = responseArray.getJSONObject(0).optJSONArray("leaderboard");
            today = responseArray.getJSONObject(0).optJSONObject("userposition");
            allTime = responseArray.getJSONObject(0).optJSONObject("alltimeuserposition");
        }
        LeaderBoard[] boards = new LeaderBoard[ARRAY_SIZE];
        // Fill with empty once first
        for (int i = 0; i < TOP_3; i++) {
            LeaderBoard leaderBoard = new LeaderBoard();
            leaderBoard.setId(i);
            leaderBoard.setName("");
            leaderBoard.setPoints(0);
            boards[i] = leaderBoard;
        }
        if (array != null) {
            for (int i = 0; i < TOP_3; i++) {
                JSONObject item = array.optJSONObject(i);
                LeaderBoard leaderBoard = boards[i]; //new LeaderBoard();
                //leaderBoard.setId(i);
                if (item != null) {
                    boards[i] = copyToLeaderBoard(leaderBoard, item);
                } else {
                    boards[i] = leaderBoard;
                }
            }
        }
        //JSONObject today = reponseArray.getJSONObject(0).optJSONObject("userposition");
        if (today != null) {
            LeaderBoard leaderBoard = new LeaderBoard();
            leaderBoard.setId(3);
            leaderBoard.setName("");
            leaderBoard.setPoints(0);
            boards[(ARRAY_SIZE - 2)] = copyToLeaderBoard(leaderBoard, today);
        } else {
            LeaderBoard leaderBoard = new LeaderBoard();
            leaderBoard.setId(3);
            leaderBoard.setName("");
            leaderBoard.setPoints(0);
            boards[(ARRAY_SIZE - 2)] = leaderBoard;
        }
        //JSONObject allTime = reponseArray.getJSONObject(0).optJSONObject("alltimeuserposition");
        if (allTime != null) {
            LeaderBoard leaderBoard = new LeaderBoard();
            leaderBoard.setId(4);
            leaderBoard.setName("");
            leaderBoard.setPoints(0);
            boards[(ARRAY_SIZE - 1)] = copyToLeaderBoard(leaderBoard, allTime);
        } else {
            LeaderBoard leaderBoard = new LeaderBoard();
            leaderBoard.setId(4);
            leaderBoard.setName("");
            leaderBoard.setPoints(0);
            boards[(ARRAY_SIZE - 1)] = leaderBoard;
        }
        leaderBoards = boards;
        return this;
    }

    private LeaderBoard copyToLeaderBoard(@NotNull LeaderBoard leaderBoard, @NotNull JSONObject item ) throws JSONException {
        leaderBoard.setRank(item.optInt("rank", 0));
        leaderBoard.setName(item.optString("name"));
        leaderBoard.setPoints(item.optInt("points", 0));
        leaderBoard.setImgpath(item.optString("imgpath"));
        if (item.optDouble("avgfanfitscore", -1f) != -1f && item.optDouble("avguserfanemote", -1f) != -1f) {
            // If avg fanfit score and avg user fanemote is present, its global leaderboard
            leaderBoard.setAvguserfanemote((float) item.getDouble("avguserfanemote"));
            leaderBoard.setHighestuserfanemote((float) item.getDouble("avgfanfitscore"));
        } else if (item.optDouble("avgfanfitscore", -1f) != -1f && item.optDouble("avguserfanemote", -1f) == -1f) {
            // if only avg fanfit score is present, its fitness leaderboard
            leaderBoard.setAvguserfanemote((float) item.getDouble("avgfanfitscore"));
        } else {
            // else its fan emote leaderboard
            leaderBoard.setAvguserfanemote((float) item.optDouble("avguserfanemote", 0));
            leaderBoard.setHighestuserfanemote((float) item.optDouble("highestuserfanemote", 0));

        }
        leaderBoard.setAvguserhr(item.optInt("avguserhr", 0));
        if (item.optInt("hearbeatcount", -1 ) != -1) {
            // If hearbeatcount is present, its global leaderboard
            leaderBoard.setTotaltapcount(item.optInt("hearbeatcount", 0));
        } else if (item.optInt("totalsteps", -1) != -1) {
            // if totalsteps is present, its fitness leaderboard
            leaderBoard.setTotaltapcount(item.optInt("totalsteps", 0));
            leaderBoard.setTotalwavecount(item.optInt("totalcalories", 0));
            leaderBoard.setHighestuserfanemote((float) item.optDouble("totaldistance", 0));
        } else {
            leaderBoard.setTotaltapcount(item.optInt("totaltapcount", 0));
            leaderBoard.setTotalwavecount(item.optInt("totalwavecount", 0));
            leaderBoard.setTotalwhistleredeemed(item.optInt("totalwhistleredeemed", 0));
        }
        return leaderBoard;
    }
    /*
    {
  "response": {
    "leaderboard": [
      {
        "teamname": "CSK-IPL",
        "leaderboard": [
          {
            "sid": 11,
            "rank": 1,
            "name": "Arjun Prasad",
            "city": "Delhi",
            "points": 27505,
            "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/11_profile_image_1601903634497.png",
            "avguserfanemote": 3.8,
            "avguserhr": 75,
            "highestuserfanemote": 7.3,
            "totaltapcount": 41,
            "totalwavecount": 41,
            "totalwhistleredeemed": 12,
            "totalheartratecount": 50906,
            "affiliationid": 1
          },
          {
            "sid": 17,
            "rank": 2,
            "name": "Purple Priya",
            "city": "",
            "points": 8029,
            "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/17_profile_image_1602178303700.png",
            "avguserfanemote": 6.1,
            "avguserhr": 71,
            "highestuserfanemote": 7.5,
            "totaltapcount": 117,
            "totalwavecount": 29,
            "totalwhistleredeemed": 5,
            "totalheartratecount": 2734,
            "affiliationid": null
          },
          {
            "sid": 14,
            "rank": 3,
            "name": "James",
            "city": "",
            "points": 7751,
            "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/14_profile_image_1602072684254.png",
            "avguserfanemote": 5.7,
            "avguserhr": 80,
            "highestuserfanemote": 8.5,
            "totaltapcount": 21,
            "totalwavecount": 35,
            "totalwhistleredeemed": 13,
            "totalheartratecount": 3042,
            "affiliationid": 1
          }
        ],
        "userposition": {
          "sid": 14,
          "rank": 3,
          "name": "James",
          "city": "",
          "points": 7751,
          "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/14_profile_image_1602072684254.png",
          "avguserfanemote": 5.7,
          "avguserhr": 80,
          "highestuserfanemote": 8.5,
          "totaltapcount": 21,
          "totalwavecount": 35,
          "totalwhistleredeemed": 13,
          "totalheartratecount": 3042,
          "affiliationid": 1
        },
        "alltimeuserposition": {
          "sid": 14,
          "rank": 3,
          "name": "James",
          "city": "",
          "points": 7751,
          "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/14_profile_image_1602072684254.png",
          "avguserfanemote": 5.7,
          "avguserhr": 80,
          "highestuserfanemote": 8.5,
          "totaltapcount": 21,
          "totalwavecount": 35,
          "totalwhistleredeemed": 13,
          "totalheartratecount": 3042,
          "affiliationid": 1
        }
      }
    ]
  },
  "statuscode": 200,
  "message": "",
  "callstarttime": "2020-10-09 03:04",
  "callendtime": "2020-10-09 03:04"
}

// Global
{
  "response": {
    "leaderboard": {
      "leaderboard": [
        {
          "sid": 11,
          "rank": 1,
          "name": "Arjun Prasad",
          "city": "Delhi",
          "mobilenumber": "9494439322",
          "email": "arjun.prasad773922@gmail.com",
          "points": 46587,
          "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/11_profile_image_1603434967576.png",
          "avguserfanemote": 4.28,
          "avgfanfitscore": 2,
          "hearbeatcount": 0,
          "avguserhr": 76,
          "affiliationid": 462
        },
        {
          "sid": 55,
          "rank": 2,
          "name": "imran",
          "city": "",
          "mobilenumber": null,
          "email": "imran@gmail.com",
          "points": 42629,
          "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/common_dp.png",
          "avguserfanemote": 6.79,
          "avgfanfitscore": 6,
          "hearbeatcount": 25320,
          "avguserhr": 97,
          "affiliationid": null
        },
        {
          "sid": 81,
          "rank": 3,
          "name": "Vidhya Naren",
          "city": "Bengaluru",
          "mobilenumber": null,
          "email": null,
          "points": 25883,
          "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/common_dp.png",
          "avguserfanemote": 6.58,
          "avgfanfitscore": 9,
          "hearbeatcount": 598859,
          "avguserhr": 89,
          "affiliationid": null
        }
      ],
      "userposition": {
        "sid": 81,
        "rank": 3,
        "name": "Vidhya Naren",
        "city": "Bengaluru",
        "mobilenumber": null,
        "email": null,
        "points": 25883,
        "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/common_dp.png",
        "avguserfanemote": 6.58,
        "avgfanfitscore": 9,
        "hearbeatcount": 598859,
        "avguserhr": 89,
        "affiliationid": null
      }
    }
  },
  "statuscode": 200,
  "message": "",
  "callstarttime": "2020-12-23 07:37",
  "callendtime": "2020-12-23 07:37"
}

Fitness leaderboard

  "response": {
    "leaderboard": [
      {
        "teamname": "CSK-IPL",
        "leaderboard": [
          {
            "sid": 152,
            "rank": 2,
            "name": "Ramesh",
            "city": "",
            "mobilenumber": null,
            "email": null,
            "points": 25,
            "imgpath": "https://fangurudevstrg.blob.core.windows.net/userprofiles/common_dp.png",
            "totalsteps": 13484,
            "totalcalories": 361,
            "totaldistance": 7.23,
            "avgfanfitscore": 7,
            "avguserhr": 78,
            "affiliationid": null
          }
        ],
        "userposition": null,
        "alltimeuserposition": null
      }
    ]
  },
  "statuscode": 200,
  "message": "",
  "callstarttime": "2020-12-23 07:43",
  "callendtime": "2020-12-23 07:43"
}
     */
}
