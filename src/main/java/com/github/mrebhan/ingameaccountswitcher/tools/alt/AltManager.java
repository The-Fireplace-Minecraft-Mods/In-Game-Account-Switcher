package com.github.mrebhan.ingameaccountswitcher.tools.alt;

import com.github.mrebhan.ingameaccountswitcher.MR;
import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import the_fireplace.ias.account.AlreadyLoggedInException;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.iasencrypt.EncryptionTools;

import java.util.UUID;
/**
 * @author MRebhan
 * @author The_Fireplace
 */
public class AltManager {
	private static AltManager manager = null;
	private final UserAuthentication auth;

	private AltManager() {
		UUID uuid = UUID.randomUUID();
		AuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), uuid.toString());
		auth = authService.createUserAuthentication(Agent.MINECRAFT);
		authService.createMinecraftSessionService();
	}

	public static AltManager getInstance() {
		if (manager == null) {
			manager = new AltManager();
		}

		return manager;
	}

	public Throwable setUser(String username, String password) {
		Throwable throwable = null;
		if(!Minecraft.getMinecraft().getSession().getUsername().equals(EncryptionTools.decode(username)) || Minecraft.getMinecraft().getSession().getToken().equals("0")){
			if (!Minecraft.getMinecraft().getSession().getToken().equals("0"))
			{
				for (AccountData data : AltDatabase.getInstance().getAlts())
				{
					if (data.alias.equals(Minecraft.getMinecraft().getSession().getUsername()) && data.user.equals(username))
					{
						throwable = new AlreadyLoggedInException();
						return throwable;
					}
				}
			}
			this.auth.logOut();
			this.auth.setUsername(EncryptionTools.decode(username));
			this.auth.setPassword(EncryptionTools.decode(password));
			try {
				this.auth.logIn();
				Session session = new Session(this.auth.getSelectedProfile().getName(), UUIDTypeAdapter.fromUUID(auth.getSelectedProfile().getId()), this.auth.getAuthenticatedToken(), this.auth.getUserType().getName());
				MR.setSession(session);
				for (int i = 0; i < AltDatabase.getInstance().getAlts().size(); i++) {
					AccountData data = AltDatabase.getInstance().getAlts().get(i);
					if (data.user.equals(username) && data.pass.equals(password)) {
						data.alias = session.getUsername();
					}
				}
			} catch (Exception e) {
				throwable = e;
			}
		}else{
			if(!ConfigValues.ENABLERELOG)
				throwable = new AlreadyLoggedInException();
		}
		return throwable;
	}

	public void setUserOffline(String username) {
		this.auth.logOut();
		Session session = new Session(username, username, "0", "legacy");
		try {
			MR.setSession(session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
