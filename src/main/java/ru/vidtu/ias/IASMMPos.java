package ru.vidtu.ias;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfig.ModsButtonStyle;

public class IASMMPos {
	public static int buttonOffset() {
		try {
			ModsButtonStyle style = ModMenuConfig.MODS_BUTTON_STYLE.getValue();
			if (style == ModsButtonStyle.ICON) {
				return -48;
			}
		} catch (Throwable t) {}
		return -12;
	}
}
