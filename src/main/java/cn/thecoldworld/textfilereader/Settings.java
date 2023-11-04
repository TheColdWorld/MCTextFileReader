package cn.thecoldworld.textfilereader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;


public class Settings {
    @Expose
    public final boolean Segmentedoutput;
    @Expose
    public final boolean RemoveInvalidFile;
    public transient boolean NeedUpdate;

    public Settings() {
        Segmentedoutput = false;
        RemoveInvalidFile = false;
        NeedUpdate = true;
    }

    public static Settings GetSettings() {
        if ( !FileIO.ConfigPath.toFile().exists() || !FileIO.ConfigPath.toFile().isFile() ) {
            return new Settings();
        }
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        try {
            Settings set = gson.fromJson(String.join("", Files.readAllLines(FileIO.ConfigPath)), Settings.class);
            return Objects.requireNonNullElseGet(set, Settings::new);
        } catch (IOException e) {
            return new Settings();
        }
    }

    public void UptoFile() throws IOException {
        if ( !NeedUpdate ) return;
        variables.Log.debug(this.getClass().getCanonicalName() + "Updating");
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        FileWriter fp = new FileWriter(FileIO.ConfigPath.toFile(), StandardCharsets.UTF_8, false);
        fp.write(gson.toJson(this));
        fp.flush();
        fp.close();
        NeedUpdate = false;
    }
}
