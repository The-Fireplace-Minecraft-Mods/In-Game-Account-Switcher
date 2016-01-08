package the_fireplace.ias.events;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

/**
 * @author The_Fireplace
 */
public class ForgeEvents {
	@SubscribeEvent
	public void preGui(InitGuiEvent.Pre event){
		if(event.gui instanceof GuiMainMenu){
			Config.save();
		}
	}
	@SubscribeEvent
	public void guiEvent(InitGuiEvent.Post event){
		GuiScreen gui = event.gui;
		if(gui instanceof GuiMainMenu){
			event.buttonList.add(new GuiButtonWithImage(20, gui.width / 2 + 104, (gui.height / 4 + 48) + 72 + 12, 20, 20, ""));
		}
	}
	@SubscribeEvent
	public void onClick(ActionPerformedEvent event){
		if(event.gui instanceof GuiMainMenu && event.button.id == 20){
			if(Config.getInstance() == null){
				Config.load();
			}
			if(Minecraft.getMinecraft() == null){
				System.out.println("Error: Server call");
			}
			Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelector());
		}
	}
}
