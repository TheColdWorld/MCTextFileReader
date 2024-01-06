package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.api.SubMod;
import com.google.gson.Gson;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

public abstract class variables {
    public static final Logger Log = LoggerFactory.getLogger("TextFileReader");
    public static final BlockingDeque<Runnable> TickEvent = new LinkedBlockingDeque<>();
    public static final Gson defaultGson = new Gson();
    public static final ArrayList<SubMod> subMods = new ArrayList<>();
    public static final ThreadPool threadPool = new ThreadPool();
    public static ScheduledExecutorService scheduledExecutorService = null;
    public static boolean IsWorldLoaded = false;
    public static Settings ModSettings;
    public static boolean IsClient = false;

    public abstract static class Identifiers {
        public final static Identifier TextFileListNetworkingIdentifier = new Identifier("textfilereader", "networking/textfilelist");
        public final static Identifier TextFileNetworkingIdentifier = new Identifier("textfilereader", "networking/textfile");
        public final static Identifier ControlingIdentifier = new Identifier("textfilereader", "networking/controling");
    }
}
