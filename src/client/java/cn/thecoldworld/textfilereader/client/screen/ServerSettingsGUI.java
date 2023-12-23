package cn.thecoldworld.textfilereader.client.screen;

import cn.thecoldworld.textfilereader.Settings;
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
import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.option.GameOptions.getGenericValueText;
@Environment(EnvType.CLIENT)
public class ServerSettingsGUI extends GameOptionsScreen {
    private final Screen prevScreen;
    private SimpleOption<?>[] Options;
    private OptionListWidget optionListWidget;

    public ServerSettingsGUI(@NotNull Screen prevScreen) {
        super(prevScreen, MinecraftClient.getInstance().options, Text.translatable("options.textfilereader.serveroptiomstitle"));
        this.prevScreen = prevScreen;
        this.Options = new SimpleOption[0];
    }

    public SimpleOption<?>[] getOptions() {
        return this.Options;
    }

    public void setOptions(@NotNull Settings ModSettings) {
        this.Options = new SimpleOption[]{
                SimpleOption.ofBoolean("options.textfilereader.removeinvalidfile", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.removeinvalidfile.tooltip")), ModSettings.isRemoveInvalidFile(), ModSettings::setRemoveInvalidFile),
                SimpleOption.ofBoolean("options.textfilereader.segmentedoutput", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.segmentedoutput.tooltip")), ModSettings.isSegmentedOutput(), ModSettings::setSegmentedOutput),
                new SimpleOption<>("options.textfilereader.threads", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.threads.tooltip")), (optionText, value) -> getGenericValueText(optionText, Text.literal(value.toString())), new SimpleOption.ValidatingIntSliderCallbacks(4, 64), ModSettings.getThreads(), ModSettings::setThreads)
        };
    }

    @Override
    protected void init() {
        optionListWidget = new OptionListWidget(this.client, this.width, this.height-25, 0, 25);
        optionListWidget.addAll(this.Options);
        ButtonWidget CancelButton = ButtonWidget.builder(Text.translatable("gui.cancel"),button -> {}).dimensions(width / 2, height - 25, 200, 20).build();
        ButtonWidget OkButton = ButtonWidget.builder(Text.translatable("gui.ok"),button -> {}).dimensions(width / 2 - 205, height - 25, 200, 20).build();
        addSelectableChild(optionListWidget);
        addDrawableChild(CancelButton);
        addDrawableChild(OkButton);
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
}
