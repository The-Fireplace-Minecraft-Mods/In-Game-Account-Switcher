package ru.vidtu.ias;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import ru.vidtu.ias.gui.IASConfigScreen;

public class IASModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> new IASConfigScreen(screen);
	}

}
