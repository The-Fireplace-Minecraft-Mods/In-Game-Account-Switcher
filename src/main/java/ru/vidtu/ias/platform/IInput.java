/*
 * In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
 * allows you to change your logged in account in-game, without restarting it.
 *
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

package ru.vidtu.ias.platform;

//? if >=26.3 {
import com.mojang.blaze3d.platform.InputConstants;
//?} else
/*import org.lwjgl.glfw.GLFW;*/

public final class IInput {
    public static final int DOWN = /*? if >=26.3 {*/ InputConstants.KEY_DOWN /*?} else {*/ /*GLFW.GLFW_KEY_DOWN*//*?}*/;
    public static final int PAGE_DOWN = /*? if >=26.3 {*/ InputConstants.KEY_PAGEDOWN /*?} else {*/ /*GLFW.GLFW_KEY_PAGE_DOWN*//*?}*/;
    public static final int UP = /*? if >=26.3 {*/ InputConstants.KEY_UP /*?} else {*/ /*GLFW.GLFW_KEY_UP*//*?}*/;
    public static final int PAGE_UP = /*? if >=26.3 {*/ InputConstants.KEY_PAGEUP /*?} else {*/ /*GLFW.GLFW_KEY_PAGE_UP*//*?}*/;
    public static final int C = /*? if >=26.3 {*/ InputConstants.KEY_C /*?} else {*/ /*GLFW.GLFW_KEY_C*//*?}*/;
    public static final int DELETE = /*? if >=26.3 {*/ InputConstants.KEY_DELETE /*?} else {*/ /*GLFW.GLFW_KEY_DELETE*//*?}*/;
    public static final int SUBTRACT = /*? if >=26.3 {*/ InputConstants.KEY_MINUS /*?} else {*/ /*GLFW.GLFW_KEY_KP_SUBTRACT*//*?}*/;
    public static final int N = /*? if >=26.3 {*/ InputConstants.KEY_N /*?} else {*/ /*GLFW.GLFW_KEY_N*//*?}*/;
    public static final int ADD = /*? if >=26.3 {*/ InputConstants.KEY_ADD /*?} else {*/ /*GLFW.GLFW_KEY_KP_ADD*//*?}*/;
    public static final int R = /*? if >=26.3 {*/ InputConstants.KEY_R /*?} else {*/ /*GLFW.GLFW_KEY_R*//*?}*/;
    public static final int MULTIPLY = /*? if >=26.3 {*/ InputConstants.KEY_MULTIPLY /*?} else {*/ /*GLFW.GLFW_KEY_KP_MULTIPLY*//*?}*/;
    public static final int Y = /*? if >=26.3 {*/ InputConstants.KEY_Y /*?} else {*/ /*GLFW.GLFW_KEY_Y*//*?}*/;
    public static final int LEFT_ALT = /*? if >=26.3 {*/ InputConstants.KEY_LALT /*?} else {*/ /*GLFW.GLFW_KEY_LEFT_ALT*//*?}*/;
    public static final int RIGHT_ALT = /*? if >=26.3 {*/ InputConstants.KEY_RALT /*?} else {*/ /*GLFW.GLFW_KEY_RIGHT_ALT*//*?}*/;
    public static final int ENTER = /*? if >=26.3 {*/ InputConstants.KEY_RETURN /*?} else {*/ /*GLFW.GLFW_KEY_ENTER*//*?}*/;
    public static final int NUMPAD_ENTER = /*? if >=26.3 {*/ InputConstants.KEY_NUMPADENTER /*?} else {*/ /*GLFW.GLFW_KEY_KP_ENTER*//*?}*/;

    private IInput() {}

    public static String yName() {
        //? if >=26.3 {
        return "Y";
        //?} else
        /*return GLFW.glfwGetKeyName(GLFW.GLFW_KEY_Y, GLFW.GLFW_KEY_UNKNOWN);*/
    }
}
