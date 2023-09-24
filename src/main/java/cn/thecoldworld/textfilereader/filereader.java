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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class filereader {
    public static void Init(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        mod.Log.info("Loading server command: /FileReader");
        dispatcher.register(CommandManager.literal("FileReader")
                .then(CommandManager.literal("Permission").requires(src->src.hasPermissionLevel(2)))
                .then(CommandManager.literal("File")
                        .then(CommandManager.literal("Get")
                                .then(CommandManager.argument("FileName", StringArgumentType.string())
                                        .then(CommandManager.literal("Global").executes(i->GetFileContext(i,FileSource.global)))
                                        .then(CommandManager.literal("Save").executes(i->GetFileContext(i,FileSource.save)))))
                        .then(CommandManager.literal("List")
                                .then(CommandManager.literal("Global").executes(i->PrintFileList(i,FileSource.global)))
                                .then(CommandManager.literal("World").executes(i->PrintFileList(i,FileSource.save)))
                        ))
                .executes(i->{
                    i.getSource().sendMessage(Text.literal("TextFileReader"));
                    i.getSource().sendMessage(Text.translatable("text.filereader.description"));
                    return 0;})
        );
    }
    public  static int PrintFileList(CommandContext<ServerCommandSource> context,FileSource fileSource) throws CommandSyntaxException
    {
        try {
            boolean Segmentedoutput= mod.GetConfig("Segmentedoutput").getAsBoolean();
            Path Path;
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
            if(Segmentedoutput)
            {
                ServerCommandSource src=context.getSource();
                CompletableFuture.supplyAsync(()->{
                    src.sendMessage(Text.translatable("text.filereader.printcurrentpath",""));
                    try {
                        Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                            if (i.getFileName().toString().equals("permissions.json")) return;
                            src.sendMessage(Text.literal(i.getFileName().toString()));
                        });
                    } catch (IOException e) {
                        mod.Log.error("",e);
                        src.sendMessage(Text.translatable("text.filereader.exception",e.getClass().toString(),e.getMessage()));
                    }
                    return 0;
                });
            }
            else
            {
                StringBuilder sb=new StringBuilder();
                Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                    if (i.getFileName().toString().equals("permissions.json")) return;
                    sb.append(i.getFileName()).append("\n");
                });
                context.getSource().sendMessage(Text.translatable("text.filereader.printcurrentpath","\n"+sb.toString()));
            }
            return  0;
        }
        catch (Exception ex) {throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",ex.getClass().toString(),ex.getMessage())).create();}
    }
    public  static  int GetFileContext(CommandContext<ServerCommandSource> context,FileSource fileSource) throws CommandSyntaxException
    {
        try {
            sender sender= GetSender(context);
            ServerCommandSource source = context.getSource();
            String FileAddress = context.getArgument("FileName", String.class);
            if(sender == cn.thecoldworld.textfilereader.sender.console) {mod.Log.info("Command FileReader File Get from server console");return PrintConsole(context,FileAddress,fileSource);}
            Entity entity = source.getEntityOrThrow();
            CompletableFuture.supplyAsync(()-> {
                try {
                    return FileIO.PrintFile(entity,source.getWorld(),FileAddress,fileSource);
                } catch (IOException e) {
                    mod.Log.error("",e);
                    context.getSource().sendMessage(Text.translatable("text.filereader.exception",e.getClass().toString(),e.getMessage()));
                    return -1;
                }
            });
            mod.Log.info("Command FileReader File Get from player "+entity.getEntityName());
            return 0;
        }
        catch (CommandSyntaxException syntaxException){ throw syntaxException;}
        catch (java.lang.Exception ex){
            context.getSource().sendMessage(Text.translatable("text.filereader.exception",ex.getClass().toString(),ex.getMessage()));
            throw new SimpleCommandExceptionType(Text.literal(ex.getMessage())).create();
        }
    }

    public static int PrintConsole(CommandContext<ServerCommandSource> context,String FileName,FileSource fileSource) throws CommandSyntaxException
    {
        try
        {
            boolean Segmentedoutput= mod.GetConfig("Segmentedoutput").getAsBoolean();
            Scanner fp;
            switch (fileSource)
            {
                case save -> {
                    fp=new Scanner(Paths.get(context.getSource().getWorld().getServer().getSavePath(WorldSavePath.ROOT).getParent().toString(),FileName),StandardCharsets.UTF_8);
                }
                case global -> {
                    fp=new Scanner(Paths.get(FileIO.GlobalTextPath.toString(),FileName), StandardCharsets.UTF_8);
                }
                default -> throw new SimpleCommandExceptionType(Text.literal("Internal error")).create();
            }
            if(Segmentedoutput)
            {
                mod.Log.info(Text.translatable("text.filereader.printfile",FileName,"").toString());
                while (fp.hasNext())
                {
                    mod.Log.info(fp.nextLine());
                }
            }
            else
            {
                StringBuilder sb= new StringBuilder();
                while (fp.hasNext())
                {
                    sb.append(fp.nextLine()).append('\n');
                }
                mod.Log.info(Text.translatable("text.filereader.printfile",FileName,"\n"+sb).toString());
            }
            fp.close();
            return 0;
        }
        catch (Exception e)
        {
            context.getSource().sendMessage(Text.translatable("text.filereader.exception",e.getClass().toString(),e.getMessage()));
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",e.getClass().toString(),e.getMessage())).create();
        }
    }



    public  static  sender GetSender(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        ServerCommandSource source = context.getSource();
        Entity sender = source.getEntity();
        if(sender == null)
        {
            if(source.getName() .equals("Server") ) return cn.thecoldworld.textfilereader.sender.console;
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.invaidsender")).create();
        }
        source.getEntityOrThrow();
        return cn.thecoldworld.textfilereader.sender.player;
    }
}
