package cn.thecoldworld.textfilereader;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Scanner;

public class cfileloader  {
    public  static void init(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher)
    {
        dispatcher.register(ClientCommandManager.literal("cFileLoader")
                .then(ClientCommandManager.literal("Read")
                        .then(ClientCommandManager.argument("FileName",StringArgumentType.string()).executes(cfileloader::CGetFileContext))
                )
                .executes(i -> {
                    i.getSource().sendFeedback(Text.literal("TextFileReader"));
                    i.getSource().sendFeedback(Text.translatable("text.filereader.description"));
                    return 0;
                })
        );
    }
    public static int CGetFileContext(@NotNull CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException
    {
        try {
            String Fileaddress= context.getArgument("FileName",String.class);
            Scanner fp=new Scanner(Paths.get(FileIO.GlobalTextPath.toString(),Fileaddress), StandardCharsets.UTF_8);
            if(mod.GetConfig("Segmentedoutput").getAsBoolean())
            {
                context.getSource().sendFeedback(Text.translatable("text.filereader.printfile",Fileaddress,""));
                while (fp.hasNext())
                {
                    context.getSource().sendFeedback(Text.literal(fp.nextLine()));
                }
            }
            else
            {
                StringBuilder sb= new StringBuilder();
                while (fp.hasNext())
                {
                    sb.append(fp.nextLine()).append('\n');
                }
                context.getSource().sendFeedback(Text.translatable("text.filereader.printfile",Fileaddress,"\n"+sb.toString()));
            }
            fp.close();
        }
        //catch (CommandSyntaxException e) {throw e;}
        catch (NoSuchFileException fe) {throw new  SimpleCommandExceptionType(Text.translatable("text.filereader,filenotfound",fe.getMessage())).create();}
        catch (Exception ex){ throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",ex.getClass().getCanonicalName(),ex.getMessage())).create();}
        return 0;
    }
}
