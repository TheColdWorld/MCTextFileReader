package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.screen.SettingsGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Logger Log = LoggerFactory.getLogger("textfilereader");
        Log.info("Start Initialize TextFileReader client mod");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> cfileloader.init(dispatcher));
        KeyBindings.inGameGuiBinding= KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.filereader.gui.open", InputUtil.Type.SCANCODE, -1,"key.filereader.gui.open"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(KeyBindings.inGameGuiBinding.wasPressed())
            {
                client.setScreen(new SettingsGUI(client.currentScreen));
            }
        });
        Log.info("Initialize TextFileReader client mod done");
    }
}
