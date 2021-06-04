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
		try {
			File ms = new File(mc.runDirectory, ".iasms");
			if (!ms.exists()) return;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ms));
			msaccounts.add((MicrosoftAccount) ois.readObject());
			ois.close();
		} catch (Throwable t) {
			System.err.println("Unable to load microsoft accounts.");
			t.printStackTrace();
		}
	}
	
	public static void save(MinecraftClient mc) {
		try {
			File ms = new File(mc.runDirectory, ".iasms");
			if (msaccounts.isEmpty()) {
				if (ms.exists()) ms.delete();
				return;
			}
			try {
				if (ms.exists()) Files.setAttribute(ms.toPath(), "dos:hidden", false);
			} catch (Throwable t) {}
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ms));
			for (MicrosoftAccount acc : msaccounts) {
				oos.writeObject(acc);
			}
			oos.close();
			try {
				Files.setAttribute(ms.toPath(), "dos:hidden", true);
			} catch (Throwable t) {}
		} catch (Throwable t) {
			System.err.println("Unable to load microsoft accounts.");
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
		mc.openScreen(new MSAuthScreen(mc.currentScreen, EncryptionTools.decode(accessToken), EncryptionTools.decode(refreshToken)));
		return null;
	}
}
