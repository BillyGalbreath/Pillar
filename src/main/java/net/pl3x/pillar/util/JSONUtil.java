package net.pl3x.pillar.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class JSONUtil {
    public static JSONObject readJsonFromResource(String filename) {
        File file = FileUtil.getResourceFile(filename);
        if (file == null) {
            throw new IllegalStateException("Could not find file " + filename);
        }

        JSONObject json = JSONUtil.readJsonFromFile(file);
        if (json == null) {
            throw new IllegalStateException("Could not read " + filename);
        }

        return json;
    }

    public static JSONObject readJsonFromFile(File file) {
        return toJson(FileUtil.readFromFile(file));
    }

    public static JSONObject readJsonFromUrl(String url) {
        return toJson(FileUtil.readFromUrl(url));
    }

    public static JSONObject toJson(String str) {
        try {
            if (str != null && !str.isBlank()) {
                return new JSONObject(str);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getObjectFromArray(String key, String value, JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getString(key).equals(value)) {
                return obj;
            }
        }
        return null;
    }
}
