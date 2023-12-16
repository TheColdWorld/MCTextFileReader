package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.variables;
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

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class Events {
    @Environment(EnvType.CLIENT)
    public static _S2CPackageEvent S2CPackageEvent;
    public static _C2SPackageEvent C2SPackageEvent;

    public static final class _C2SPackageEvent {
        private final LinkedList<C2SPackageEventArg> EventHandle;

        public _C2SPackageEvent() {
            EventHandle = new LinkedList<>();
        }

        public _C2SPackageEvent Register(C2SPackageEventArg callback) {
            EventHandle.add(callback);
            return this;
        }

        public void Invoke(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
            EventHandle.forEach(i -> i.Invoke(server, player, handler, buf, responseSender, Identifier));
        }

        public void InvokeAsync(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
            EventHandle.forEach(i -> variables.scheduledExecutorService.schedule(() -> i.Invoke(server, player, handler, buf, responseSender, Identifier),0, TimeUnit.MICROSECONDS));
        }

        @FunctionalInterface
        public interface C2SPackageEventArg {
            void Invoke(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier);
        }
    }

    @Environment(EnvType.CLIENT)
    public static final class _S2CPackageEvent {
        private final LinkedList<S2CPackageEventArg> EventHandle;

        public _S2CPackageEvent() {
            EventHandle = new LinkedList<>();
        }

        public _S2CPackageEvent Register(S2CPackageEventArg callback) {
            EventHandle.add(callback);
            return this;
        }

        public void Invoke(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
                           PacketSender responseSender, Identifier Identifier) {
            EventHandle.forEach(i -> i.Invoke(client, handler, buf, responseSender, Identifier));
        }

        public void InvokeAsync(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
                                PacketSender responseSender, Identifier Identifier) {
            EventHandle.forEach(i -> variables.scheduledExecutorService.schedule(() -> i.Invoke(client, handler, buf, responseSender, Identifier),0,TimeUnit.MICROSECONDS));
        }

        @Environment(EnvType.CLIENT)
        @FunctionalInterface
        public interface S2CPackageEventArg {
            void Invoke(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier textFileIdentifier);
        }
    }
}
