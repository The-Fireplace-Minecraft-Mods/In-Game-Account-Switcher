package the_fireplace.ias.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
/**
 * The button with the image on it.
 * @author The_Fireplace
 */
public class GuiButtonWithImage extends ButtonWidget {
	private static final Identifier customButtonTextures = new Identifier("ias", "textures/gui/custombutton.png");

	public GuiButtonWithImage(int x, int y, PressAction p) {
		super(x, y, 20, 20, new LiteralText(""), p);
	}
	
	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float delta) {
		if (this.visible) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, customButtonTextures);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = getYImage(hovered);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			drawTexture(ms, this.x, this.y, 0, k * 20, 20, 20);
		}
	}
}
