package ru.vidtu.ias;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import ru.vidtu.ias.gui.IASConfigScreen;

public class IASModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IASConfigScreen::new;
    }

    public static int buttonOffset() {
        try {
            ModMenuConfig.ModsButtonStyle style = ModMenuConfig.MODS_BUTTON_STYLE.getValue();
            if (style == ModMenuConfig.ModsButtonStyle.ICON) {
                return -48;
            }
        } catch (Throwable ignored) {}
        return -24;
    }
}
