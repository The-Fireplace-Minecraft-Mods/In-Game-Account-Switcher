package ru.vidtu.ias.mixins;

import com.mojang.authlib.minecraft.SocialInteractionsService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.main.GameConfig;
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

    @Accessor("socialInteractionsService")
    @Mutable
    void ias$socialInteractionsService(SocialInteractionsService s);

    @Invoker("createSocialInteractions")
    SocialInteractionsService ias$createSocialInteractions(YggdrasilAuthenticationService ygg, GameConfig cfg);
}
