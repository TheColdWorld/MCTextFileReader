package cn.thecoldworld.textfilereader.client.screen;

import cn.thecoldworld.textfilereader.Settings;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minecraft.client.option.GameOptions.getGenericValueText;

@Environment(EnvType.CLIENT)
public final class ClientSettingGUI extends GameOptionsScreen {
    private final Screen prevScreen;
    private OptionListWidget optionListWidget;

    public ClientSettingGUI(@NotNull Screen prevScreen, MinecraftClient client) {
        super(prevScreen, Objects.requireNonNullElse(client, MinecraftClient.getInstance()).options, Text.translatable("options.textfilereader.clientoptiomstitle"));
        this.client = Objects.requireNonNullElse(client, MinecraftClient.getInstance());
        this.prevScreen = prevScreen;
    }

    private static SimpleOption<?>[] getOptions(Settings ModSettings, cn.thecoldworld.textfilereader.client.Settings ClientSettings) {
        return new SimpleOption[]{
                SimpleOption.ofBoolean("options.textfilereader.removeinvalidfile", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.removeinvalidfile.tooltip")), ModSettings.isRemoveInvalidFile(), ModSettings::setRemoveInvalidFile),
                SimpleOption.ofBoolean("options.textfilereader.segmentedoutput", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.segmentedoutput.tooltip")), ModSettings.isSegmentedOutput(), ModSettings::setSegmentedOutput),
                SimpleOption.ofBoolean("options.textfilereader.guiautoupdatefilelist", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.guiautoupdatefilelist.tooltip")), ClientSettings.isGuiAutoUpdateFileList(), ClientSettings::setGuiAutoUpdateFileList),
                SimpleOption.ofBoolean("options.textfilereader.pausegame", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.pausegame.tooltop")), ClientSettings.isPauseGame(), ClientSettings::setPauseGame),
                SimpleOption.ofBoolean("options.textfilereader.logsenders", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.logsenders.tooltip")), ModSettings.isLogSenders(), ModSettings::setLogSenders),
                new SimpleOption<>("options.textfilereader.readscreen.linesperpage", SimpleOption.emptyTooltip(), (optionText, value) -> getGenericValueText(optionText, Text.literal(value.toString())), new SimpleOption.ValidatingIntSliderCallbacks(1, 512), ClientSettings.getLinesPerPage(), ClientSettings::setLinesPerPage),
                new SimpleOption<>("options.textfilereader.threads", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.threads.tooltip")), (optionText, value) -> getGenericValueText(optionText, Text.literal(value.toString())), new SimpleOption.ValidatingIntSliderCallbacks(4, 64), ModSettings.getThreads(), ModSettings::setThreads)

        };
    }

    @Override
    protected void init() {
        optionListWidget = new OptionListWidget(this.client, this.width, this.height, 0, 25);
        optionListWidget.addAll(getOptions(variables.ModSettings, cn.thecoldworld.textfilereader.client.variables.ClientModSettings));
        addSelectableChild(optionListWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        optionListWidget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
    }

    public void Execute(Runnable action) {
        Objects.requireNonNullElse(client, MinecraftClient.getInstance()).execute(action);
    }
}
