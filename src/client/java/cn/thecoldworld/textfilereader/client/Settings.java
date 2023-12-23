package cn.thecoldworld.textfilereader.client;

import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class Settings {
    public transient boolean NeedUpdate;


    private boolean GuiAutoUpdateFileList;

    private int LinesPerPage;
    private boolean PauseGame;

    public Settings() {
        LinesPerPage = 10;
        GuiAutoUpdateFileList = false;
        NeedUpdate = true;
    }

    @Environment(EnvType.CLIENT)
    public static Settings GetSettings() {
        if (!cn.thecoldworld.textfilereader.client.variables.ClientConfigPath.toFile().exists() || !cn.thecoldworld.textfilereader.client.variables.ClientConfigPath.toFile().isFile()) {
            return new Settings();
        }
        try {
            Settings set = variables.defaultGson.fromJson(String.join("", Files.readAllLines(cn.thecoldworld.textfilereader.client.variables.ClientConfigPath)), Settings.class);
            return Objects.requireNonNullElseGet(set, Settings::new);
        } catch (IOException e) {
            return new Settings();
        }
    }

    public boolean isPauseGame() {
        return PauseGame;
    }

    public void setPauseGame(boolean pauseGame) {
        PauseGame = pauseGame;
    }

    public int getLinesPerPage() {
        return LinesPerPage;
    }

    public void setLinesPerPage(int linesPerPage) {
        LinesPerPage = linesPerPage;
        NeedUpdate = true;
    }

    public boolean isGuiAutoUpdateFileList() {
        return GuiAutoUpdateFileList;
    }

    public void setGuiAutoUpdateFileList(boolean guiAutoUpdateFileList) {
        GuiAutoUpdateFileList = guiAutoUpdateFileList;
        NeedUpdate = true;
    }

    public void UptoFile() throws IOException {
        if (!NeedUpdate) return;
        variables.Log.debug(this.getClass().getCanonicalName() + "Updating");
        FileWriter fp = new FileWriter(cn.thecoldworld.textfilereader.client.variables.ClientConfigPath.toFile(), StandardCharsets.UTF_8, false);
        fp.write(variables.defaultGson.toJson(this));
        fp.flush();
        fp.close();
        NeedUpdate = false;
    }
}
