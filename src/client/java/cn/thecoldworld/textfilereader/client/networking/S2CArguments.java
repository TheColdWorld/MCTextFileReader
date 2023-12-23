package cn.thecoldworld.textfilereader.client.networking;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class S2CArguments {
    public final MinecraftClient client;
    public final JsonObject value;

    public S2CArguments(MinecraftClient client, JsonObject value) {
        this.client = client;
        this.value = value;
    }
}
