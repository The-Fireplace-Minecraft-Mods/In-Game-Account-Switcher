package the_fireplace.ias.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * @author The_Fireplace
 */
public class ClientEvents {
	/*public void preGui(InitGuiEvent.Pre event){
		if(event.getGui() instanceof GuiMainMenu){
			Config.save();
		}
	}*/
	/*public void guiEvent(InitGuiEvent.Post event){
		GuiScreen gui = event.getGui();
		if(gui instanceof GuiMainMenu){
			event.getButtonList().add(new GuiButtonWithImage(20, gui.width / 2 + 104, (gui.height / 4 + 48) + 72 + 12, 20, 20, ""));
		}
	}*/
	/*public void onClick(ActionPerformedEvent event){
		if(event.getGui() instanceof GuiMainMenu && event.getButton().id == 20){
			if(Config.getInstance() == null){
				Config.load();
			}
			Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelector());
		}
	}*/
	public static void onTick(Minecraft mc) {
		GuiScreen screen = mc.currentScreen;
		if (screen instanceof GuiMainMenu) {
			screen.drawCenteredString(mc.fontRendererObj, I18n.format("ias.loggedinas") + mc.getSession().getUsername()+".", screen.width / 2, screen.height / 4 + 48 + 72 + 12 + 22, 0xFFCC8888);
		}else if(screen instanceof GuiMultiplayer){
			if (mc.getSession().getToken().equals("0")) {
				screen.drawCenteredString(mc.fontRendererObj, I18n.format("ias.offlinemode"), screen.width / 2, 10, 16737380);
			}
		}
	}
	/*public void configChanged(ConfigChangedEvent event){
		if(event.getModID().equals(Reference.MODID)){
			IAS.syncConfig();
		}
	}*/
}
