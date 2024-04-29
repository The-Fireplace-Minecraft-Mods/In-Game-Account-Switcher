/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
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
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.config.IASConfig;

/**
 * Mixin for {@link IASConfig#barNick}.
 *
 * @author VidTu
 */
@SuppressWarnings("DollarSignInName") // <- Mixin.
@Mixin(Minecraft.class)
public final class MinecraftMixin {
    @Shadow @Final private User user;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private MinecraftMixin() {
        throw new AssertionError("No instances.");
    }

    @Inject(method = "createUserApiService", at = @At("HEAD"))
    public void ias$createUserApiService$head(YggdrasilAuthenticationService service, GameConfig config, CallbackInfoReturnable<UserApiService> cir) {
        // Capture service.
        IASMinecraft.service(service);
    }

    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void ias$createTitle$return(CallbackInfoReturnable<String> cir) {
        // Skip if not enabled or not fully loaded.
        if (!IASConfig.barNick || !I18n.exists("ias.bar") || this.user == null) return;

        // Modify otherwise.
        String original = cir.getReturnValue();
        cir.setReturnValue(I18n.get("ias.bar", original, this.user.getName()));
    }
}
