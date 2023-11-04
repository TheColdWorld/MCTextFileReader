package cn.thecoldworld.textfilereader.networking;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record C2SEventArgs(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                           PacketByteBuf buf, PacketSender responseSender, Identifier textFileIdentifier) {

}
