package cn.thecoldworld.textfilereader.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Path;

public class variables {
    @Environment(EnvType.CLIENT)
    public static Path ClientConfigPath = null;
    public static cn.thecoldworld.textfilereader.client.Settings ClientModSettings;
}
