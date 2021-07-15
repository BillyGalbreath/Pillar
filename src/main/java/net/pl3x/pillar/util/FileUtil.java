package net.pl3x.pillar.util;

import net.pl3x.pillar.Main;
import net.pl3x.pillar.Pillar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
    public static File baseDir() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
    }

    public static File getAndCreateDir(String path) {
        try {
            File dir = new File(FileUtil.baseDir(), path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IllegalStateException("Could not create directory " + dir.getPath());
                }
            }
            return dir;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getResourceFile(String filename) {
        return getResourceFile(filename, null);
    }

    public static File getResourceFile(String filename, String dirname) {
        try {
            File dir = dirname == null ? FileUtil.baseDir() : new File(FileUtil.baseDir(), dirname);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IllegalStateException("Could not create directory " + dir.getAbsolutePath());
                }
            }
            File file = new File(dir, filename);
            if (!file.exists()) {
                InputStream source = Main.class.getResourceAsStream("/" + filename);
                if (source != null) {
                    Files.copy(source, file.toPath());
                }
            }
            return file;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFromFile(File file) {
        try {
            return readAll(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFromUrl(String url) {
        try (InputStream stream = new URL(url).openStream()) {
            InputStreamReader is = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(is);
            return readAll(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void write(File file, String str) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String download(Pillar pillar, int build) {
        File tmp = FileUtil.getAndCreateDir("www/tmp");
        String url = pillar.settings().url() + "/" + build + "/artifact/" + pillar.settings().artifact()
                .replace("{project}", pillar.project())
                .replace("{version}", pillar.version())
                .replace("{build}", Integer.toString(build));
        try (InputStream in = URI.create(url).toURL().openStream()) {
            File file = new File(tmp, pillar.project());
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String version = getVersion(file);
            File dir = FileUtil.getAndCreateDir("www/" + pillar.project() + "/" + version + "/" + build);
            Files.move(file.toPath(), new File(dir, pillar.project() + "-" + version + "-" + build).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return version;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    private static String getVersion(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            //ZipEntry entry = zipFile.getEntry("version.json");
            ZipEntry entry = zipFile.getEntry("patch.properties");
            Properties props = new Properties();
            //String str;
            try (InputStream stream = zipFile.getInputStream(entry)) {
                props.load(stream);
                //InputStreamReader is = new InputStreamReader(stream, StandardCharsets.UTF_8);
                //str = readAll(new BufferedReader(is));
            }
            return props.getProperty("version");
            //return JSONUtil.toJson(str).getString("release_target");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = rd.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }
}
