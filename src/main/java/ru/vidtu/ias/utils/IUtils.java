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

package ru.vidtu.ias.utils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Various IAS utils.
 *
 * @author VidTu
 */
public final class IUtils {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private IUtils() {
        throw new AssertionError("No instances.");
    }

    /**
     * Gets whether the user should be warned about the name.
     *
     * @param name Target name
     * @return Warning key, {@code null} if none
     */
    public static String warnKey(String name) {
        // Blank.
        if (name.isBlank()) return "ias.nick.blank";

        // Length.
        int length = name.length();
        if (length < 3) return "ias.nick.short";
        if (length > 16) return "ias.nick.long";

        // Chars.
        for (int i = 0; i < length; i++) {
            int c = name.codePointAt(i);
            if (c == '_' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') continue;
            return "ias.nick.chars";
        }

        // Valid.
        return null;
    }

    /**
     * Tests the exceptions from the causal chain.
     *
     * @param root   Causal chain root exception
     * @param tester Exception tester
     * @return {@code true} if the predicate has matched any exception in the causal chain, {@code false} if not, causal loop detected, or causal stack is too big
     */
    public static boolean anyInCausalChain(Throwable root, Predicate<Throwable> tester) {
        // Causal loop detection set.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>(8));

        // 256 is the (arbitrarily-chosen) limit for causal stack.
        for (int i = 0; i < 256; i++) {
            // Break if null (reached the end) or loop detected.
            if (root == null || !dejaVu.add(root)) break;

            // Return if tested exception.
            if (tester.test(root)) return true;

            // Next cause.
            root = root.getCause();
        }

        // Not found. (or reached the 256 limit)
        return false;
    }
}
