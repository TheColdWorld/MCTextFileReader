package cn.thecoldworld.textfilereader;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class FileIO {
    public static Path Rootdir = null;
    public static Path GlobalTextPath = null;
    public static Path ConfigPath = null;

    public static int PrintFile(Entity entity, World world, String Filename, FileSource fileSource) throws IOException {
        if ( world.isClient ) return -1;
        boolean Segmentedoutput = mod.GetConfig("Segmentedoutput").getAsBoolean();
        Scanner fp;
        switch (fileSource) {
            case save -> {
                fp = new Scanner(Paths.get(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", Filename), StandardCharsets.UTF_8);
            }
            case global -> {
                fp = new Scanner(Paths.get(GlobalTextPath.toString(), Filename), StandardCharsets.UTF_8);
            }
            default -> throw new IOException("Internal error");
        }
        if ( Segmentedoutput ) {
            entity.sendMessage(Text.translatable("text.filereader.printfile", Filename, ""));
            while (fp.hasNext()) {
                entity.sendMessage(Text.literal(fp.nextLine()));
            }
        } else {
            StringBuilder sb = new StringBuilder();
            while (fp.hasNext()) {
                sb.append(fp.nextLine()).append('\n');
            }
            entity.sendMessage(Text.translatable("text.filereader.printfile", Filename, "\n" + sb.toString()));
        }
        fp.close();
        return 0;
    }
}
