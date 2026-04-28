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
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.minecraftforge.net/") // Forge.
        maven("https://maven.neoforged.net/releases/") // NeoForge.
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.2"
}

// Project.
rootProject.name = "In-Game Account Switcher"

// Prepare the list of versions and types.
val types = listOf("fabric", "forge", "neoforge")
val versions = (file("dev/versions/versions_beta.txt").readLines()
        + file("dev/versions/versions_active.txt").readLines()
        + file("dev/versions/versions_legacy.txt").readLines())
    .filter { it.isNotEmpty() }
    .filter { !it.startsWith('#') }
    .toSet()

// Ignored version IDs. See that file for reasoning on such ignorance.
val ignoredIds = file("dev/versions/ignored.txt")
    .readLines()
    .filter { it.isNotEmpty() }
    .filter { !it.startsWith('#') }
    .toSet()

// Actively supported version system. See README.md for the support policy.
// Depends on the "ru.vidtu.ias.legacy" boolean system property:
// - "false" (default): Compile only versions listed in "supportedVersions".
// - "true": Compile all versions listed in "versions".
// If "only" version feature is used, this is ignored.
val supportedVersions = (file("dev/versions/versions_beta.txt").readLines()
        + file("dev/versions/versions_active.txt").readLines())
    .filter { it.isNotEmpty() }
    .filter { !it.startsWith('#') }
    .toSet()
require(versions.containsAll(supportedVersions)) { "Not all actively supported versions '${supportedVersions}' are listed in all supported versions '${versions}'." }
val includeLegacyVersions = System.getProperty("ru.vidtu.ias.legacy").toBoolean()

// Process the "only" version feature.
// Pass the "ru.vidtu.ias.only" system property with "<version>-<type>"
// to the Gradle daemon, and it will compile only* the required version,
// which may reduce the build time if you don't need other versions.
// (* Sometimes, the latest version will also be compiled due to how this works)
val onlyId: String? = System.getProperty("ru.vidtu.ias.only")
val latestId = "${versions.first()}-${types.first()}"

// Check the "only" version validity.
if (onlyId != null) {
    logger.warn("Processing only version '${onlyId}' via 'ru.vidtu.ias.only'.")
    val idx = onlyId.indexOf('-')
    require(idx != -1) { "Invalid only version '${onlyId}', no '-' delimiter extracted from 'ru.vidtu.ias.only'." }
    val onlyVersion = onlyId.take(idx)
    val onlyType = onlyId.substring(idx + 1)
    require(onlyVersion in versions) { "Invalid only version '${onlyId}', version number '${onlyVersion}' extracted from 'ru.vidtu.ias.only' not found in ${versions.joinToString()}." }
    require(onlyType in types) { "Invalid only version '${onlyId}', type '${onlyType}' extracted from 'ru.vidtu.ias.only' not found in ${types.joinToString()}." }
}

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
            if ((onlyId == null) && !includeLegacyVersions && (version !in supportedVersions)) continue

            // Iterate types.
            for (type in types) {
                // Extract the ID.
                val id = "${version}-${type}"

                // Process the "only" version ID.
                if ((onlyId != null) && (id != onlyId) && (id != latestId)) continue

                // Check if version ID is ignored.
                if (id in ignoredIds) continue

                // Set up the project.
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

// Log about mode.
val mode = if (onlyId != null) "Only:${onlyId}"
else if (includeLegacyVersions) "Legacy"
else "Normal"
logger.lifecycle("Mode: '${mode}'.")
