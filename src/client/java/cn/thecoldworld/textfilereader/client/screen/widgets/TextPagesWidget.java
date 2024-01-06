package cn.thecoldworld.textfilereader.client.screen.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Objects;

public final class TextPagesWidget extends ScrollableWidget {
    private final MultilineTextWidget wrapped;
    private final TextRenderer textRenderer;
    private MutableText MainText;

    public TextPagesWidget(int x, int y, int width, int height, MutableText content, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.wrapped = (new MultilineTextWidget(content, textRenderer)).setMaxWidth(this.getWidth() - this.getPaddingDoubled());
        this.textRenderer = textRenderer;
        this.MainText = content;
    }

    public MutableText getMainText() {
        return MainText;
    }

    public void setMainText(MutableText mainText) {
        MainText = mainText;
    }

    @Override
    public boolean isNarratable() {
        return true;
    }


    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.USAGE, MainText);
    }

    @Override
    public double getDeltaYPerScroll() {
        Objects.requireNonNull(this.textRenderer);
        return 10.0;
    }

    @Override
    protected int getContentsHeight() {
        return this.wrapped.getHeight();
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        this.wrapped.render(context, mouseX, mouseY, delta);
    }
}
