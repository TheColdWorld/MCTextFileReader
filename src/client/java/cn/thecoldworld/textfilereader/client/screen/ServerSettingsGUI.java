package cn.thecoldworld.textfilereader.client.screen;

import cn.thecoldworld.textfilereader.Settings;
import cn.thecoldworld.textfilereader.client.networking.ClientNetWorkingTask;
import cn.thecoldworld.textfilereader.networking.jsonformats.C2SChangeSettings;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import static net.minecraft.client.option.GameOptions.getGenericValueText;

@Environment(EnvType.CLIENT)
public final class ServerSettingsGUI extends GameOptionsScreen {
    private final Screen prevScreen;
    private SimpleOption<?>[] Options;
    private OptionListWidget optionListWidget;

    private Settings ServerSettings;

    public ServerSettingsGUI(@Nullable Screen prevScreen, MinecraftClient client) {
        super(prevScreen, Objects.requireNonNullElse(client, MinecraftClient.getInstance()).options, Text.translatable("options.textfilereader.serveroptiomstitle"));
        this.client = Objects.requireNonNullElse(client, MinecraftClient.getInstance());
        this.prevScreen = prevScreen;
        this.Options = new SimpleOption[0];
    }

    public SimpleOption<?>[] getOptions() {
        return this.Options;
    }

    public ServerSettingsGUI setOptions(Settings settings) {
        this.ServerSettings = settings;
        this.Options = new SimpleOption[]{
                SimpleOption.ofBoolean("options.textfilereader.removeinvalidfile", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.removeinvalidfile.tooltip")), this.ServerSettings.isRemoveInvalidFile(), this.ServerSettings::setRemoveInvalidFile),
                SimpleOption.ofBoolean("options.textfilereader.segmentedoutput", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.segmentedoutput.tooltip")), this.ServerSettings.isSegmentedOutput(), this.ServerSettings::setSegmentedOutput),
                SimpleOption.ofBoolean("options.textfilereader.logsenders", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.logsenders.tooltip")), this.ServerSettings.isLogSenders(), this.ServerSettings::setLogSenders),
                new SimpleOption<>("options.textfilereader.threads", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.threads.tooltip")), (optionText, value) -> getGenericValueText(optionText, Text.literal(value.toString())), new SimpleOption.ValidatingIntSliderCallbacks(4, 64), this.ServerSettings.getThreads(), this.ServerSettings::setThreads)
        };
        return this;
    }

    @Override
    protected void init() {
        if (this.ServerSettings != null) {
            optionListWidget = new OptionListWidget(this.client, this.width, this.height - 25, 0, 25);
            optionListWidget.addAll(this.getOptions());
            addSelectableChild(optionListWidget);
        } else this.optionListWidget = null;
        ButtonWidget CancelButton = ButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.close())
                .dimensions(width / 2, height - 25, 200, 20).build();
        ButtonWidget OkButton = ButtonWidget.builder(Text.translatable("gui.ok"), button -> {
            SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            outputFmt.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            ClientNetWorkingTask.Run(
                    new C2SChangeSettings("UTC " + outputFmt.format(Calendar.getInstance().getTime()), this.ServerSettings),
                    variables.Identifiers.ControlingIdentifier, false);
            this.close();
        }).dimensions(width / 2 - 205, height - 25, 200, 20).build();
        addDrawableChild(CancelButton);
        addDrawableChild(OkButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.optionListWidget != null) optionListWidget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
    }

    public void Execute(Runnable action) {
        Objects.requireNonNullElse(client, MinecraftClient.getInstance()).execute(action);
    }
}
