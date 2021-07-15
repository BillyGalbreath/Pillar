package net.pl3x.pillar.jenkins;

import net.pl3x.pillar.Pillar;
import net.pl3x.pillar.data.Data;
import net.pl3x.pillar.util.FileUtil;
import net.pl3x.pillar.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Build(Pillar pillar, int build, long timestamp, long duration, String result, List<Commit> commits) {
    public static final Pattern MD5_PATTERN = Pattern.compile(">([a-z0-9]{32})<");

    public static Build get(Pillar pillar, int build) {
        JSONObject json = JSONUtil.readJsonFromUrl(pillar.settings().getBuildURL(build));
        if (json == null) {
            throw new IllegalStateException("Could not find build on Jenkins");
        }

        List<Commit> commits = new ArrayList<>();
        JSONArray arr = json.getJSONObject("changeSet").getJSONArray("items");
        for (int i = 0; i < arr.length(); i++) {
            Commit commit = Commit.of(arr.getJSONObject(i));
            if (commit != null) {
                commits.add(commit);
            }
        }

        return new Build(pillar, build, json.getLong("timestamp"), json.getLong("duration"), json.getString("result").trim(), commits);
    }

    public void download() {
        boolean downloaded = false;
        String md5 = "";
        if (this.result.equalsIgnoreCase("SUCCESS")) {
            System.out.print("Downloading " + pillar.project() + " build " + this.build + "...");
            pillar.version(FileUtil.download(pillar, this.build));
            try {
                String str = FileUtil.readFromUrl(pillar.settings().url() + "/" + this.build + "/artifact/" + pillar.settings().artifact()
                        .replace("{project}", pillar.project())
                        .replace("{version}", pillar.version())
                        .replace("{build}", Integer.toString(build))
                        + "/*fingerprint*/");
                if (str != null && !str.isBlank()) {
                    Matcher matcher = MD5_PATTERN.matcher(str);
                    if (matcher.find()) {
                        md5 = matcher.group(1);
                        downloaded = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.print("Build failed. Nothing to artifact.");
        }

        String buildStr = String.valueOf(this.build);
        JSONObject json = pillar.data().json();
        JSONObject project = json.has(pillar.project()) ? json.getJSONObject(pillar.project()) : new JSONObject();
        JSONObject version = project.has(pillar.version()) ? project.getJSONObject(pillar.version()) : new JSONObject();
        JSONObject build = project.has(buildStr) ? project.getJSONObject(buildStr) : new JSONObject();
        build.put("result", this.result);
        build.put("timestamp", this.timestamp);
        build.put("duration", this.duration);
        build.put("md5", md5);
        build.put("commits", Commit.toJson(this.commits));
        version.put(buildStr, build);
        project.put(pillar.version(), version);
        json.put(pillar.project(), project);

        if (downloaded) {
            int i = 0;
            while (true) {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    try {
                        File dir = FileUtil.getAndCreateDir("www/" + pillar.project() + "/" + pillar.version() + "/" + this.build);
                        md.update(Files.readAllBytes(new File(dir, pillar.project() + "-" + pillar.version() + "-" + this.build).toPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String chk = String.format("%1$32s", new BigInteger(1, md.digest()).toString(16)).replace(' ', '0');
                    if (chk.equalsIgnoreCase(md5)) {
                        break;
                    }
                    System.out.print(" Failed (" + i + ")");
                    System.out.println();
                    System.out.println(md5);
                    System.out.println(chk);
                    System.out.println("---");
                    FileUtil.download(pillar, this.build);
                } catch (NoSuchAlgorithmException e) {
                    System.out.println();
                    e.printStackTrace();
                    throw new IllegalStateException("Could not verify MD5 checksum");
                }
                if (++i >= 5) {
                    System.out.println();
                    throw new IllegalStateException("Downloaded build is corrupt. Tried 5 times...");
                }
            }
        }

        System.out.print(" Saving data...");
        FileUtil.write(FileUtil.getResourceFile(Data.FILENAME), json.toString());
        System.out.println(" Done.");
    }
}
