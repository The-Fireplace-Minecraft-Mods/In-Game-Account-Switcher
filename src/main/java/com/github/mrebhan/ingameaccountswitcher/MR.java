package com.github.mrebhan.ingameaccountswitcher;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import ru.vidtu.iasfork.mixins.MinecraftClientAccessor;
import ru.vidtu.iasfork.msauth.MicrosoftAccount;

/**
 * @author MRebhan
 */
public class MR {
	public static void init(){
		Config.load();
		MicrosoftAccount.load(MinecraftClient.getInstance());
	}
	public static void setSession(Session s) {
		try {
			((MinecraftClientAccessor)MinecraftClient.getInstance()).setSession(s);
		} catch (Throwable t) {
			
		}
	}
}
