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

package ru.vidtu.ias.auth.handlers;

import ru.vidtu.ias.auth.LoginData;

import java.util.concurrent.CompletableFuture;

/**
 * Handler for logins.
 *
 * @author VidTu
 * @apiNote All methods in this class can be called from another thread
 */
public interface LoginHandler {
    /**
     * Gets the cancelled state.
     *
     * @return Whether the authentication is cancelled
     */
    boolean cancelled();

    /**
     * Changes the authentication stage.
     *
     * @param stage New auth stage translation key
     * @param args  New auth stage translation args
     */
    void stage(String stage, Object... args);

    /**
     * Requests an encryption password.
     *
     * @return Future that will complete with password string on password enter, with {@code null} on cancel, exceptionally on error
     */
    CompletableFuture<String> password();

    /**
     * Called when an authentication has performed successfully.
     *
     * @param data    Auth data
     * @param changed Whether the storage has been modified and may require saving
     */
    void success(LoginData data, boolean changed);

    /**
     * Called when an authentication has failed.
     *
     * @param error Failure reason
     */
    void error(Throwable error);
}
