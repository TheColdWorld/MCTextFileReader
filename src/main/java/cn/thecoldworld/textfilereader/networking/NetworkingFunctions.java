package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.FileIO;
import cn.thecoldworld.textfilereader.ServerFileSource;
import cn.thecoldworld.textfilereader.api.event.Events;
import cn.thecoldworld.textfilereader.api.funcitons;
import cn.thecoldworld.textfilereader.exceptions.TranslatableException;
import cn.thecoldworld.textfilereader.networking.jsonformats.*;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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
        if (identifier.equals(variables.Identifiers.TextFileListNetworkingIdentifier)) {
            if (C2SGetFileList.IsInstance(sendNetworkPackage.Body.toString())) {
                try {
                    List<String> _Files = funcitons.GetFileList(server, player, new C2SGetFileList(sendNetworkPackage.Body).GetFileSource());
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
                    ServerFileSource serverFileSource = switch (sendNetworkPackage.Body.get("ListFileSource").getAsString().toLowerCase()) {
                        case "global" -> ServerFileSource.global;
                        case "save" -> ServerFileSource.save;
                        default ->
                                throw new IllegalStateException("Unexpected value: " + sendNetworkPackage.Body.get("ListFileSource").getAsString().toLowerCase());
                    };
                    if (cn.thecoldworld.textfilereader.FilePermissions.HavePermission(player, fileName, serverFileSource)) {
                        ServerNetWorkingTask.Run(
                                new S2CGetFileContent(FileIO.GetFileContent(fileName, serverFileSource, server)).ToJsonObject(),
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

    public static void GetNetPackageCallback(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
        if (ResponseNetworkPackage.IsResponse(buf, StandardCharsets.UTF_8)) {
            ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
            if (responseNetworkPackage == null || responseNetworkPackage.ResponseID == null)
                return;
            funcitons.AutoRemoveGetItemFromStream(ServerNetWorkingTask.TaskPool,
                            t -> t.isReturned() && t.PackageID.equals(responseNetworkPackage.ResponseID))
                    .forEach(i -> i.Return(new C2SArguments(responseNetworkPackage.Body, server, player)));
        } else if (SendNetworkPackage.IsSendPackage(buf, StandardCharsets.UTF_8)) {
            Events.C2SSendPacketEvent.EVENT.Invoke(server, player, handler, SendNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8), responseSender, Identifier);
        } else {
            Events.C2SPacketEvent.EVENT.Invoke(server, player, handler, buf, responseSender, Identifier);
        }
    }

}
