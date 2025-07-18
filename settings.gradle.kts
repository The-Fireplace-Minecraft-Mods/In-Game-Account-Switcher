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

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") // Architectury Loom. (Fabric dependencies)
        maven("https://maven.architectury.dev/") // Architectury Loom.
        maven("https://maven.minecraftforge.net/") // Architectury Loom. (Forge dependencies)
        maven("https://maven.neoforged.net/releases/") // Architectury Loom. (NeoForge dependencies)
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "In-Game Account Switcher"

val types = listOf("fabric", "forge", "neoforge", "root")
val versions = listOf("1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.2", "1.18.2")
for (version in versions) {
    for (type in types) {
        val subPath = file("$version/$type")
        if (!subPath.isDirectory) continue
        include("$version-$type")
        project(":$version-$type").projectDir = subPath
    }
}
