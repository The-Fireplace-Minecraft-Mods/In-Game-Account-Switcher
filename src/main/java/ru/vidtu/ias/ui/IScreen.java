package ru.vidtu.ias.ui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * General abstraction superclass for all IAS screens. This class contain useful abstractions.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public abstract class IScreen extends Screen {
    //? if < 1.19.4 {
    /*/^*
     * Tooltip to be rendered last pass. (<1.19.4)
     ^/
    @Nullable
    protected List<FormattedCharSequence> tooltip;
    *///?}

    /**
     * Creates a new screen.
     *
     * @param title Screen title
     */
    @Contract(pure = true)
    protected IScreen(Component title) {
        // Call super.
        super(title);

        // Validate.
        assert title != null : "Parameter 'title' is null. (screen: " + this + ')';
    }


    /**
     * Renders this screen. Called by the implementation.
     *
     * @param graphics  Current graphics handler
     * @param mouseX    Scaled mouse X position
     * @param mouseY    Scaled mouse Y position
     * @param tickDelta Current tick delta (not to be confused with the partial tick)
     */
    //? if >=1.20.1 {
    protected abstract void renderContents(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float tickDelta);
    //?} else
    /*protected abstract void renderContents(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float tickDelta);*/

    /**
     * Adds the widget to this screen using the appropriate method in the implementation.
     *
     * @param widget Widget to add
     */
    protected final void add(AbstractWidget widget) {
        // Validate.
        assert widget != null : "IAS: Parameter 'widget' is null. (screen: " + this + ')';

        // Delegate.
        //? if >=1.17.1 {
        this.addRenderableWidget(widget);
        //?} else
        /*this.addButton(widget);*/
    }

    /**
     * Sets the tooltip. (<1.19.4)
     *
     * @param tooltip Tooltip to be rendered last pass
     */
    protected final void tooltip(List<FormattedCharSequence> tooltip) {
        // Validate.
        assert tooltip != null : "IAS: Parameter 'tooltip' is null. (screen: " + this + ')';

        // Assign.
        //? if <1.19.4
        /*this.tooltip = tooltip;*/
    }

    /**
     * Renders this screen. Called by the implementation.
     *
     * @param graphics  Current graphics handler
     * @param mouseX    Scaled mouse X position
     * @param mouseY    Scaled mouse Y position
     * @param tickDelta Current tick delta (not to be confused with the partial tick)
     */
    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
    @Override
    //? if >=1.20.1 {
    public final void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
    //?} else
    /*public final void render(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float tickDelta) {*/
        // Validate.
        assert graphics != null : "IAS: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
        assert mouseX >= 0 : "IAS: Parameter 'mouseX' is negative. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
        assert mouseY >= 0 : "IAS: Parameter 'mouseY' is negative. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
        assert (tickDelta >= 0.0F) && (tickDelta < Float.POSITIVE_INFINITY)  : "IAS: Parameter 'tickDelta' is not in the [0..inf) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';

        // Render background and widgets.
        //? if <1.20.2
        /*this.renderBackground(graphics);*/
        super.render(graphics, mouseX, mouseY, tickDelta);

        // Render contents.
        this.renderContents(graphics, mouseX, mouseY, tickDelta);

        // Render the last pass tooltip.
        //? if < 1.19.4 {
        /*if (this.tooltip == null) return;
        this.renderTooltip(graphics, this.tooltip, mouseX, mouseY);
        this.tooltip = null;
        *///?}
    }
}
