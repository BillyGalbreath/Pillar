package net.pl3x.pillar;

import net.pl3x.pillar.data.Data;
import net.pl3x.pillar.data.Settings;
import net.pl3x.pillar.jenkins.Build;
import net.pl3x.pillar.util.FileUtil;
import net.pl3x.pillar.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record Pillar(String project, String version, int build, Settings settings, Data data) {

    public Pillar(String project, String version, int build) {
        this(project, version, build, Settings.get(project), Data.get());
        FileUtil.getResourceFile("index.php", "www");
    }

    public void run() {
        if (this.version.equalsIgnoreCase("scrape")) {
            scrape();
        } else if (this.version.equalsIgnoreCase("verify")) {
            List<Integer> list = new ArrayList<>();
            List<String> list2 = new ArrayList<>();
            Map<String, List<String>> map = new TreeMap<>(Collections.reverseOrder());
            JSONObject versionsJson = this.data.json().getJSONObject("purpur");
            Iterator<String> versionKeys = versionsJson.keys();
            while (versionKeys.hasNext()) {
                String version = versionKeys.next();
                List<String> builds = map.getOrDefault(version, new ArrayList<>());
                JSONObject buildsJson = versionsJson.getJSONObject(version);
                Iterator<String> buildKeys = buildsJson.keys();
                while(buildKeys.hasNext()) {
                    String build = buildKeys.next();
                    list.add(Integer.parseInt(build));
                    JSONObject json = buildsJson.getJSONObject(build);

                    String result = json.getString("result");
                    String md5 = json.getString("md5");
                    if (result.equals("SUCESS") && (md5 == null || md5.isBlank())) {
                        list2.add(build);
                    }

                    builds.add(String.format("%1$4s", build).replace(' ', '0') + " " + result + " " + md5);
                }
                builds.sort(Collections.reverseOrder());
                map.put(version, builds);
            }
            map.forEach((version, builds) -> {
                System.out.println("version: " + version);
                builds.forEach((build) -> {
                    System.out.println("  build: " + build);
                });
            });
            System.out.println("Missing builds:");
            for (int i = 1265; i > 0; i--) {
                if (!list.contains(i)) {
                    System.out.println("  " + i);
                }
            }
            System.out.println("Missing md5:");
            list2.forEach((build) -> {
                System.out.println("  " + build);
            });
        } else if (build > 0) {
            Build build = Build.get(this, this.build);
            build.download();
        } else {
            throw new IllegalArgumentException("Invalid command");
        }
    }

    public void scrape() {
        System.out.println("Scrapping all builds for " + project);
        System.out.print("Please wait...");

        JSONObject json = JSONUtil.readJsonFromUrl(settings.getAllBuilds());
        if (json == null) {
            throw new IllegalStateException("Could not find any builds on Jenkins");
        }

        JSONArray arr = json.getJSONArray("allBuilds");

        List<Integer> builds = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            builds.add(Integer.parseInt(obj.getString("id")));
        }

        System.out.println(" Done.");

        System.out.println("Found " + builds.size() + " builds");

        System.out.println("Adding all builds");

        for (Integer build : builds) {
            //if (build < 5)
            Build.get(this, build).download();
        }
    }

    private static String lastVersion;

    public void version(String version) {
        lastVersion = version;
    }

    public String version() {
        return lastVersion == null ? this.version : lastVersion;
    }
}
