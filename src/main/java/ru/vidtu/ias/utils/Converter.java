package ru.vidtu.ias.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;

import net.minecraft.client.MinecraftClient;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.account.MojangAccount;
import ru.vidtu.ias.account.OfflineAccount;
import the_fireplace.ias.IAS;
import the_fireplace.ias.account.ExtendedAccountData;

/**
 * Convert old accounts to new accounts.
 * @author VidTu
 * @apiNote Should be removed some day or tweaked in way of not requiring another classes.
 */
@SuppressWarnings("deprecation")
public class Converter {
	/**
	 * Find and convert all old accounts to new accounts.
	 * @param mc Minecraft client instance
	 */
	public static void convert(MinecraftClient mc) {
		//Convert Mojang/Offline accounts.
		try { //Convert shared IASX.
			File iasx = new File(oldSharedFolder(), ".iasx");
			if (iasx.exists()) {
				File iasp = new File(iasx.getParentFile(), ".iasp");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(iasx))) {
					com.github.mrebhan.ingameaccountswitcher.tools.Config o = (com.github.mrebhan.ingameaccountswitcher.tools.Config) ois.readObject();
					for (AccountData ad : o.field_218893_c.get(0).obj2.altList) {
						Config.accounts.add(convertMojang(iasp, ad));
					}
				}
				Config.save(mc);
				iasx.delete();
				IAS.LOG.info("Converted shared IASX");
			}
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to convert shared IASX", t);
		}
		try { //Convert non-shared IASX.
			File iasx = new File(mc.runDirectory, ".iasx");
			if (iasx.exists()) {
				File iasp = new File(mc.runDirectory, ".iasp");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(iasx))) {
					com.github.mrebhan.ingameaccountswitcher.tools.Config o = (com.github.mrebhan.ingameaccountswitcher.tools.Config) ois.readObject();
					for (AccountData ad : o.field_218893_c.get(0).obj2.altList) {
						Config.accounts.add(convertMojang(iasp, ad));
					}
				}
				Config.save(mc);
				iasx.delete();
				IAS.LOG.info("Converted non-shared IASX");
			}
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to convert non-shared IASX", t);
		}
		//Convert Microsoft accounts.
		try { //Convert bugged v1 (singleton) Microsoft account storage using shared pass.
			File iasms = new File(mc.runDirectory, ".iasms");
			if (iasms.exists()) {
				File iasp = new File(oldSharedFolder(), ".iasp");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(iasms))) {
					Config.accounts.add(convertMicrosoft(iasp, (ru.vidtu.iasfork.msauth.MicrosoftAccount) ois.readObject()));
				}
				Config.save(mc);
				iasms.delete();
				IAS.LOG.info("Converted old (v1) IASMS (shared)");
			}
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to convert old (v1) IASMS (shared)", t);
		}
		try { //Convert bugged v1 (singleton) Microsoft account storage using mcdir pass.
			File iasms = new File(mc.runDirectory, ".iasms");
			if (iasms.exists()) {
				File iasp = new File(mc.runDirectory, ".iasp");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(iasms))) {
					Config.accounts.add(convertMicrosoft(iasp, (ru.vidtu.iasfork.msauth.MicrosoftAccount) ois.readObject()));
				}
				Config.save(mc);
				iasms.delete();
				IAS.LOG.info("Converted old (v1) IASMS (mcdir)");
			}
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to convert old (v1) IASMS (mcdir)", t);
		}
		try { //Convert v2 Microsoft account storage using shared pass.
			File iasms = new File(mc.runDirectory, ".iasms_v2");
			if (iasms.exists()) {
				File iasp = new File(oldSharedFolder(), ".iasp");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(iasms))) {
					int ttl = ois.readInt();
					for (int i = 0; i < ttl; i++) {
						Config.accounts.add(convertMicrosoft(iasp, (ru.vidtu.iasfork.msauth.MicrosoftAccount) ois.readObject()));
					}
				}
				Config.save(mc);
				iasms.delete();
				IAS.LOG.info("Converted new (v2) IASMS (shared)");
			}
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to convert new (v2) IASMS (shared)", t);
		}
		try { //Convert v2 Microsoft account storage using mcdir pass.
			File iasms = new File(mc.runDirectory, ".iasms_v2");
			if (iasms.exists()) {
				File iasp = new File(mc.runDirectory, ".iasp");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(iasms))) {
					int ttl = ois.readInt();
					for (int i = 0; i < ttl; i++) {
						Config.accounts.add(convertMicrosoft(iasp, (ru.vidtu.iasfork.msauth.MicrosoftAccount) ois.readObject()));
					}
				}
				Config.save(mc);
				iasms.delete();
				IAS.LOG.info("Converted new (v2) IASMS (mcdir)");
			}
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to convert new (v2) IASMS (mcdir)", t);
		}
	}
	
	/**
	 * Convert old Mojang account to new account.
	 * @param pwdfile Possible password file, <code>null</code> if none exist
	 * @param ad Old account data
	 * @return New account data
	 * @throws Exception If we're unable to convert account
	 */
	public static Account convertMojang(File pwdfile, AccountData ad) throws Exception {
		String login = pwdfile != null && pwdfile.isFile()?decrypt(pwdfile, ad.user):ad.user;
		String pass = pwdfile != null && pwdfile.isFile()?decrypt(pwdfile, ad.pass):ad.pass;
		if (pass.isEmpty()) {
			OfflineAccount oa = new OfflineAccount(login);
			if (ad instanceof ExtendedAccountData) {
				oa.uses = ((ExtendedAccountData) ad).useCount;
				int[] lastused = ((ExtendedAccountData) ad).lastused;
				oa.lastUse = ZonedDateTime.of(lastused[2], lastused[0], lastused[1], 0, 0, 0, 0, ZoneId.systemDefault()).toEpochSecond() * 1000L;
			}
			return oa;
		}
		MojangAccount ma = Auth.authMojang(login, pass);
		if (ad instanceof ExtendedAccountData) {
			ma.uses = ((ExtendedAccountData) ad).useCount;
			int[] lastused = ((ExtendedAccountData) ad).lastused;
			ma.lastUse = ZonedDateTime.of(lastused[2], lastused[0], lastused[1], 0, 0, 0, 0, ZoneId.systemDefault()).toEpochSecond() * 1000L;
		}
		return ma;
	}
	
	/**
	 * Convert old Microsoft account to new account.
	 * @param pwdfile Possible password file, <code>null</code> if none exist
	 * @param oma Old account data
	 * @return New account data
	 * @throws Throwable If we're unable to convert account
	 */
	public static MicrosoftAccount convertMicrosoft(File pwdfile, ru.vidtu.iasfork.msauth.MicrosoftAccount oma) throws Throwable {
		MicrosoftAccount ma;
		if (pwdfile != null && pwdfile.isFile()) {
			//Name is not encrypted.
			ma = new MicrosoftAccount(oma.username, decrypt(pwdfile, oma.accessToken), decrypt(pwdfile, oma.refreshToken), null); //UUID is null, but it will be got on first login.
		} else {
			ma = new MicrosoftAccount(oma.username, oma.accessToken, oma.refreshToken, null); //UUID is null, but it will be got on first login.
		}
		ma.syncRefresh();
		return ma;
	}
	
	/**
	 * Get old shared folder. We don't use shared folders anymore.
	 * @return System-dependent shared folder
	 * @see https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/414a8020288fdab798f88d325e614a2066b4dc36/src/main/java/the_fireplace/iasencrypt/Standards.java
	 */
	public static File oldSharedFolder() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return new File(System.getenv("AppData"));
		}
		if (os.contains("mac")) {
			return new File(System.getProperty("user.home") + "/Library/Application Support");
		}
		return new File(System.getProperty("user.home"));
	}
	
	public static String decrypt(File pwdfile, String s) throws Exception {
		String pass;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pwdfile))) {
			pass = (String) ois.readObject();
		}
		byte[] data = Base64.getDecoder().decode(s);
		Cipher cipher = Cipher.getInstance("AES");
		String password = "DownWithTheLexManosIsAlwaysRightFoundation" + pass + "DownWithTheLexManosIsAlwaysRightFoundation"; //LexManosIsAlwaysWrong
		byte[] key = Arrays.copyOf(MessageDigest.getInstance("SHA-512").digest(password.getBytes(StandardCharsets.UTF_8)), 16);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
		return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
	}
}
