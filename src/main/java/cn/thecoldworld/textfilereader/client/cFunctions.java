package cn.thecoldworld.textfilereader.client;

import cn.thecoldworld.textfilereader.FileIO;
import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.client.screen.TextGUI;
import cn.thecoldworld.textfilereader.networking.ClientNetWorkingTask;
import cn.thecoldworld.textfilereader.networking.NetworkingFunctions;
import cn.thecoldworld.textfilereader.networking.jsonformats.C2SGetFileContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.FailedContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2CGetFileContent;
import cn.thecoldworld.textfilereader.variables;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

@Environment(EnvType.CLIENT)
public class cFunctions {
    @Environment(EnvType.CLIENT)
    public static void RegisterNetworkReceivers(Identifier... NetworkingIdentifiers) {
        for (Identifier i : NetworkingIdentifiers) {
            ClientPlayNetworking.registerGlobalReceiver(i, ((client, handler, buf, responseSender) -> NetworkingFunctions.GetNetPackageCallback(client, handler, buf, responseSender, i)));
        }
    }

    @Environment(EnvType.CLIENT)
    public static int GetFileFromServer(CommandContext<FabricClientCommandSource> context, FileSource fileSource) {
        {
            try {
                ClientNetWorkingTask.Run(new C2SGetFileContent(context.getArgument("FileName", String.class), fileSource), variables.Identifiers.TextFileNetworkingIdentifier,
                        List.of(arguments -> {
                            try {
                                if (FailedContent.IsInstance(arguments.value.toString())) {
                                    LinkedList<String> Keys = new LinkedList<>(Arrays.stream(arguments.value.get("Reason").getAsString().split("\n")).toList());
                                    String Key = Keys.get(0);
                                    Keys.remove(0);
                                    context.getSource().sendError(Text.translatable(Key, Keys.toArray()));
                                }
                                if (!S2CGetFileContent.IsInstance(arguments.value.toString()))
                                    return;
                                context.getSource().sendFeedback(Text.literal(arguments.value.get("Value").getAsString()));
                            } catch (Exception e) {
                                context.getSource().sendError(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()));
                            }
                        }));
            } catch (Exception e) {
                context.getSource().sendError(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()));
            }
            return 0;
        }
    }

    public static void GetFileFromServer(String FileName, FileSource fileSource, TextGUI Screen) throws Exception {
        Screen.SetText(Text.translatable("gui.textfilereader.read.wait"));
        ClientNetWorkingTask.Run(new C2SGetFileContent(FileName, fileSource), variables.Identifiers.TextFileNetworkingIdentifier,
                List.of(arguments -> {
                    try {
                        if (FailedContent.IsInstance(arguments.value.toString())) {
                            LinkedList<String> Keys = new LinkedList<>(Arrays.stream(arguments.value.get("Reason").getAsString().split("\n")).toList());
                            String Key = Keys.get(0);
                            Keys.remove(0);
                            Screen.SetText(Text.translatable(Key, Keys));
                        }
                        if (!S2CGetFileContent.IsInstance(arguments.value.toString())) return;
                        String i = arguments.value.get("Value").getAsString();
                        if (!i.contains("\n")) {
                            Screen.SetText(i);
                        } else {
                            Scanner PageScanner = new Scanner(i);
                            LinkedList<String> Lines = new LinkedList<>();
                            while (PageScanner.hasNextLine()) {
                                Lines.add(PageScanner.nextLine().replaceAll("[\n\r]", ""));
                            }
                            PageScanner.close();
                            if (Lines.isEmpty()) return;
                            if (Lines.size() <= variables.ClientModSettings.getLinesPerPage()) {
                                Screen.SetText(i);
                                return;
                            }
                            int Pages = cn.thecoldworld.textfilereader.funcitons.DivisibleUpwards(Lines.size(), variables.ClientModSettings.getLinesPerPage());
                            String[] pages = new String[Pages];
                            int EndRow = 0;
                            for (int k = 0; k < Pages; k++) {
                                StringBuilder sb = new StringBuilder();
                                for (int j = 0; j < variables.ClientModSettings.getLinesPerPage() && EndRow < Lines.size(); j++, EndRow++) {
                                    sb.append(Lines.get(EndRow).replace("\t", "    ")).append('\n');
                                }
                                pages[k] = sb.toString();
                            }
                            Screen.SetTextPage(pages);
                        }
                    } catch (Exception e) {
                        Screen.SetText(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()).formatted(Formatting.RED));
                    }
                }));
    }

    @Environment(EnvType.CLIENT)
    public static int CGetFileContext(@NotNull CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            String Fileaddress = context.getArgument("FileName", String.class);
            Scanner fp = new Scanner(Paths.get(FileIO.GlobalTextPath.toString(), Fileaddress), StandardCharsets.UTF_8);
            if (variables.ModSettings.isSegmentedOutput()) {
                context.getSource().sendFeedback(Text.translatable("text.filereader.printfile", Fileaddress, ""));
                while (fp.hasNext()) {
                    context.getSource().sendFeedback(Text.literal(fp.nextLine()));
                }
            } else {
                StringBuilder sb = new StringBuilder();
                while (fp.hasNext()) {
                    sb.append(fp.nextLine()).append('\n');
                }
                context.getSource().sendFeedback(Text.translatable("text.filereader.printfile", Fileaddress, "\n" + sb));
            }
            fp.close();
        } catch (NoSuchFileException fe) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.filenotfound", fe.getMessage())).create();
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", ex.getClass().getCanonicalName(), ex.getMessage())).create();
        }
        return 0;
    }
}
