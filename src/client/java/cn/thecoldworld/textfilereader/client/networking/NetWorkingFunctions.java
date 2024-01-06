package cn.thecoldworld.textfilereader.client.networking;

import cn.thecoldworld.textfilereader.api.funcitons;
import cn.thecoldworld.textfilereader.client.api.event.Events;
import cn.thecoldworld.textfilereader.networking.ResponseNetworkPackage;
import cn.thecoldworld.textfilereader.networking.SendNetworkPackage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public abstract class NetWorkingFunctions {
    @Environment(EnvType.CLIENT)
    public static void OnReceiveSedPackage(MinecraftClient client, Identifier identifier, SendNetworkPackage sendNetworkPackage) {

    }

    @Environment(EnvType.CLIENT)
    public static void GetNetPackageCallback(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
        if (ResponseNetworkPackage.IsResponse(buf, StandardCharsets.UTF_8)) {
            ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
            if (responseNetworkPackage == null || responseNetworkPackage.ResponseID == null)
                return;
            funcitons.AutoRemoveGetItemFromStream(ClientNetWorkingTask.TaskPool,
                            t -> !t.isReturned() && t.PackageID.equals(responseNetworkPackage.ResponseID))
                    .forEach(i -> i.Return(new S2CArguments(client, responseNetworkPackage.Body)));
        } else if (SendNetworkPackage.IsSendPackage(buf, StandardCharsets.UTF_8)) {
            Events.S2CSendPacketEvent.EVENT.Invoke(client, handler, SendNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8), responseSender, Identifier);
        } else {
            Events.S2CPacketEvent.EVENT.Invoke(client, handler, buf, responseSender, Identifier);
        }
    }
}
