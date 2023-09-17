package cn.thecoldworld.textfilereader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Logger Log = LoggerFactory.getLogger("textfilereader");
        Log.info("Start Initialize TextFileReader client mod");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> cfileloader.init(dispatcher));
        Log.info("Initialize TextFileReader client mod done");
    }
}
