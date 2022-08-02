package fludevity.ingameaccountswitcher.utils;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import fludevity.ingameaccountswitcher.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;

public class OrwellAuth {
    public static void loginOnline(Minecraft mc) throws AuthenticationException {
        ((MinecraftAccessor) mc).setUserApiService(((MinecraftAccessor) mc).getAuthenticationService().createUserApiService(mc.getUser().getAccessToken()));
        ((MinecraftAccessor) mc).setProfileKeyPairManager(new ProfileKeyPairManager(((MinecraftAccessor) mc).getUserApiService(), mc.getUser().getProfileId(), mc.gameDirectory.toPath()));
    }
    public static void loginOffline(Minecraft mc) {
        ((MinecraftAccessor) mc).setUserApiService(UserApiService.OFFLINE);
        ((MinecraftAccessor) mc).setProfileKeyPairManager(new ProfileKeyPairManager(((MinecraftAccessor) mc).getUserApiService(), mc.getUser().getProfileId(), mc.gameDirectory.toPath()));
    }
}
