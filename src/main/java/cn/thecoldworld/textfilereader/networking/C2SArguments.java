package cn.thecoldworld.textfilereader.networking;

import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class C2SArguments {
    public final JsonObject value;
    public final MinecraftServer server;
    public final ServerPlayerEntity Sender;

    public C2SArguments(JsonObject value, MinecraftServer server, ServerPlayerEntity sender) {
        this.value = value;
        this.server = server;
        Sender = sender;
    }
}
