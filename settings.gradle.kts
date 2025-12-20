/*
 * In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
 * allows you to change your logged in account in-game, without restarting it.
 *
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
    id("dev.kikugie.stonecutter") version "0.8"
}

rootProject.name = "In-Game Account Switcher"

// Stonecutter.
val types = listOf("fabric", "forge", "neoforge")
val versions = listOf("1.21.11", "1.21.10", "1.21.8", "1.21.5", "1.21.4", "1.21.3", "1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.2", "1.18.2")
val ignored = mutableListOf<String>()
stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        for (version in versions) {
            for (type in types) {
                val id = "$version-$type"
                val subPath = file("versions/$id")
                if (subPath.resolve(".ignored").isFile || !subPath.isDirectory) { // TODO(VidTu): Once the migration finishes, delete the second check.
                    ignored.add(id)
                    continue
                }
                version("$version-$type", version)
            }
        }
        vcsVersion = "${versions[0]}-${types[0]}"
    }
}
logger.warn("Ignored versions: ${ignored.joinToString()}")

// Migration helper START.
// Legacy.
include("legacy_shared")
project(":legacy_shared").projectDir = file("legacy/shared")
val oldTypes = listOf("fabric", "forge", "neoforge", "root")
for (version in versions) {
    for (type in oldTypes) {
        val subPath = file("legacy/$version/$type")
        if (!subPath.isDirectory) continue
        include("legacy_$version-$type")
        project(":legacy_$version-$type").projectDir = subPath
    }
}
// Migration helper END.
