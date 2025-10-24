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

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.ias.screen.PopupBox;

/**
 * Legacy Mixin for hacky way to disable bordering of {@link EditBox} without changing offsets.
 * This works because it checks offsets via field access and rendering via method.
 *
 * @author VidTu
 */
@SuppressWarnings("DollarSignInName") // <- Mixin.
@Mixin(EditBox.class)
public final class EditBoxMixin {
    @SuppressWarnings({"ConstantValue", "InstanceofThis"}) // <- Very hacky check for popup boxes.
    @Inject(method = "isBordered", at = @At("HEAD"), cancellable = true)
    public void ias$isBordered$head(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PopupBox)) return;
        cir.setReturnValue(false);
    }
}
