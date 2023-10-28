package cn.thecoldworld.textfilereader;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

public abstract class variables {
    public static Logger Log = LoggerFactory.getLogger("TextFileReader");
    public static BlockingDeque<Runnable> TickEvent = new LinkedBlockingDeque<>();
    public static ScheduledExecutorService scheduledExecutorService=null;
    public static ThreadPool threadPool=null;
    public  static boolean IsWorldLoaded=false;
    public  static Settings ModSettings;

    public static Identifier TextFileIdentifier_receive;
    public static Identifier TextFileIdentifier_get;
}
