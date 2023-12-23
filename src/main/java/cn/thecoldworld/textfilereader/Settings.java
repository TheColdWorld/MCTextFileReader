package cn.thecoldworld.textfilereader;

import com.google.gson.annotations.SerializedName;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;


public class Settings {
    public transient boolean NeedUpdate;
    private boolean RemoveInvalidFile;
    @SerializedName("Segmentedoutput")
    private boolean SegmentedOutput;
    private int Threads;

    public Settings() {
        SegmentedOutput = false;
        NeedUpdate = true;
    }

    public static Settings GetSettings() {
        if (!FileIO.ConfigPath.toFile().exists() || !FileIO.ConfigPath.toFile().isFile()) {
            return new Settings();
        }
        try {
            Settings set = variables.defaultGson.fromJson(String.join("", Files.readAllLines(FileIO.ConfigPath)), Settings.class);
            set.NeedUpdate = false;
            return Objects.requireNonNullElseGet(set, Settings::new);
        } catch (IOException e) {
            return new Settings();
        }
    }

    public int getThreads() {
        return Threads;
    }

    public void setThreads(int threads) {
        Threads = threads;
        this.NeedUpdate = true;
    }

    public void UptoFile() throws IOException {
        if (!NeedUpdate) return;
        variables.Log.debug(this.getClass().getCanonicalName() + "Updating");
        FileWriter fp = new FileWriter(FileIO.ConfigPath.toFile(), StandardCharsets.UTF_8, false);
        fp.write(variables.defaultGson.toJson(this));
        fp.flush();
        fp.close();
        NeedUpdate = false;
    }

    public boolean isSegmentedOutput() {
        return SegmentedOutput;
    }

    public void setSegmentedOutput(boolean segmentedOutput) {
        SegmentedOutput = segmentedOutput;
        NeedUpdate = true;
    }

    public boolean isRemoveInvalidFile() {
        return RemoveInvalidFile;
    }

    public void setRemoveInvalidFile(boolean removeInvalidFile) {
        RemoveInvalidFile = removeInvalidFile;
        NeedUpdate = true;
    }
}
