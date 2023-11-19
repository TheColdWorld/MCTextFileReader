package cn.thecoldworld.textfilereader;

import com.google.gson.Gson;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

public abstract class variables {
    public static final Logger Log = LoggerFactory.getLogger("TextFileReader");
    public static final BlockingDeque<Runnable> TickEvent = new LinkedBlockingDeque<>();
    public static ScheduledExecutorService scheduledExecutorService = null;
    public static ThreadPool threadPool = null;
    public static boolean IsWorldLoaded = false;
    public static Settings ModSettings;

    public static Gson defaultGson;

    public abstract static class Identifiers {
        public static Identifier TextFileNetworkingIdentifier;
        public static Identifier DebugFileIdentifier;
    }
}
