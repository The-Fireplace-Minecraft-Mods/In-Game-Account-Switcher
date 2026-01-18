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

// This is the root Stonecutter entrypoint. It configures some
// version-independent aspects of the Stonecutter preprocessor.
// See "build.gradle.kts" for the per-version Gradle buildscript.
// See "settings.gradle.kts" for the Gradle configuration.

// Plugins.
plugins {
    id("dev.kikugie.stonecutter")
}

// Active Stonecutter version. See:
// https://stonecutter.kikugie.dev/wiki/glossary#active-version
// https://stonecutter.kikugie.dev/wiki/glossary#vcs-version
stonecutter active "1.21.11-fabric" /* [SC] DO NOT EDIT */

// Process the JSON files via Stonecutter.
// This is needed for the Mixin configuration.
stonecutter handlers {
    inherit("java", "json")
}
