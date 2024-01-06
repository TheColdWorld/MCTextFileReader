package cn.thecoldworld.textfilereader.api.event;

import cn.thecoldworld.textfilereader.networking.SendNetworkPackage;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public abstract class Events {
    public static final class C2SPacketEvent {
        public static final C2SPacketEvent EVENT = new C2SPacketEvent();
        private final LinkedList<C2SPacketEventArg> actions;

        private C2SPacketEvent() {
            actions = new LinkedList<>();
        }

        public void Register(ServerPlayNetworking.PlayChannelHandler action, Identifier... NetworkingIdentifier) {
            for (Identifier i : NetworkingIdentifier) actions.add(new C2SPacketEventArg(i, action));
        }

        public void Invoke(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier identifier) {
            actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier)).forEach(i -> i.action.receive(server, player, handler, buf, responseSender));
        }

        public void InvokeAsync(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier identifier) {
            Stream<C2SPacketEventArg> stream = actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier));
            variables.scheduledExecutorService.schedule(() -> stream.forEach(i -> i.action.receive(server, player, handler, buf, responseSender)), 0, TimeUnit.MICROSECONDS);
        }

        private static final class C2SPacketEventArg {
            final Identifier NetworkingIdentifier;
            final ServerPlayNetworking.PlayChannelHandler action;

            public C2SPacketEventArg(Identifier networkingIdentifier, ServerPlayNetworking.PlayChannelHandler action) {
                NetworkingIdentifier = networkingIdentifier;
                this.action = action;
            }
        }
    }

    public static final class C2SSendPacketEvent {
        public static final C2SSendPacketEvent EVENT = new C2SSendPacketEvent();
        private final LinkedList<C2SSendPacketEventArg> actions;

        private C2SSendPacketEvent() {
            actions = new LinkedList<>();
        }

        public void Register(C2SSendPacketEventAction action, Identifier... NetworkingIdentifier) {
            for (Identifier i : NetworkingIdentifier) actions.add(new C2SSendPacketEventArg(i, action));
        }

        public void Invoke(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, SendNetworkPackage sendNetworkPackage, PacketSender responseSender, Identifier identifier) {
            actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier)).forEach(i -> i.action.receive(server, player, handler, sendNetworkPackage, responseSender, identifier));
        }

        public void InvokeAsync(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, SendNetworkPackage sendNetworkPackage, PacketSender responseSender, Identifier identifier) {
            Stream<C2SSendPacketEventArg> stream = actions.stream().filter(i -> i.NetworkingIdentifier.equals(identifier));
            variables.scheduledExecutorService.schedule(() -> stream.forEach(i -> i.action.receive(server, player, handler, sendNetworkPackage, responseSender, identifier)), 0, TimeUnit.MICROSECONDS);
        }

        @FunctionalInterface
        public interface C2SSendPacketEventAction {
            void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, SendNetworkPackage sendNetworkPackage, PacketSender responseSender, Identifier identifier);
        }

        private static final class C2SSendPacketEventArg {
            final Identifier NetworkingIdentifier;
            final C2SSendPacketEventAction action;

            public C2SSendPacketEventArg(Identifier networkingIdentifier, C2SSendPacketEventAction action) {
                NetworkingIdentifier = networkingIdentifier;
                this.action = action;
            }
        }
    }

}
