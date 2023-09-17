package cn.thecoldworld.textfilereader;


import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class cfileloader {
    public  static void init(CommandDispatcher<FabricClientCommandSource> dispatcher)
    {
        mod.Log.info("Loading client command: /cFileReader");
        dispatcher.register(ClientCommandManager.literal("cFileLoader")
        );
    }
}
