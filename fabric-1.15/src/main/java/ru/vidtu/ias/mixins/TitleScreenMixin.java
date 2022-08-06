package ru.vidtu.ias.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.Expression;
import ru.vidtu.ias.IASModMenuCompat;
import the_fireplace.ias.IAS;
import the_fireplace.ias.gui.AccountListScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    private Button ias$button;
    private int ias$tx;
    private int ias$ty;
    private TitleScreenMixin() {
        super(null);
        throw new AssertionError("@Mixin instance.");
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void ias$init(CallbackInfo ci) {
        if (Config.titleScreenButton) {
            int bx = width / 2 + 104;
            int by = height / 4 + 48 + 72 + (IAS.modMenu ? IASModMenuCompat.buttonOffset() : -24);
            try {
                bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, width, height);
                by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, width, height);
            } catch (Throwable t) {
                bx = width / 2 + 104;
                by = height / 4 + 48 + 72 + (IAS.modMenu ? IASModMenuCompat.buttonOffset() : -24);
            }
            addButton(ias$button = new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS.IAS_BUTTON,
                    256, 256, btn -> minecraft.setScreen(new AccountListScreen(this)), "In-Game Account Switcher"));
        }
        if (Config.titleScreenText) {
            try {
                ias$tx = (int) Expression.parseWidthHeight(Config.titleScreenTextX, width, height);
                ias$ty = (int) Expression.parseWidthHeight(Config.titleScreenTextY, width, height);
            } catch (Throwable t) {
                ias$tx = width / 2;
                ias$ty = height / 4 + 48 + 72 + 12 + (IAS.modMenu ? 32 : 22);
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void ias$render(int mx, int my, float delta, CallbackInfo ci) {
        if (Config.titleScreenText) {
            if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
                drawString(font, I18n.get("ias.title", minecraft.getUser().getName()), ias$tx, ias$ty, 0xFFCC8888);
            } else if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
                drawString(font, I18n.get("ias.title", minecraft.getUser().getName()), ias$tx - minecraft.font.width(I18n.get("ias.title", minecraft.getUser().getName())), ias$ty, 0xFFCC8888);
            } else {
                drawCenteredString(font, I18n.get("ias.title", minecraft.getUser().getName()), ias$tx, ias$ty, 0xFFCC8888);
            }
        }
        if (ias$button != null && ias$button.isHovered()) {
            renderTooltip("In-Game Account Switcher", mx, my);
        }
    }
}
