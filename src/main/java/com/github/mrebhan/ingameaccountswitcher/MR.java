package com.github.mrebhan.ingameaccountswitcher;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

/**
 * @author MRebhan
 */
public class MR {
	public static void init(){
		Config.load();
	}
	public static void setSession(Session s) throws Exception {
		Minecraft.getMinecraft().session = s;
	}
}
