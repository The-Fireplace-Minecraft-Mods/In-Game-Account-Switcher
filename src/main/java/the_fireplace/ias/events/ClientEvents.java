package the_fireplace.ias.events;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

/**
 * @author The_Fireplace
 */
public class ClientEvents {
	private static int textX, textY;
	@SubscribeEvent
	public void guiEvent(InitGuiEvent.Post event){
		GuiScreen gui = event.getGui();
		if(gui instanceof GuiMainMenu){
			try {
				ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
				textX = ((Number) engine.eval(ConfigValues.TEXT_X.replace("%width%", Integer.toString(event.getGui().width))
						.replace("%height%", Integer.toString(event.getGui().height)))).intValue();
				textY = ((Number) engine.eval(ConfigValues.TEXT_Y.replace("%width%", Integer.toString(event.getGui().width))
						.replace("%height%", Integer.toString(event.getGui().height)))).intValue();
			} catch (Throwable t) {
				textX = event.getGui().width / 2;
				textY = event.getGui().height / 4 + 48 + 72 + 12 + 22;
			}
			event.getButtonList().add(new GuiButtonWithImage(20, gui.width / 2 + 104, (gui.height / 4 + 48) + 72 + 12));
		}
	}
	@SubscribeEvent
	public void onClick(ActionPerformedEvent event){
		if(event.getGui() instanceof GuiMainMenu && event.getButton().id == 20){
			if(Config.getInstance() == null){
				Config.load();
			}
			Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelector());
		}
	}
	@SubscribeEvent
	public void onTick(DrawScreenEvent.Post t) {
		GuiScreen screen = t.getGui();
		if (screen instanceof GuiMainMenu) {
			screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format("ias.loggedinas") + Minecraft.getMinecraft().getSession().getUsername()+".", textX, textY, 0xFFCC8888);
		}else if(screen instanceof GuiMultiplayer){
			if (Minecraft.getMinecraft().getSession().getToken().equals("0")) {
				screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format("ias.offlinemode"), screen.width / 2, 10, 16737380);
			}
		}
	}
}
