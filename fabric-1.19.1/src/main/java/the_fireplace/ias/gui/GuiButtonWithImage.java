package the_fireplace.ias.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The button with the image on it.
 *
 * @author The_Fireplace
 */
public class GuiButtonWithImage extends Button {
    private static final ResourceLocation customButtonTextures = new ResourceLocation("ias", "textures/gui/custombutton.png");

    public GuiButtonWithImage(int x, int y, OnPress p) {
        super(x, y, 20, 20, Component.literal("ButterDog"), p);
    }

    @Override
    public void renderButton(PoseStack ms, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            Minecraft mc = Minecraft.getInstance();
            RenderSystem.setShaderTexture(0, customButtonTextures);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = getYImage(isHovered);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.blendFunc(770, 771);
            blit(ms, this.x, this.y, 0, k * 20, 20, 20);
        }
    }
}
