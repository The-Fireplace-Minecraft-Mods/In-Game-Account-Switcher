package the_fireplace.ias.mixin;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Inject(method = "initGui()V", at = @At("HEAD"))
    private void onInit(CallbackInfo ci)
    {
        this.buttonList.add(new GuiButtonWithImage(20, this.width / 2 + 104, (this.height / 4 + 48) + 72 + 12, 20, 20, ""));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onButtonPressed(GuiButton button, CallbackInfo ci)
    {
        if(button.id == 20){
            if(Config.getInstance() == null){
                Config.load();
            }
            Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelector());
        }
    }
}
