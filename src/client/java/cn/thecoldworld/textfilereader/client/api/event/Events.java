package cn.thecoldworld.textfilereader.client.api.event;

import cn.thecoldworld.textfilereader.networking.SendNetworkPackage;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public abstract class Events {
    public static final class S2CPacketEvent {
        public static final S2CPacketEvent EVENT = new S2CPacketEvent();
        private final LinkedList<C2SPacketEventArg> actions;

        private S2CPacketEvent() {
            actions = new LinkedList<>();
        }

        public void Register(ClientPlayNetworking.PlayChannelHandler action, Identifier... NetworkingIdentifier) {
            for (Identifier i : NetworkingIdentifier) actions.add(new C2SPacketEventArg(i, action));
        }

        public void Invoke(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier identifier) {
            actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier)).forEach(i -> i.action.receive(client, handler, buf, responseSender));
        }

        public void InvokeAsync(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier identifier) {
            Stream<C2SPacketEventArg> stream = actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier));
            variables.scheduledExecutorService.schedule(() -> stream.forEach(i -> i.action.receive(client, handler, buf, responseSender)), 0, TimeUnit.MICROSECONDS);
        }

        private static final class C2SPacketEventArg {
            final Identifier NetworkingIdentifier;
            final ClientPlayNetworking.PlayChannelHandler action;

            public C2SPacketEventArg(Identifier networkingIdentifier, ClientPlayNetworking.PlayChannelHandler action) {
                NetworkingIdentifier = networkingIdentifier;
                this.action = action;
            }
        }
    }

    public static final class S2CSendPacketEvent {
        public static final S2CSendPacketEvent EVENT = new S2CSendPacketEvent();
        private final LinkedList<S2CSendPacketEventArg> actions;

        private S2CSendPacketEvent() {
            actions = new LinkedList<>();
        }

        public void Register(S2CSendPacketEventHandler action, Identifier... NetworkingIdentifier) {
            for (Identifier i : NetworkingIdentifier) actions.add(new S2CSendPacketEventArg(i, action));
        }

        public void Invoke(MinecraftClient client, ClientPlayNetworkHandler handler, SendNetworkPackage sendNetworkPackage, PacketSender responseSender, Identifier identifier) {
            actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier)).forEach(i -> i.action.receive(client, handler, sendNetworkPackage, responseSender, identifier));
        }

        public void InvokeAsync(MinecraftClient client, ClientPlayNetworkHandler handler, SendNetworkPackage sendNetworkPackage, PacketSender responseSender, Identifier identifier) {
            Stream<S2CSendPacketEventArg> stream = actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier));
            variables.scheduledExecutorService.schedule(() -> stream.forEach(i -> i.action.receive(client, handler, sendNetworkPackage, responseSender, identifier)), 0, TimeUnit.MICROSECONDS);
        }

        @FunctionalInterface
        public interface S2CSendPacketEventHandler {
            void receive(MinecraftClient client, ClientPlayNetworkHandler handler, SendNetworkPackage sendNetworkPackage, PacketSender responseSender, Identifier identifier);
        }

        private static final class S2CSendPacketEventArg {
            final Identifier NetworkingIdentifier;
            final S2CSendPacketEventHandler action;

            public S2CSendPacketEventArg(Identifier networkingIdentifier, S2CSendPacketEventHandler action) {
                NetworkingIdentifier = networkingIdentifier;
                this.action = action;
            }
        }
    }
}
