package the_fireplace.ias.events;

import the_fireplace.ias.IAS;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import the_fireplace.ias.tools.Reference;

/**
 * @author The_Fireplace
 */
public class FMLEvents {

	@SubscribeEvent
	public void onTick(RenderTickEvent t) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if (screen instanceof GuiMainMenu) {
			screen.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, StatCollector.translateToLocal("ias.loggedinas") + Minecraft.getMinecraft().getSession().getUsername()+".", screen.width / 2, screen.height / 4 + 48 + 72 + 12 + 22, 0xFFCC8888);
		}else if(screen instanceof GuiMultiplayer){
			if (Minecraft.getMinecraft().getSession().getToken().equals("0")) {
				screen.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, StatCollector.translateToLocal("ias.offlinemode"), screen.width / 2, 10, 16737380);
			}
		}
	}
	@SubscribeEvent
	public void configChanged(ConfigChangedEvent event){
		if(event.modID.equals(Reference.MODID)){
			IAS.syncConfig();
		}
	}
}
