package the_fireplace.ias.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

public class GuiPasswordField extends TextFieldWidget {
    public GuiPasswordField(FontRenderer font, int x, int y, int par5Width, int par6Height, ITextComponent c) {
        super(font, x, y, par5Width, par6Height, c);
        setFormatter((t, u) -> new StringTextComponent(StringUtils.repeat('*', t.length())).getVisualOrderText());
    }

    @Override
    public boolean keyPressed(int key, int oldkey, int mods) {
        return !Screen.isCopy(key) && !Screen.isCut(key) && super.keyPressed(key, oldkey, mods);
    }
}
