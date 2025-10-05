/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.mixins;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

/**
 * Mixin accessor for changing session-related data in {@link Minecraft}.
 *
 * @author VidTu
 */
@SuppressWarnings("DollarSignInName") // <- Mixin.
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    /**
     * Sets the game services.
     *
     * @param services New services
     * @see Minecraft#services()
     */
    @Accessor("services")
    @Mutable
    void ias$services(Services services);

    /**
     * Sets the game user.
     *
     * @param user New user
     * @see Minecraft#getUser()
     */
    @Accessor("user")
    @Mutable
    void ias$user(User user);

    /**
     * Sets the profile future.
     *
     * @param future New profile future
     * @see Minecraft#getGameProfile()
     */
    @Accessor("profileFuture")
    @Mutable
    void ias$profileFuture(CompletableFuture<ProfileResult> future);

    /**
     * Sets the user API service.
     *
     * @param service New user API service
     */
    @Accessor("userApiService")
    @Mutable
    void ias$userApiService(UserApiService service);

    /**
     * Sets the user properties future.
     *
     * @param future New user properties future
     */
    @Accessor("userPropertiesFuture")
    @Mutable
    void ias$userPropertiesFuture(CompletableFuture<UserApiService.UserProperties> future);

    /**
     * Sets the player social manager.
     *
     * @param manager New player social manager
     * @see Minecraft#getPlayerSocialManager()
     */
    @Accessor("playerSocialManager")
    @Mutable
    void ias$playerSocialManager(PlayerSocialManager manager);

    /**
     * Sets the telemetry manager.
     *
     * @param manager New client telemetry manager
     * @see Minecraft#getTelemetryManager()
     */
    @Accessor("telemetryManager")
    @Mutable
    void ias$telemetryManager(ClientTelemetryManager manager);

    /**
     * Sets the new profile key pair manager.
     *
     * @param manager New profile key pair manager
     * @see Minecraft#getProfileKeyPairManager()
     */
    @Accessor("profileKeyPairManager")
    @Mutable
    void ias$profileKeyPairManager(ProfileKeyPairManager manager);

    /**
     * Sets the new reporting context.
     *
     * @param context New reporting context
     * @see Minecraft#getReportingContext()
     */
    @Accessor("reportingContext")
    @Mutable
    void ias$reportingContext(ReportingContext context);
}
