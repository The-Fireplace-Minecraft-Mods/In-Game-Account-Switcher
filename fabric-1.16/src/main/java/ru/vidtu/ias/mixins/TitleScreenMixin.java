package ru.vidtu.ias.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.IASModMenuCompat;
import the_fireplace.ias.IAS;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    private static int textX, textY;

    private TitleScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        try {
            if (StringUtils.isNotBlank(Config.textX) && StringUtils.isNotBlank(Config.textY)) {
                textX = Integer.parseInt(Config.textX);
                textY = Integer.parseInt(Config.textY);
            } else {
                textX = width / 2;
                textY = height / 4 + 48 + 72 + 12 + (IAS.MOD_MENU ? 32 : 22);
            }
        } catch (Throwable t) {
            textX = width / 2;
            textY = height / 4 + 48 + 72 + 12 + (IAS.MOD_MENU ? 32 : 22);
        }
        if (Config.showOnTitleScreen) {
            int buttonX = width / 2 + 104;
            int buttonY = height / 4 + 48 + 72 + (IAS.MOD_MENU ? IASModMenuCompat.buttonOffset() : -24);
            try {
                if (StringUtils.isNotBlank(Config.btnX) && StringUtils.isNotBlank(Config.btnY)) {
                    buttonX = Integer.parseInt(Config.btnX);
                    buttonY = Integer.parseInt(Config.btnY);
                }
            } catch (Throwable t) {
                buttonX = width / 2 + 104;
                buttonY = height / 4 + 48 + 72 + (IAS.MOD_MENU ? IASModMenuCompat.buttonOffset() : -24);
            }
            addButton(new GuiButtonWithImage(buttonX, buttonY, btn -> minecraft.setScreen(new GuiAccountSelector(this))));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(PoseStack ms, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawCenteredString(ms, font, new TranslatableComponent("ias.loggedinas", minecraft.getUser().getName()), textX, textY, 0xFFCC8888);
    }
}
