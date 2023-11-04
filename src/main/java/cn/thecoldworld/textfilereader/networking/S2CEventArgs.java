package cn.thecoldworld.textfilereader.networking;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record S2CEventArgs(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
                           PacketSender responseSender, Identifier textFileIdentifier) {
}
