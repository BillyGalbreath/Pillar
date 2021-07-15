package net.pl3x.pillar.data;

import net.pl3x.pillar.util.JSONUtil;
import org.json.JSONObject;

public record Settings(String project, String url, String artifact) {
    public static final String FILENAME = "settings.json";

    public static Settings get(String project) {
        JSONObject json = JSONUtil.readJsonFromResource(FILENAME);

        JSONObject obj = JSONUtil.getObjectFromArray("name", project, json.getJSONArray("projects"));
        if (obj == null) {
            throw new IllegalStateException("No project `" + project + "` in " + FILENAME);
        }

        return new Settings(project, obj.getString("url"), obj.getString("artifact"));
    }

    public String getBuildURL(int build) {
        return url + "/" + build + "/api/json";
    }

    public String getAllBuilds() {
        return url + "/api/json?tree=allBuilds[id]";
    }
}
