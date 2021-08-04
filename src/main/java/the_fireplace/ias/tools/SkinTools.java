package the_fireplace.ias.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.vidtu.iasfork.msauth.MicrosoftAccount;

/**
 * Tools that have to do with Skins
 * 
 * @author The_Fireplace
 */
@Environment(EnvType.CLIENT)
public class SkinTools {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final File cachedir = new File(MinecraftClient.getInstance().runDirectory, "cachedImages/skins/");
	private static final File skinOut = new File(cachedir, "temp.png");
	public static void buildSkin(String name) {
		try {
			File f = new File(cachedir, name + ".png");
			if (!f.exists()) loadFromMojang(MinecraftClient.getInstance(), name, f);
			BufferedImage skin = ImageIO.read(f);
			BufferedImage drawing = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
			if (skin.getHeight() == 64) {// New skin type
				int[] head = skin.getRGB(8, 8, 8, 8, null, 0, 8);
				int[] torso = skin.getRGB(20, 20, 8, 12, null, 0, 8);
				int[] larm = skin.getRGB(44, 20, 4, 12, null, 0, 4);
				int[] rarm = skin.getRGB(36, 52, 4, 12, null, 0, 4);
				int[] lleg = skin.getRGB(4, 20, 4, 12, null, 0, 4);
				int[] rleg = skin.getRGB(20, 52, 4, 12, null, 0, 4);
				int[] hat = skin.getRGB(40, 8, 8, 8, null, 0, 8);
				int[] jacket = skin.getRGB(20, 36, 8, 12, null, 0, 8);
				int[] larm2 = skin.getRGB(44, 36, 4, 12, null, 0, 4);
				int[] rarm2 = skin.getRGB(52, 52, 4, 12, null, 0, 4);
				int[] lleg2 = skin.getRGB(4, 36, 4, 12, null, 0, 4);
				int[] rleg2 = skin.getRGB(4, 52, 4, 12, null, 0, 4);
				for (int i = 0; i < hat.length; i++)
					if (new Color(hat[i], true).getAlpha() == 0)
						hat[i] = head[i];
				for (int i = 0; i < jacket.length; i++)
					if (new Color(jacket[i], true).getAlpha() == 0)
						jacket[i] = torso[i];
				for (int i = 0; i < larm2.length; i++)
					if (new Color(larm2[i], true).getAlpha() == 0)
						larm2[i] = larm[i];
				for (int i = 0; i < rarm2.length; i++)
					if (new Color(rarm2[i], true).getAlpha() == 0)
						rarm2[i] = rarm[i];
				for (int i = 0; i < lleg2.length; i++)
					if (new Color(lleg2[i], true).getAlpha() == 0)
						lleg2[i] = lleg[i];
				for (int i = 0; i < rleg2.length; i++)
					if (new Color(rleg2[i], true).getAlpha() == 0)
						rleg2[i] = rleg[i];
				drawing.setRGB(4, 0, 8, 8, hat, 0, 8);
				drawing.setRGB(4, 8, 8, 12, jacket, 0, 8);
				drawing.setRGB(0, 8, 4, 12, larm2, 0, 4);
				drawing.setRGB(12, 8, 4, 12, rarm2, 0, 4);
				drawing.setRGB(4, 20, 4, 12, lleg2, 0, 4);
				drawing.setRGB(8, 20, 4, 12, rleg2, 0, 4);
			} else {// old skin type
				int[] head = skin.getRGB(8, 8, 8, 8, null, 0, 8);
				int[] torso = skin.getRGB(20, 20, 8, 12, null, 0, 8);
				int[] arm = skin.getRGB(44, 20, 4, 12, null, 0, 4);
				int[] leg = skin.getRGB(4, 20, 4, 12, null, 0, 4);
				int[] hat = skin.getRGB(40, 8, 8, 8, null, 0, 8);
				for (int i = 0; i < hat.length; i++)
					if (new Color(hat[i], true).getAlpha() == 0)
						hat[i] = head[i];
				drawing.setRGB(4, 0, 8, 8, hat, 0, 8);
				drawing.setRGB(4, 8, 8, 12, torso, 0, 8);
				drawing.setRGB(0, 8, 4, 12, arm, 0, 4);
				drawing.setRGB(12, 8, 4, 12, arm, 0, 4);
				drawing.setRGB(4, 20, 4, 12, leg, 0, 4);
				drawing.setRGB(8, 20, 4, 12, leg, 0, 4);
			}
			ImageIO.write(drawing, "png", skinOut);
		} catch (Exception e) {
			e.printStackTrace();
			if (skinOut.exists()) skinOut.delete();
			return;
		}
	}
	
	public static void javDrawSkin(MatrixStack ms, int x, int y, int width, int height) {
		if (!skinOut.exists()) return;
		SkinRender r = new SkinRender(MinecraftClient.getInstance().getTextureManager(), skinOut);
		r.drawImage(ms, x, y, width, height);
	}
	
	public static void cacheSkins(boolean force) {
		if (!cachedir.exists() && !cachedir.mkdirs()) {
			System.err.println("unable to load cachedir");
			return;
		}
		MinecraftClient mc = MinecraftClient.getInstance();
		for (int i = 0; i < AltDatabase.getInstance().getAlts().size(); i++) {
			AccountData data = AltDatabase.getInstance().getAlts().get(i);
			mc.getWindow().setTitle("Minecraft* " + SharedConstants.getGameVersion().getName() + " (IAS: Updating skin " + data.alias + "...)");
			File file = new File(cachedir, data.alias + ".png");
			if (force || !file.exists()) {
				loadFromMojang(mc, data.alias, file);
			}
		}
		for (int i = 0; i < MicrosoftAccount.msaccounts.size(); i++) {
			MicrosoftAccount data = MicrosoftAccount.msaccounts.get(i);
			GLFW.glfwSetWindowTitle(mc.getWindow().getHandle(), "Minecraft* " + SharedConstants.getGameVersion().getName() + " (IAS: Updating skin " + data.alias() + "...)");
			File file = new File(cachedir, data.alias() + ".png");
			if (force || !file.exists()) {
				loadFromMojang(mc, data.alias(), file);
			}
		}
		mc.updateWindowTitle();
	}
	
	public static void loadFromMojang(MinecraftClient mc, String name, File f) {
		try {
			InputStream is = new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream();
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			String uuid = GSON.fromJson(isr, JsonObject.class).get("id").getAsString();
			isr.close();
			is.close();
			is = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).openStream();
			isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			String s = GSON.fromJson(isr, JsonObject.class).get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
			isr.close();
			is.close();
			is = new URL(GSON.fromJson(new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8), JsonObject.class).get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString()).openStream();
			if (f.exists()) f.delete();
			Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
			is.close();
		} catch (Exception ign) {
			try {
				InputStream is = mc.getResourceManager().getResource(new Identifier("textures/entity/steve.png")).getInputStream();
				Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
				is.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
