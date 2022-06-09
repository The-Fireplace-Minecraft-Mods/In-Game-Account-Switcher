package the_fireplace.ias.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;

public class GuiPasswordField extends EditBox {
    public GuiPasswordField(Font font, int x, int y, int par5Width, int par6Height, Component c) {
        super(font, x, y, par5Width, par6Height, c);
        setFormatter((t, u) -> new TextComponent(StringUtils.repeat('*', t.length())).getVisualOrderText());
    }

    @Override
    public boolean keyPressed(int key, int oldkey, int mods) {
        return !Screen.isCopy(key) && !Screen.isCut(key) && super.keyPressed(key, oldkey, mods);
    }
}
