package the_fireplace.ias.events;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;
import xyz.openmodloader.OpenModLoader;
import xyz.openmodloader.event.impl.GuiEvent;
import xyz.openmodloader.event.impl.UpdateEvent;

/**
 * @author The_Fireplace
 */
public class ClientEvents {
	public static void init(){
		OpenModLoader.INSTANCE.getEventBus().register(GuiEvent.Init.class, event -> {
			if(event.getGui() instanceof GuiMainMenu) {
				Config.save();
				event.getButtonList().add(new GuiButtonWithImage(20, event.getGui().width / 2 + 104, (event.getGui().height / 4 + 48) + 72 + 12, 20, 20, ""));
			}
		});
		OpenModLoader.INSTANCE.getEventBus().register(GuiEvent.ButtonClick.class, event -> {
			if(event.getGui() instanceof GuiMainMenu && event.getButton().id == 20){
				if(Config.getInstance() == null){
					Config.load();
				}
				Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelector());
			}
		});
		OpenModLoader.INSTANCE.getEventBus().register(UpdateEvent.RenderUpdate.class, event -> {
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiMainMenu) {
				screen.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, I18n.format("ias.loggedinas") + Minecraft.getMinecraft().getSession().getUsername()+".", screen.width / 2, screen.height / 4 + 48 + 72 + 12 + 22, 0xFFCC8888);
			}else if(screen instanceof GuiMultiplayer){
				if (Minecraft.getMinecraft().getSession().getToken().equals("0")) {
					screen.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, I18n.format("ias.offlinemode"), screen.width / 2, 10, 16737380);
				}
			}
		});
	}
}
