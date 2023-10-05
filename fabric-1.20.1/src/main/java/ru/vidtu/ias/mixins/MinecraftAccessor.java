package ru.vidtu.ias.mixins;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("user")
    @Mutable
    void ias$user(User s);

    @Accessor("playerSocialManager")
    @Mutable
    void ias$playerSocialManager(PlayerSocialManager m);

    @Accessor("profileKeyPairManager")
    @Mutable
    void ias$profileKeyPairManager(ProfileKeyPairManager m);

    @Accessor("reportingContext")
    @Mutable
    void ias$reportingContext(ReportingContext c);

    @Accessor("userApiService")
    @Mutable
    void ias$userApiService(UserApiService s);

    @Invoker("createUserApiService")
    UserApiService ias$createUserApiService(YggdrasilAuthenticationService ygg, GameConfig cfg);

    @Accessor("authenticationService")
    YggdrasilAuthenticationService ias$authenticationService();
}
