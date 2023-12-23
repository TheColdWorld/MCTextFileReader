package cn.thecoldworld.textfilereader.client;

import cn.thecoldworld.textfilereader.client.screen.MainGUI;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindings.inGameGuiBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.filereader.gui.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.filereader.gui.open"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeyBindings.inGameGuiBinding.wasPressed()) {
                client.setScreen(new MainGUI(client.currentScreen));
            }
        });
        cFunctions.RegisterNetworkReceivers(
                variables.Identifiers.TextFileListNetworkingIdentifier,
                variables.Identifiers.DebugFileIdentifier,
                variables.Identifiers.TextFileNetworkingIdentifier
        );
        cn.thecoldworld.textfilereader.client.variables.ClientConfigPath = Paths.get(System.getProperty("user.dir"), "config", "TextFileReader.client.json").toAbsolutePath().normalize();
        cn.thecoldworld.textfilereader.client.variables.ClientModSettings = Settings.GetSettings();
        variables.IsClient = true;
        variables.scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (variables.IsClient && cn.thecoldworld.textfilereader.client.variables.ClientModSettings != null) {
                try {
                    cn.thecoldworld.textfilereader.client.variables.ClientModSettings.UptoFile();
                } catch (IOException e) {
                    variables.Log.error("", e);
                }
            }
        }, 0, 500, TimeUnit.MICROSECONDS);
    }
}