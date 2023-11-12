package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.screen.SettingGUI;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment (EnvType.CLIENT)
public class TextFileReaderModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new SettingGUI(MinecraftClient.getInstance().currentScreen);
    }
}
