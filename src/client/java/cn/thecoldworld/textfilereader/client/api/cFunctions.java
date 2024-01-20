package cn.thecoldworld.textfilereader.client.api;

import cn.thecoldworld.textfilereader.FileIO;
import cn.thecoldworld.textfilereader.ServerFileSource;
import cn.thecoldworld.textfilereader.client.networking.ClientNetWorkingTask;
import cn.thecoldworld.textfilereader.client.networking.NetWorkingFunctions;
import cn.thecoldworld.textfilereader.client.screen.TextGUI;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

@Environment(EnvType.CLIENT)
public abstract class cFunctions {
    @Environment(EnvType.CLIENT)
    public static void RegisterClientNetworkReceivers(Identifier... NetworkingIdentifiers) {
        for (Identifier i : NetworkingIdentifiers) {
            ClientPlayNetworking.registerGlobalReceiver(i, ((client, handler, buf, responseSender) -> NetWorkingFunctions.GetNetPackageCallback(client, handler, buf, responseSender, i)));
        }
    }

    @Environment(EnvType.CLIENT)
    public static int GetFileFromServer(CommandContext<FabricClientCommandSource> context, ServerFileSource serverFileSource) {
        {
            try {
                ClientNetWorkingTask.Run(new C2SGetFileContent(context.getArgument("FileName", String.class), serverFileSource), variables.Identifiers.TextFileNetworkingIdentifier,
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

    public static void GetLocalFile(String FileName, TextGUI screen) {
        screen.SetText("Reading");
        File file = Paths.get(MinecraftClient.getInstance().runDirectory.getAbsoluteFile().toString(), "Texts", FileName).normalize().toFile();
        if (!file.isFile() || !file.exists()) screen.SetText(I18n.translate("text.filereader.filenotfound", FileName));
        try (Scanner fp = new Scanner(file)) {
            LinkedList<String> filelines = new LinkedList<>();
            while (fp.hasNextLine()) {
                filelines.add(fp.nextLine().replaceAll("[\n\b\r]", ""));
            }
            screen.SetTextPage(cn.thecoldworld.textfilereader.api.funcitons.GetPages(cn.thecoldworld.textfilereader.client.variables.ClientModSettings.getLinesPerPage(), filelines));
        } catch (FileNotFoundException e) {
            screen.SetText(I18n.translate("text.filereader.filenotfound", FileName));
        }
    }

    public static void GetFileFromServer(String FileName, ServerFileSource serverFileSource, TextGUI Screen) {
        Screen.SetText(Text.translatable("gui.textfilereader.read.wait"));
        ClientNetWorkingTask.Run(new C2SGetFileContent(FileName, serverFileSource), variables.Identifiers.TextFileNetworkingIdentifier,
                List.of(arguments -> {
                    try {
                        if (FailedContent.IsInstance(arguments.value)) {
                            LinkedList<String> Keys = new LinkedList<>(Arrays.asList(arguments.value.get("Reason").getAsString().split("\n")));
                            String Key = Keys.get(0);
                            Keys.remove(0);
                            Screen.SetText(Text.translatable(Key, Keys));
                            return;
                        }
                        if (!S2CGetFileContent.IsInstance(arguments.value)) return;
                        String i = arguments.value.get("Value").getAsString();
                        if (!i.contains("\n")) {
                            Screen.SetText(i);
                        } else {
                            Scanner PageScanner = new Scanner(i);
                            LinkedList<String> Lines = new LinkedList<>();
                            while (PageScanner.hasNextLine()) {
                                Lines.add(PageScanner.nextLine().replaceAll("[\n\r\b]", ""));
                            }
                            PageScanner.close();
                            if (Lines.isEmpty()) return;
                            if (Lines.size() <= cn.thecoldworld.textfilereader.client.variables.ClientModSettings.getLinesPerPage()) {
                                Screen.SetText(i);
                                return;
                            }
                            Screen.SetTextPage(cn.thecoldworld.textfilereader.api.funcitons.GetPages(cn.thecoldworld.textfilereader.client.variables.ClientModSettings.getLinesPerPage(), Lines));
                        }
                    } catch (Exception e) {
                        Screen.SetText(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()).formatted(Formatting.RED));
                    }
                    Screen.Execute(Screen::clearAndInit);
                }));
    }

    @Environment(EnvType.CLIENT)
    public static int CGetFileContext(@NotNull CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {

        String Fileaddress = context.getArgument("FileName", String.class);
        try (Scanner fp = new Scanner(Paths.get(FileIO.GlobalTextPath.toString(), Fileaddress), StandardCharsets.UTF_8)) {
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
        } catch (NoSuchFileException fe) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.filenotfound", fe.getMessage())).create();
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", ex.getClass().getCanonicalName(), ex.getMessage())).create();
        }
        return 0;
    }

    @Environment(EnvType.CLIENT)
    public String AsString(FailedContent content) {
        LinkedList<String> Keys = new LinkedList<>(Arrays.asList(content.Reason.split("\n")));
        String Key = Keys.get(0);
        Keys.remove(0);
        return I18n.translate(Key, Keys.toArray());
    }
}
