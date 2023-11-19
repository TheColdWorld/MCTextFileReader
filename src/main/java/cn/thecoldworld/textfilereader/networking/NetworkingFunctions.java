package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.FileIO;
import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.networking.jsonformats.C2SGetContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.FailedContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2CGetContent;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class NetworkingFunctions {
    public static void OnReceiveSedPackage(MinecraftServer server, ServerPlayerEntity player, Identifier identifier, SendNetworkPackage sendNetworkPackage) {
        if ( !sendNetworkPackage.NeedResponse ) return;
        if ( identifier.equals(variables.Identifiers.DebugFileIdentifier) ) {
            JsonObject object = new JsonObject();
            object.addProperty("tmp", "ads");
            Tasks.Task.Run(player, object, sendNetworkPackage.ID, variables.Identifiers.DebugFileIdentifier);
        }
        if ( identifier.equals(variables.Identifiers.TextFileNetworkingIdentifier) ) {
            try {
                if ( C2SGetContent.IsInstance(sendNetworkPackage.Body.toString()) ) {
                    String fileName = sendNetworkPackage.Body.get("FileName").getAsString();
                    FileSource fileSource = switch (sendNetworkPackage.Body.get("fileSource").getAsString().toLowerCase()) {
                        case "global" -> FileSource.global;
                        case "save" -> FileSource.save;
                        default ->
                                throw new IllegalStateException("Unexpected value: " + sendNetworkPackage.Body.get("fileSource").getAsString().toLowerCase());
                    };
                    if ( cn.thecoldworld.textfilereader.filereader.HavePermission(player, fileName, fileSource) ) {
                        Tasks.Task.Run(player,
                                new S2CGetContent(FileIO.GetFileContent(fileName, fileSource, server)).ToJsonObject(),
                                sendNetworkPackage.ID, variables.Identifiers.TextFileNetworkingIdentifier);
                    } else Tasks.Task.Run(player,
                            new FailedContent(Text.translatable("text.filereader.printfile.nopermission", fileName).asTruncatedString(Integer.MAX_VALUE)).ToJsonObject(),
                            sendNetworkPackage.ID, variables.Identifiers.TextFileNetworkingIdentifier);
                }
            } catch (Exception e) {
                Tasks.Task.Run(player,
                        new FailedContent(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()).asTruncatedString(Integer.MAX_VALUE)).ToJsonObject(),
                        sendNetworkPackage.ID, variables.Identifiers.TextFileNetworkingIdentifier);
            }

        }
    }

    public static void OnReceiveSedPackage(MinecraftClient client, Identifier identifier, SendNetworkPackage sendNetworkPackage) {

    }
}
