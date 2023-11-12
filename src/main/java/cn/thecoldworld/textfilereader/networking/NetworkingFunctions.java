package cn.thecoldworld.textfilereader.networking;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class NetworkingFunctions {
    public static void OnReceiveSedPackage(MinecraftServer server, ServerPlayerEntity player, Identifier identifier, SendNetworkPackage sendNetworkPackage) {
        if ( !sendNetworkPackage.NeedResponse ) return;
        if ( identifier.equals(Identifiers.DebugFileIdentifier) ) {
            JsonObject object = new JsonObject();
            object.addProperty("tmp", "ads");
            Tasks.Task.Run(player, object, sendNetworkPackage.ID, Identifiers.DebugFileIdentifier);
        }
        if ( identifier.equals(Identifiers.TextFileNetworkingIdentifier) ) {

        }
    }

    public static void OnReceiveSedPackage(MinecraftClient client, Identifier identifier, SendNetworkPackage sendNetworkPackage) {

    }
}
