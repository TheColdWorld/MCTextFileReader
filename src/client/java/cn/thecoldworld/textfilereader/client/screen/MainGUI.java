package cn.thecoldworld.textfilereader.client.screen;

import cn.thecoldworld.textfilereader.ClientFileSource;
import cn.thecoldworld.textfilereader.ServerFileSource;
import cn.thecoldworld.textfilereader.client.api.cFunctions;
import cn.thecoldworld.textfilereader.client.networking.ClientNetWorkingTask;
import cn.thecoldworld.textfilereader.client.screen.widgets.FileListWidget;
import cn.thecoldworld.textfilereader.networking.jsonformats.C2SGetFileList;
import cn.thecoldworld.textfilereader.networking.jsonformats.FailedContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2CGetFileList;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class MainGUI extends BasicGui {
    protected final List<String> files;
    protected ClientFileSource fileSource;
    protected ScheduledFuture<?> syncService;

    public MainGUI(@Nullable Screen prevScreen, MinecraftClient client) {
        super(Objects.requireNonNullElse(client, MinecraftClient.getInstance()), Text.translatable("gui.filereader.main.title"), prevScreen);
        this.fileSource = ClientFileSource.global;
        this.files = new LinkedList<>();

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        FileListWidget widget = new FileListWidget(client, width, height / 3 * 2, 0, 25, textRenderer);
        ButtonWidget SwitchModeButton = ButtonWidget.builder(Text.translatable(switch (fileSource) {
                    case global -> "gui.filereader.main.switchbutton.global";
                    case save -> "gui.filereader.main.switchbutton.save";
                    case local -> "gui.filereader.main.switchbutton.local";
                }), button -> {
                    switch (fileSource) {
                        case save -> UpdateFileList(ClientFileSource.local);
                        case global -> UpdateFileList(ClientFileSource.save);
                        case local -> UpdateFileList(ClientFileSource.global);
                    }
                })
                .dimensions(width / 2 - 205, height - 25, 200, 20)
                .build();
        ButtonWidget ReadButton = ButtonWidget.builder(Text.translatable("gui.filereader.main.readbutton"), button -> {
            try {
                if (Optional.ofNullable(widget.getSelectedOrNull()).isEmpty()) return;
                TextGUI GUI = new TextGUI(this, client, widget.getSelectedOrNull().FileName);
                String FileName = widget.getSelectedOrNull().FileName;
                if (fileSource == ClientFileSource.local) {
                    cFunctions.GetLocalFile(FileName, GUI);
                } else {
                    cFunctions.GetFileFromServer(FileName, switch (fileSource) {
                        case save -> ServerFileSource.save;
                        case global -> ServerFileSource.global;
                        default -> throw new IllegalStateException("Unexpected value: " + fileSource);
                    }, GUI);
                }
                client.setScreen(GUI);
            } catch (Exception e) {
                variables.Log.error("", e);
            }
        }).dimensions(width / 2, height - 25, 200, 20).build();
        widget.AddChildren(files);
        addDrawableChild(widget);
        addDrawableChild(SwitchModeButton);
        addDrawableChild(ReadButton);
    }

    public void UpdateFileList(ClientFileSource serverFileSource) {
        this.fileSource = serverFileSource;
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
        variables.scheduledExecutorService.schedule(() -> {
            if (fileSource == ClientFileSource.local) {
                if (Objects.isNull(client)) return;
                File dir = Paths.get(client.runDirectory.toString(), "Texts").toAbsolutePath().normalize().toFile();
                files.clear();
                for (File f : Objects.requireNonNullElse(dir.listFiles(f -> (f.isFile() || f.exists()) && !f.getName().equals("permissions.json")), new File[0])) {
                    files.add(f.getName());
                }
            } else {
                JsonObject aReturn = ClientNetWorkingTask.Run(new C2SGetFileList(switch (fileSource) {
                    case global -> ServerFileSource.global;
                    case save -> ServerFileSource.save;
                    default -> throw new IllegalStateException("Unexpected value: " + fileSource);
                }), variables.Identifiers.TextFileListNetworkingIdentifier, true).getAWaiter().GetResult().value;
                files.clear();
                if (FailedContent.IsInstance(aReturn)) {
                    LinkedList<String> Keys = new LinkedList<>(Arrays.asList(aReturn.get("Reason").getAsString().split("\n")));
                    String Key = Keys.get(0);
                    Keys.remove(0);
                    files.add(Text.translatable(Key, Keys).formatted(Formatting.RED).getString());
                } else if (S2CGetFileList.IsInstance(aReturn.toString())) {
                    aReturn.get("files").getAsJsonArray().forEach(jx -> files.add(jx.getAsString()));
                } else files.add(Text.literal("Error").formatted(Formatting.RED).getString());
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
        super.close();
        if (cn.thecoldworld.textfilereader.client.variables.ClientModSettings.isGuiAutoUpdateFileList())
            syncService.cancel(true);
    }

    public void Execute(Runnable action) {
        Objects.requireNonNullElse(client, MinecraftClient.getInstance()).execute(action);
    }
}
