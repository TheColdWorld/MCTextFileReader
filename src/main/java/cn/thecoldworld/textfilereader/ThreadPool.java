package cn.thecoldworld.textfilereader;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class ThreadPool implements ThreadFactory {

    @Override
    public final Thread newThread(@NotNull Runnable r) {
        Thread tr = new Thread(r, "TextFileReader-ThreadPool-Worker");
        tr.setDaemon(true);
        return tr;
    }
}
