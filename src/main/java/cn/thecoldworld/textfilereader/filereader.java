package cn.thecoldworld.textfilereader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class filereader {
    public static void Init(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        mod.Log.info("Loading server command: /FileReader");
        dispatcher.register(CommandManager.literal("FileReader")
                .then(CommandManager.literal("Permission").requires(src->src.hasPermissionLevel(2)))
                .then(CommandManager.literal("File")
                        .then(CommandManager.literal("Get"))
                        .then(CommandManager.literal("List")
                                .then(CommandManager.literal("Global").executes(i->PrintFileList(i,FileSource.global)))
                                .then(CommandManager.literal("World").executes(i->PrintFileList(i,FileSource.save)))
                        .then(CommandManager.argument("FileName", StringArgumentType.string()))))
                .executes(i->{
                    i.getSource().sendMessage(Text.literal("TextFileReader"));
                    i.getSource().sendMessage(Text.translatable("filereader.description"));
                    return 0;})
        );
    }
    public  static int PrintFileList(CommandContext<ServerCommandSource> context,FileSource fileSource) throws CommandSyntaxException
    {
        try {
            Path Path;
            StringBuilder sb=new StringBuilder();
            if(fileSource == FileSource.save) Path= Paths.get(context.getSource().getWorld().getServer().getSavePath(WorldSavePath.ROOT).getParent().toString(),"Texts");
            else Path= FileIO.GlobalTextPath;
            if(!Path.toFile().exists() || !Path.toFile().isDirectory())
            {
                mod.Log.info("Missing world text data directory,starting crate");
                Files.createDirectory(Path);
            }
            Path PermissionFilePath = Paths.get(Path.toString(), "permissions.json");
            if(!PermissionFilePath.toFile().exists() || !PermissionFilePath.toFile().isFile())
            {
                mod.Log.info("Missing world text data permission file,starting crate");
                Files.createFile(PermissionFilePath);
                FileWriter fr=new FileWriter(String.valueOf(PermissionFilePath));
                fr.write("{}");
                fr.flush();
                fr.close();
            }
            Files.walk(Path,Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i->{
                if(i.getFileName().toString().equals("permissions.json")) return;
                sb.append(i.getFileName()).append("\n");
            });
            context.getSource().sendMessage(Text.translatable("filereader.printcurrentpath","\n"+sb.toString()));
            return  0;
        }
        catch (Exception ex) {throw new SimpleCommandExceptionType(Text.literal("Exception occurred Message:"+ex.getMessage())).create();}
    }
    public  static  int WriteFile(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        try {
            sender sender= GetSender(context);
            ServerCommandSource source = context.getSource();
            if(sender == cn.thecoldworld.textfilereader.sender.console) {mod.Log.info("Command FileReader from server console");return PrintConsole(context);}
            Entity entity = source.getEntityOrThrow();
            mod.Log.info("Command FileReader from player "+entity.getEntityName());
            source.sendMessage(Text.literal(String.format("From %s send",entity.getEntityName())));
            return 0;
        }
        catch (CommandSyntaxException syntaxException){ throw syntaxException;}
        catch (java.lang.Exception ex){throw new SimpleCommandExceptionType(Text.literal(ex.getMessage())).create();}
    }

    public static int PrintConsole(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        System.out.println("From Console");
        return 0;
    }



    public  static  sender GetSender(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        ServerCommandSource source = context.getSource();
        Entity sender = source.getEntity();
        if(sender == null)
        {
            if(source.getName() .equals("Server") ) return cn.thecoldworld.textfilereader.sender.console;
            throw new SimpleCommandExceptionType(Text.translatable("filereader.invaidsender")).create();
        }
        source.getEntityOrThrow();
        return cn.thecoldworld.textfilereader.sender.player;
    }
}
