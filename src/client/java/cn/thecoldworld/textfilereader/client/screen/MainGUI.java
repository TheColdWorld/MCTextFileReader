package cn.thecoldworld.textfilereader.client.screen;

import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.client.cFunctions;
import cn.thecoldworld.textfilereader.client.networking.ClientNetWorkingTask;
import cn.thecoldworld.textfilereader.client.screen.widgets.FileListWidget;
import cn.thecoldworld.textfilereader.networking.jsonformats.C2SGetFileList;
import cn.thecoldworld.textfilereader.networking.jsonformats.FailedContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2CGetFileList;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class MainGUI extends Screen {
    private final List<String> files;
    @NotNull
    private final Screen prevScreen;
    private FileSource fileSource;
    private ScheduledFuture syncService;

    public MainGUI(@NotNull Screen prevScreen) {
        super(Text.literal("Test"));
        this.prevScreen = prevScreen;
        this.fileSource = FileSource.global;
        this.files = new LinkedList<>();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        FileListWidget widget = new FileListWidget(client, width, height / 3 * 2, 0, height - 25, textRenderer);
        ButtonWidget SwitchModeButton = ButtonWidget.builder(Text.translatable(switch (fileSource) {
                    case global -> "gui.filereader.main.switchbutton.global";
                    case save -> "gui.filereader.main.switchbutton.save";
                    case local -> "gui.filereader.main.switchbutton.local";
                }), button -> {
                    switch (fileSource) {
                        case save -> UpdateFileList(FileSource.global);
                        case global -> UpdateFileList(FileSource.save);
                        case local -> UpdateFileList(FileSource.local);
                    }
                })
                .dimensions(width / 2 - 205, height - 25, 200, 20)
                .build();
        ButtonWidget ReadButton = ButtonWidget.builder(Text.translatable("gui.filereader.main.readbutton"), button -> {
            try {
                if (widget.getSelectedOrNull() == null) return;
                TextGUI GUI = new TextGUI(this, client, widget.getSelectedOrNull().FileName);
                cFunctions.GetFileFromServer(widget.getSelectedOrNull().FileName, fileSource, GUI);
                client.setScreen(GUI);
                client.execute(GUI::clearAndInit);
            } catch (Exception e) {
                variables.Log.error("", e);
            }
        }).dimensions(width / 2, height - 25, 200, 20).build();
        widget.AddChildren(files);
        addDrawableChild(widget);
        addDrawableChild(SwitchModeButton);
        addDrawableChild(ReadButton);
    }

    public void UpdateFileList(FileSource fileSource) {
        this.fileSource = fileSource;
        this.SyncFiles();
        Reinit();
    }

    public void Reinit() {
        if (client == null) return;
        client.execute(this::clearAndInit);
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        if (cn.thecoldworld.textfilereader.client.variables.ClientModSettings.isGuiAutoUpdateFileList()) {
            syncService = variables.scheduledExecutorService.scheduleAtFixedRate(this::SyncFiles, 100, 10, TimeUnit.SECONDS);
        }
        SyncFiles();
    }

    public void SyncFiles() {
        variables.scheduledExecutorService.schedule(
                () -> {
                    JsonObject aReturn;
                    try {
                        aReturn = ClientNetWorkingTask.Run(new C2SGetFileList(fileSource)
                                , variables.Identifiers.TextFileListNetworkingIdentifier, true).getAWaiter().GetResult().value;
                    } catch (NullPointerException e) {
                        variables.Log.error("", e);
                        return;
                    }
                    if (FailedContent.IsInstance(aReturn.toString())) {
                        files.clear();
                        LinkedList<String> Keys = new LinkedList<>(Arrays.stream(aReturn.get("Reason").getAsString().split("\n")).toList());
                        String Key = Keys.get(0);
                        Keys.remove(0);
                        files.add(Text.translatable(Key, Keys).formatted(Formatting.RED).getString());
                    } else {
                        files.clear();
                        if (S2CGetFileList.IsInstance(aReturn.toString())) {
                            List<JsonElement> j = aReturn.get("files").getAsJsonArray().asList();
                            j.forEach(jx -> files.add(jx.getAsString()));
                        } else {
                            files.add(Text.literal("Error").formatted(Formatting.RED).getString());
                        }
                    }
                    Reinit();
                }, 0, TimeUnit.MICROSECONDS);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
        if (cn.thecoldworld.textfilereader.client.variables.ClientModSettings.isGuiAutoUpdateFileList())
            syncService.cancel(true);
    }
}
