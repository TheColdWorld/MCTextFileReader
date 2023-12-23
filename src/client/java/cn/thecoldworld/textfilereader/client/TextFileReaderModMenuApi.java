package cn.thecoldworld.textfilereader.client;

import cn.thecoldworld.textfilereader.client.screen.ClientSettingGUI;
import cn.thecoldworld.textfilereader.client.screen.ServerSettingsGUI;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class TextFileReaderModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ClientSettingGUI(MinecraftClient.getInstance().currentScreen);
    }
}