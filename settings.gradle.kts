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

// This is the root Gradle entrypoint. It installs the Stonecutter preprocessor,
// and various root Gradle things, as well as includes and generates every
// virtual subproject by the Stonecutter. Also includes compile-time project.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.

// Plugins.
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
    id("dev.kikugie.stonecutter") version "0.9"
}

// Project.
rootProject.name = "In-Game Account Switcher"

// Prepare the list of versions and types.
val types = listOf("fabric", "forge", "neoforge")
val versions = listOf(/*FIXME "26.2", */"26.1.2", "1.21.11", "1.21.10", "1.21.8", "1.21.5", "1.21.4", "1.21.3", "1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.2", "1.18.2")

// Actively supported version system. See README.md for the support policy.
// Depends on the "ru.vidtu.ias.legacy" boolean system property:
// - "false" (default): Compile only versions listed in "supportedVersions".
// - "true": Compile all versions listed in "versions".
// If "only" version feature is used, this is ignored.
val supportedVersions = setOf(/*FIXME "26.2", */"26.1.2", "1.21.11", "1.21.1", "1.20.1")
require(versions.containsAll(supportedVersions)) { "Not all actively supported versions '${supportedVersions}' are listed in all supported versions '${versions}'." }
val includeLegacyVersions = System.getProperty("ru.vidtu.ias.legacy").toBoolean()

// Process the "only" version feature.
// Pass the "ru.vidtu.ias.only" system property with "<version>-<type>"
// to the Gradle daemon and it will compile only* the required version,
// which may reduce the build time if you don't need other versions.
// (* Sometimes, the latest version will also be compiled due to how this works)
val onlyId = System.getProperty("ru.vidtu.ias.only")
val latestId = "${versions[0]}-${types[0]}"

// Check the "only" version validity.
if (onlyId != null) {
    logger.warn("Processing only version '${onlyId}' via 'ru.vidtu.ias.only'.")
    val idx = onlyId.indexOf('-')
    require(idx != -1) { "Invalid only version '${onlyId}', no '-' delimiter extracted from 'ru.vidtu.ias.only'." }
    val onlyVersion = onlyId.substring(0, idx)
    val onlyType = onlyId.substring(idx + 1)
    require(versions.contains(onlyVersion)) { "Invalid only version '${onlyId}', version number '${onlyVersion}' extracted from 'ru.vidtu.ias.only' not found in ${versions.joinToString()}." }
    require(types.contains(onlyType)) { "Invalid only version '${onlyId}', type '${onlyType}' extracted from 'ru.vidtu.ias.only' not found in ${types.joinToString()}." }
}

// Create the "excluded" version list. It serves no real purpose, just to log.
val excluded = mutableListOf<String>()

// Setup stonecutter.
stonecutter {
    // Enable kts support.
    kotlinController = true

    // Setup.
    create(rootProject) {
        // Create projects.
        for (version in versions) {
            // Process the "supported" versions.
            // Note: There's no concept of "supported" loaders.
            if ((onlyId == null) && !includeLegacyVersions && !supportedVersions.contains(version)) {
                excluded.add(version)
                continue
            }

            // Iterate types.
            for (type in types) {
                // Extract the ID.
                val id = "${version}-${type}"
                if (id == "1.20.1-neoforge") continue // FIXME(VidTu): Too many hacks for now, fix later.

                // Process the "only" version ID.
                if ((onlyId != null) && (id != onlyId) && (id != latestId)) continue

                // Check if version is ignored.
                val subPath = file("versions/${id}")
                if (subPath.resolve(".ignored").isFile) {
                    excluded.add(id)
                    continue
                }

                // Setup the project.
                val project = version(id, version)
                if (type == "fabric") {
                    // Fabric builds require "special care",
                    // because they use different plugin systems:
                    // - "intermediary" (remapped) for older (<=1.21.11) versions.
                    // - "mojmap" (non-remapped) for newer (>=26.1) versions.
                    val flavor = if (version.startsWith("1.")) "intermediary" else "mojmap"
                    project.buildscript = "build.fabric-${flavor}.gradle.kts"
                } else if (id == "1.20.1-neoforge") {
                    // NeoForge 1.20.1 is a piece of hacky mess that's basically
                    // Forge 1.20.1 with a "95% OFF" discount. It is loosely
                    // Forge, but not Forge. It uses Forge packages, but
                    // diverges from (can't keep up with) the (Lex) MCForge
                    // 1.20.1. I don't know why support this edge case
                    // for approximately 6 or 7 users total.
                    project.buildscript = "build.neoforge-hacky.gradle.kts"
                } else {
                    project.buildscript = "build.${type}.gradle.kts"
                }
            }
        }

        // Make the VCS version the latest one.
        vcsVersion = latestId
    }
}

// Log about excluded versions.
if (excluded.isNotEmpty()) {
    if (includeLegacyVersions) {
        logger.lifecycle("Excluded versions: ${excluded.joinToString()}. Ignored (.ignore) versions are always excluded. Legacy versions were included.")
    } else {
        logger.lifecycle("Excluded versions: ${excluded.joinToString()}. Ignored (.ignore) versions are always excluded. Legacy versions were excluded, use 'ru.vidtu.ias.legacy' property or '--legacy' script flag to include them.")
    }
}
