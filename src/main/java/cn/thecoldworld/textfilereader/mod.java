package cn.thecoldworld.textfilereader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class mod implements ModInitializer {
    public static Logger Log = LoggerFactory.getLogger("TextFileReader");
    @Override
    public void onInitialize() {
        Log.info("Start Initialize TextFileReader mod");
        FileIO.Rootdir= Paths.get(System.getProperty("user.dir"));
        if(!FileIO.Rootdir.isAbsolute()) throw new RuntimeException("Cannot get Data path");
        FileIO.GlobalTextPath=Paths.get(FileIO.Rootdir.toString(),"Texts");
        if(!FileIO.GlobalTextPath.toFile().exists() || !FileIO.GlobalTextPath.toFile().isDirectory())
        {
            try {
                Log.info("Cannot find global text data directory,starting crate");
                Files.createDirectory(FileIO.GlobalTextPath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create global text data directory ,exception message:"+e.getMessage());
            }
        }
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> filereader.Init(dispatcher)));
        Log.info("Initialize TextFileReader mod done");
    }
}
