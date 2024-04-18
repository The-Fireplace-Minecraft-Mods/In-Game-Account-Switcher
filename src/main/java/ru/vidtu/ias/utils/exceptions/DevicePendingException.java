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

package ru.vidtu.ias.utils.exceptions;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * A runtime exception indicating repolling intention.
 *
 * @author VidTu
 */
public final class DevicePendingException extends RuntimeException {
    /**
     * Creates a new exception.
     *
     * @param message Target message
     */
    public DevicePendingException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param message Target message
     * @param cause   Suppressed exception cause
     */
    public DevicePendingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Gets whether the pending exception is in the causal chain.
     *
     * @param root Causal chain root exception
     * @return Whether the pending exception is in the causal chain
     */
    public static boolean hasPending(Throwable root) {
        // Causal loop detection set.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>(8));

        // 256 is the (arbitrarily-chosen) limit for causal stack.
        for (int i = 0; i < 256; i++) {
            // Break if null (reached the end) or loop detected.
            if (root == null || !dejaVu.add(root)) break;

            // Return if pending exception.
            if (root instanceof DevicePendingException) return true;

            // Next cause.
            root = root.getCause();
        }

        // Not found. (or reached the 256 limit)
        return false;
    }
}
