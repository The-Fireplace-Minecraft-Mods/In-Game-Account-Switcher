package the_fireplace.ias.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

/**
 * The button with the image on it.
 *
 * @author The_Fireplace
 */
public class GuiButtonWithImage extends Button {
    private static final ResourceLocation customButtonTextures = new ResourceLocation("ias", "textures/gui/custombutton.png");

    public GuiButtonWithImage(int x, int y, OnPress p) {
        super(x, y, 20, 20, "ButterDog", p);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float delta) {
        if (this.visible) {
            Minecraft mc = Minecraft.getInstance();
            mc.getTextureManager().bind(customButtonTextures);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = getYImage(isHovered);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.blendFunc(770, 771);
            blit(this.x, this.y, 0, k * 20, 20, 20);
        }
    }
}
