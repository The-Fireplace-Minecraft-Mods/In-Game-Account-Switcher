package ru.vidtu.ias.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.Expression;
import the_fireplace.ias.IAS;
import the_fireplace.ias.gui.AccountListScreen;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen {
    private Button ias$button;
    private JoinMultiplayerScreenMixin() {
        super(null);
        throw new AssertionError("@Mixin instance.");
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void ias$init(CallbackInfo ci) {
        if (Config.multiplayerScreenButton) {
            int bx = width / 2 + 4 + 76 + 79;
            int by = height - 28;
            try {
                bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, width, height);
                by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, width, height);
            } catch (Throwable t) {
                bx = width / 2 + 4 + 76 + 79;
                by = height - 28;
            }
            addButton(ias$button = new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS.IAS_BUTTON,
                    256, 256, btn -> minecraft.setScreen(new AccountListScreen(this)),
                    "In-Game Account Switcher"));
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void ias$render(int mx, int my, float delta, CallbackInfo ci) {
        if (ias$button != null && ias$button.isHovered()) {
            renderTooltip("In-Game Account Switcher", mx, my);
        }
    }
}
