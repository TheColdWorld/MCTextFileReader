package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.networking.Tasks;
import cn.thecoldworld.textfilereader.screen.MainGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.LinkedList;

public class client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> cFileReader.init(dispatcher));
        KeyBindings.inGameGuiBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.filereader.gui.open", InputUtil.Type.SCANCODE, -1, "key.filereader.gui.open"
        ));
        Tasks.TaskPool_Client = new LinkedList<>();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( KeyBindings.inGameGuiBinding.wasPressed() ) {
                client.setScreen(new MainGUI(client.currentScreen));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(variables.Identifiers.TextFileNetworkingIdentifier,
                (client, handler, buf, responseSender) -> Tasks.GetNetPackageCallback(client, handler, buf, responseSender, variables.Identifiers.TextFileNetworkingIdentifier));
        ClientPlayNetworking.registerGlobalReceiver(variables.Identifiers.DebugFileIdentifier, (client, handler, buf, responseSender) -> Tasks.GetNetPackageCallback(client, handler, buf, responseSender, variables.Identifiers.DebugFileIdentifier));
    }
}
