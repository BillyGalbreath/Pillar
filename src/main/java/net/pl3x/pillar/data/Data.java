package net.pl3x.pillar.data;

import net.pl3x.pillar.util.JSONUtil;
import org.json.JSONObject;

public record Data(JSONObject json) {
    public static final String FILENAME = "data.json";

    public static Data get() {
        return new Data(JSONUtil.readJsonFromResource(FILENAME));
    }
}
