package fludevity.ingameaccountswitcher.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Accessor("authenticationService")
	@Mutable
	YggdrasilAuthenticationService getAuthenticationService();

	@Accessor("userApiService")
	@Mutable
	UserApiService getUserApiService();

	@Accessor("userApiService")
	@Mutable
	void setUserApiService(UserApiService uas);

	@Accessor("profileKeyPairManager")
	@Mutable
	void setProfileKeyPairManager(ProfileKeyPairManager uas);
}
