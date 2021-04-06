package com.fanplayiot.core.background.io;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonFileIO {
    private static final String TAG = "FanEngagementFile";

    /**
     * Write list to Json file
     *
     * @param jsonFile file containing json
     * @param list     list of objects to write
     */
    public void writeJsonStream(File jsonFile, List<OfflineJson> list) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            writer.write(" ");
            writeArray(writer, list);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "error", e);
        }
    }

    private void writeArray(OutputStreamWriter writer, List<OfflineJson> list)
            throws IOException, JSONException {
        JSONArray jsonArray;
        if (list != null && list.size() > 0) {
            jsonArray = new JSONArray();
            for (OfflineJson entry : list) {
                jsonArray.put(new JSONObject(entry.getJsonString()));
            }
            writer.write(jsonArray.toString());
        }
    }

    /**
     * Read list from Json file
     *
     * @param jsonFile file to read list from
     * @return list of objects
     */
    public List<OfflineJson> readJsonStream(File jsonFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(jsonFile), StandardCharsets.UTF_8))) {
            if (reader.read() == -1) return null;
            return readArray(reader);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "error", e);
        }
        return null;
    }

    private List<OfflineJson> readArray(BufferedReader reader)
            throws IOException, JSONException {
        List<OfflineJson> list = new ArrayList<>();
        JSONArray jsonArray;
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            sb.append(str);
        }
        jsonArray = new JSONArray(sb.toString());
        for (int i = 0; i < jsonArray.length(); i++) {
            OfflineJson fe = new OfflineJson();
            fe.setJsonString(jsonArray.getJSONObject(i).toString());
            list.add(fe);
        }
        return list;
    }

    /**
     * Read JSON Array from Json file
     *
     * @param jsonFile file to read list from
     * @return list of objects
     */
    public JSONArray readJsonArray(File jsonFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(jsonFile), StandardCharsets.UTF_8))) {
            if (reader.read() == -1) return null;
            return readJSONArray(reader);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "error", e);
        }
        return null;
    }

    private JSONArray readJSONArray(BufferedReader reader)
            throws IOException, JSONException {
        JSONArray jsonArray;
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            sb.append(str);
        }
        jsonArray = new JSONArray(sb.toString());
        return jsonArray;
    }
}
