package the_fireplace.ias.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.StringUtils;

public class GuiPasswordField extends EditBox {
    public GuiPasswordField(Font font, int x, int y, int par5Width, int par6Height, String s) {
        super(font, x, y, par5Width, par6Height, s);
        setFormatter((t, u) -> StringUtils.repeat('*', t.length()));
    }

    @Override
    public boolean keyPressed(int key, int oldkey, int mods) {
        return !Screen.isCopy(key) && !Screen.isCut(key) && super.keyPressed(key, oldkey, mods);
    }
}
