package cn.thecoldworld.textfilereader.screen;

import cn.thecoldworld.textfilereader.Settings;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Environment (EnvType.CLIENT)
public class SettingGUI extends GameOptionsScreen {
    private final Screen prevScreen;
    private OptionListWidget optionListWidget;

    public SettingGUI(@NotNull Screen prevScreen) {
        super(prevScreen, MinecraftClient.getInstance().options, Text.translatable("options.textfilereader.optiomstitle"));
        this.prevScreen = prevScreen;
    }

    private static SimpleOption<?>[] getOptions(Settings ModSettings) {
        return new SimpleOption[]{
                SimpleOption.ofBoolean("options.textfilereader.removeinvalidfile", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.removeinvalidfile.tooltip")), ModSettings.isRemoveInvalidFile(), ModSettings::setRemoveInvalidFile),
                SimpleOption.ofBoolean("options.textfilereader.segmentedoutput", SimpleOption.constantTooltip(Text.translatable("options.textfilereader.segmentedoutput.tooltip")), ModSettings.isSegmentedOutput(), ModSettings::setSegmentedOutput)
        };
    }

    @Override
    protected void init() {
        optionListWidget = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        optionListWidget.addAll(getOptions(variables.ModSettings));
        addSelectableChild(optionListWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, optionListWidget, mouseX, mouseY, delta);

    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
    }

    private ButtonWidget createButton(Text message, ButtonWidget.PressAction pressAction, int width, int height, int x, int y, Text tooltip) {
        return ButtonWidget.builder(message, pressAction)
                .dimensions(x, y, width, height)
                .tooltip(Tooltip.of(tooltip))
                .build();
    }
}
