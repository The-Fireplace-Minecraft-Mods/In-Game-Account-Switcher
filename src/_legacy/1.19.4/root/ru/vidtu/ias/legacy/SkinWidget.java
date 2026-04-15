/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2026 VidTu
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

package ru.vidtu.ias.legacy;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Supplier;

/**
 * Legacy widget class of a player skin.
 *
 * @author VidTu
 */
public class SkinWidget extends AbstractWidget {
    /**
     * Classic model.
     */
    private final PlayerModel<?> classic;

    /**
     * Slim model.
     */
    private final PlayerModel<?> slim;

    /**
     * Skin provider.
     */
    private final Supplier<Skin> skin;

    /**
     * Skin rotation X.
     */
    private float rotationX = -5.0F;

    /**
     * Skin rotation Y.
     */
    private float rotationY = 30.0F;

    /**
     * Creates a new skin widget.
     *
     * @param x      Target X
     * @param y      Target Y
     * @param width  Widget width
     * @param height Widget height
     * @param models Entity models
     * @param skin   Skin provider
     */
    public SkinWidget(int x, int y, int width, int height, EntityModelSet models, Supplier<Skin> skin) {
        // Assign.
        super(x, y, width, height, CommonComponents.EMPTY);
        this.skin = skin;

        // Prepare models.
        this.classic = new PlayerModel<>(models.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel<>(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.classic.young = false;
        this.slim.young = false;
    }

    @Override
    public void renderWidget(PoseStack pose, int mouseX, int mouseY, float delta) {
        // Prepare.
        float scale = this.getHeight() / 2.125F;
        pose.pushPose();
        pose.translate(this.getX() + this.getWidth() / 2.0F, this.getY() + this.getHeight(), 100.0F);
        pose.scale(scale, scale, scale);
        pose.translate(0.0F, -0.0625F, 0.0F);
        Matrix4f matrix = pose.last().pose();
        matrix.rotateAround(Axis.XP.rotationDegrees(this.rotationX), 0.0F, -1.0625F, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(this.rotationY));

        // Render.
        Skin skin = this.skin.get();
        Lighting.setupForEntityInInventory();
        pose.pushPose();
        pose.mulPoseMatrix(new Matrix4f().scaling(1.0F, 1.0F, -1.0F));
        pose.translate(0.0F, -1.5F, 0.0F);
        PlayerModel<?> model = skin.slim() ? this.slim : this.classic;
        RenderType renderType = model.renderType(skin.skin());
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        model.renderToBuffer(pose, bufferSource.getBuffer(renderType), 0xF000F0, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        Lighting.setupFor3DItems();

        // End.
        pose.popPose();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.rotationX = Mth.clamp(this.rotationX - (float)dragY * 2.5F, -50.0F, 50.0F);
        this.rotationY += (float)dragX * 2.5F;
    }

    @Override
    public void playDownSound(SoundManager manager) {
        // NO-OP
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        // NO-OP
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return null;
    }
}
