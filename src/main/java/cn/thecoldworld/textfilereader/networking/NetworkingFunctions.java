package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.FileIO;
import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.exceptions.TranslatableException;
import cn.thecoldworld.textfilereader.networking.jsonformats.*;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class NetworkingFunctions {
    public static void OnReceiveSedPackage(MinecraftServer server, ServerPlayerEntity player, Identifier identifier, SendNetworkPackage sendNetworkPackage) {
        if (!sendNetworkPackage.NeedResponse) return;
        if (identifier.equals(variables.Identifiers.DebugFileIdentifier)) {
            JsonObject object = new JsonObject();
            object.addProperty("tmp", "ads");
            ServerNetWorkingTask.Run(object, sendNetworkPackage.ID, player, variables.Identifiers.DebugFileIdentifier);
        }
        if (identifier.equals(variables.Identifiers.TextFileListNetworkingIdentifier)) {
            if (C2SGetFileList.IsInstance(sendNetworkPackage.Body.toString())) {
                try {
                    List<String> _Files = cn.thecoldworld.textfilereader.filereader.GetFileList(server, player, new C2SGetFileList(sendNetworkPackage.Body).GetFileSource());
                    ServerNetWorkingTask.Run(
                            new S2CGetFileList(_Files).ToJsonObject(),
                            sendNetworkPackage.ID,
                            player,
                            variables.Identifiers.TextFileListNetworkingIdentifier
                    );
                } catch (TranslatableException tre) {
                    ServerNetWorkingTask.Run(
                            new FailedContent(tre.TranslateKey).ToJsonObject(),
                            sendNetworkPackage.ID,
                            player,
                            variables.Identifiers.TextFileListNetworkingIdentifier
                    );
                } catch (Exception e) {
                    ServerNetWorkingTask.Run(
                            new FailedContent("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()).ToJsonObject(),
                            sendNetworkPackage.ID,
                            player,
                            variables.Identifiers.TextFileListNetworkingIdentifier
                    );
                }
            }
        }
        if (identifier.equals(variables.Identifiers.TextFileNetworkingIdentifier)) {
            try {
                if (C2SGetFileContent.IsInstance(sendNetworkPackage.Body.toString())) {
                    String fileName = sendNetworkPackage.Body.get("FileName").getAsString();
                    FileSource fileSource = switch (sendNetworkPackage.Body.get("ListFileSource").getAsString().toLowerCase()) {
                        case "global" -> FileSource.global;
                        case "save" -> FileSource.save;
                        default ->
                                throw new IllegalStateException("Unexpected value: " + sendNetworkPackage.Body.get("ListFileSource").getAsString().toLowerCase());
                    };
                    if (cn.thecoldworld.textfilereader.filereader.HavePermission(player, fileName, fileSource)) {
                        ServerNetWorkingTask.Run(
                                new S2CGetFileContent(FileIO.GetFileContent(fileName, fileSource, server)).ToJsonObject(),
                                sendNetworkPackage.ID,
                                player,
                                variables.Identifiers.TextFileNetworkingIdentifier
                        );
                    } else
                        ServerNetWorkingTask.Run(
                                new FailedContent("text.filereader.printfile.nopermission", fileName).ToJsonObject(),
                                sendNetworkPackage.ID,
                                player,
                                variables.Identifiers.TextFileNetworkingIdentifier
                        );
                }
            } catch (Exception e) {
                ServerNetWorkingTask.Run(
                        new FailedContent("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()).ToJsonObject(),
                        sendNetworkPackage.ID,
                        player,
                        variables.Identifiers.TextFileNetworkingIdentifier
                );
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static void OnReceiveSedPackage(MinecraftClient client, Identifier identifier, SendNetworkPackage sendNetworkPackage) {

    }

    public static void GetNetPackageCallback(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
        if (ResponseNetworkPackage.IsResponse(buf, StandardCharsets.UTF_8)) {
            ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
            if (responseNetworkPackage == null || responseNetworkPackage.ResponseID == null)
                return;
            cn.thecoldworld.textfilereader.funcitons.AutoRemoveGetItemFromStream(ServerNetWorkingTask.TaskPool,
                            t -> t.isReturned() && t.PackageID.equals(responseNetworkPackage.ResponseID))
                    .forEach(i -> i.Return(new C2SArguments(responseNetworkPackage.Body, server, player)));
        } else if (SendNetworkPackage.IsSendPackage(buf, StandardCharsets.UTF_8)) {
            OnReceiveSedPackage(server, player, Identifier, SendNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8));
        } else {
            Events.C2SPackageEvent.InvokeAsync(server, player, handler, buf, responseSender, Identifier);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void GetNetPackageCallback(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
        if (ResponseNetworkPackage.IsResponse(buf, StandardCharsets.UTF_8)) {
            ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
            if (responseNetworkPackage == null || responseNetworkPackage.ResponseID == null)
                return;
            cn.thecoldworld.textfilereader.funcitons.AutoRemoveGetItemFromStream(ClientNetWorkingTask.TaskPool,
                            t -> !t.isReturned() && t.PackageID.equals(responseNetworkPackage.ResponseID))
                    .forEach(i -> i.Return(new S2CArguments(client, responseNetworkPackage.Body)));

        } else if (SendNetworkPackage.IsSendPackage(buf, StandardCharsets.UTF_8)) {
            OnReceiveSedPackage(client, Identifier, SendNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8));
        } else {
            Events.S2CPackageEvent.InvokeAsync(client, handler, buf, responseSender, Identifier);
        }

    }
}
