package cn.thecoldworld.textfilereader.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

@Environment(EnvType.CLIENT)
public abstract class KeyBindings {
    public static KeyBinding inGameGuiBinding;
}
