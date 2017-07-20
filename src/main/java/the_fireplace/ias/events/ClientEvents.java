package the_fireplace.ias.events;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.ias.IAS;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;
import the_fireplace.ias.tools.Reference;

/**
 * @author The_Fireplace
 */
public class ClientEvents {
	@SubscribeEvent
	public void guiEvent(InitGuiEvent.Post event){
		GuiScreen gui = event.getGui();
		if(gui instanceof GuiMainMenu){
			event.getButtonList().add(new GuiButtonWithImage(20, gui.width / 2 + 104, (gui.height / 4 + 48) + 72 + 12, 20, 20, ""));
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
	public void onTick(TickEvent.RenderTickEvent t) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if (screen instanceof GuiMainMenu) {
			screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format("ias.loggedinas") + Minecraft.getMinecraft().getSession().getUsername()+".", screen.width / 2, screen.height / 4 + 48 + 72 + 12 + 22, 0xFFCC8888);
		}else if(screen instanceof GuiMultiplayer){
			if (Minecraft.getMinecraft().getSession().getToken().equals("0")) {
				screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format("ias.offlinemode"), screen.width / 2, 10, 16737380);
			}
		}
	}
	@SubscribeEvent
	public void configChanged(ConfigChangedEvent event){
		if(event.getModID().equals(Reference.MODID)){
			IAS.syncConfig();
		}
	}
}
