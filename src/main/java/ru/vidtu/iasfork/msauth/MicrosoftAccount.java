package ru.vidtu.iasfork.msauth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import the_fireplace.iasencrypt.EncryptionTools;

public class MicrosoftAccount implements Account, Serializable {
	private static final long serialVersionUID = 5836857834701515666L;
	public static transient ArrayList<MicrosoftAccount> msaccounts = new ArrayList<MicrosoftAccount>();
	
	public String username;
	public String accessToken;
	public String refreshToken;
	
	public MicrosoftAccount(String name, String token, String refresh) {
		this.username = name;
		this.accessToken = token;
		this.refreshToken = refresh;
	}

	public static void load(MinecraftClient mc) {
		//Loads MS account from 7.1.0-pre2 thru 7.1.1.
		//See: https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/issues/40
		try {
			File oldData = new File(mc.runDirectory, ".iasms");
			if (oldData.exists()) {
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(oldData))) {
					msaccounts.add((MicrosoftAccount) ois.readObject());
				}
				oldData.delete();
			}
		} catch (Throwable t) {
			System.err.println("Unable to load old microsoft accounts.");
			t.printStackTrace();
		}
		
		//Loads new account data
		try {
			File ms = new File(mc.runDirectory, ".iasms_v2");
			if (ms.exists()) {
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ms))) {
					int amount = ois.readInt();
					for (int i = 0; i < amount; i++) {
						msaccounts.add((MicrosoftAccount) ois.readObject());
					}
				}
			}
		} catch (Throwable t) {
			System.err.println("Unable to load microsoft accounts.");
			t.printStackTrace();
		}
	}
	
	public static void save(MinecraftClient mc) {
		try {
			File ms = new File(mc.runDirectory, ".iasms_v2");
			try {
				Files.setAttribute(ms.toPath(), "dos:hidden", false); //Writing to hidden files is not allowed (at least in Windows)
			} catch (Throwable t) {}
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ms))) {
				oos.writeInt(msaccounts.size());
				for (MicrosoftAccount acc : msaccounts) {
					oos.writeObject(acc);
				}
			}
			try {
				Files.setAttribute(ms.toPath(), "dos:hidden", true);
			} catch (Throwable t) {}
		} catch (Throwable t) {
			System.err.println("Unable to save microsoft accounts.");
			t.printStackTrace();
		}
	}

	@Override
	public String alias() {
		return username;
	}

	@Override
	public Throwable login() {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.setScreen(new MSAuthScreen(mc.currentScreen, EncryptionTools.decode(accessToken), EncryptionTools.decode(refreshToken)));
		return null;
	}
}
