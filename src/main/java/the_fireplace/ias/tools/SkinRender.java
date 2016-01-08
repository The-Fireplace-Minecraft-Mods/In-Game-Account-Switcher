package the_fireplace.ias.tools;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import the_fireplace.ias.tools.Reference;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Takes care of loading and drawing images to the screen. Adapted from http://www.minecraftforge.net/forum/index.php?topic=11991.0
 * @author dayanto
 * @author The_Fireplace
 */
public class SkinRender
{
	private final File file;
	private DynamicTexture previewTexture;
	private ResourceLocation resourceLocation;
	private final TextureManager textureManager;

	public SkinRender(TextureManager textureManager, File file)
	{
		this.textureManager = textureManager;
		this.file = file;
	}

	/**
	 * Attempts to load the image. Returns whether it was successful or not.
	 */
	private boolean loadPreview()
	{
		try {
			BufferedImage image = ImageIO.read(file);
			previewTexture = new DynamicTexture(image);
			resourceLocation = textureManager.getDynamicTextureLocation(Reference.MODID, previewTexture);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void drawImage(int xPos, int yPos, int width, int height)
	{
		if(previewTexture == null) {
			boolean successful = loadPreview();
			if(!successful){
				System.out.println("Failure");
				return;
			}
		}
		previewTexture.updateDynamicTexture();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer rend = tessellator.getWorldRenderer();
		textureManager.bindTexture(resourceLocation);
		rend.func_181668_a(7, DefaultVertexFormats.field_181709_i);
		rend.func_181662_b(xPos, yPos+height, 0).func_181673_a(0.0D, 1.0D).func_181669_b(255, 255, 255, 255).func_181675_d();
		rend.func_181662_b(xPos+width, yPos+height, 0).func_181673_a(1.0D, 1.0D).func_181669_b(255, 255, 255, 255).func_181675_d();
		rend.func_181662_b(xPos+width, yPos, 0).func_181673_a(1.0D, 0.0D).func_181669_b(255, 255, 255, 255).func_181675_d();
		rend.func_181662_b(xPos, yPos, 0).func_181673_a(0.0D, 0.0D).func_181669_b(255, 255, 255, 255).func_181675_d();
		tessellator.draw();
	}
}