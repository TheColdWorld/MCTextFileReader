package cn.thecoldworld.textfilereader.client.networking;

import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class Events {
    @Environment(EnvType.CLIENT)
    public static _S2CPackageEvent S2CPackageEvent;

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
            EventHandle.forEach(i -> variables.scheduledExecutorService.schedule(() -> i.Invoke(client, handler, buf, responseSender, Identifier), 0, TimeUnit.MICROSECONDS));
        }

        @Environment(EnvType.CLIENT)
        @FunctionalInterface
        public interface S2CPackageEventArg {
            void Invoke(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier textFileIdentifier);
        }
    }
}
