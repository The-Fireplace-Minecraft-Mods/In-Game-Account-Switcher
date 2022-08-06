package ru.vidtu.ias;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import ru.vidtu.ias.gui.IASConfigScreen;

public class IASModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IASConfigScreen::new;
    }

    public static int buttonOffset() {
        return -24;
    }
}
