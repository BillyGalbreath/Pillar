package net.pl3x.pillar.jenkins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public record Commit(String hash, long timestamp, String author, String email, String description) {

    public static Commit of(JSONObject json) {
        try {
            return new Commit(
                    json.getString("commitId").trim(),
                    json.getLong("timestamp"),
                    json.getJSONObject("author").getString("fullName").trim(),
                    json.getString("authorEmail").trim(),
                    json.getString("comment").trim()
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray toJson(List<Commit> commits) {
        JSONArray arr = new JSONArray();
        for (Commit commit : commits) {
            JSONObject obj = new JSONObject();
            obj.put("hash", commit.hash);
            obj.put("timestamp", commit.timestamp);
            obj.put("author", commit.author);
            obj.put("email", commit.email);
            obj.put("description", commit.description);
            arr.put(obj);
        }
        return arr;
    }
}
