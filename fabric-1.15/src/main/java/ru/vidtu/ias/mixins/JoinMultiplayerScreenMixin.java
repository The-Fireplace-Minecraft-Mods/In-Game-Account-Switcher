package ru.vidtu.ias.mixins;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ias.Config;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

import java.util.List;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen {
    private JoinMultiplayerScreenMixin() {
        super(null);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (minecraft.getUser().getAccessToken().equals("0") || minecraft.getUser().getAccessToken().equals("-")) {
            List<String> list = font.split(I18n.get("ias.offlinemode"), width);
            for (int i = 0; i < list.size(); i++) {
                drawCenteredString(font, list.get(i), width / 2, i * 9 + 1, 16737380);
            }
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        if (Config.showOnMPScreen) {
            addButton(new GuiButtonWithImage(this.width / 2 + 4 + 76 + 79, height - 28, btn -> {
                minecraft.setScreen(new GuiAccountSelector(this));
            }));
        }
    }
}
