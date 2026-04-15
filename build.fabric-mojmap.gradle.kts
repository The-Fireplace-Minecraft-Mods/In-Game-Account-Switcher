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

// This is the Mojmap Fabric loader buildscript. It is processed by the
// Stonecutter multiple times, for each non-remapped version. (compiled once)
// Based on Loom and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.fabricmc.loom.task.RunGameTask

// Configure plugins.
plugins {
    alias(libs.plugins.fabric.loom)
}

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = 25
val javaVersion = JavaVersion.toVersion(javaTarget)!!
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain.languageVersion = JavaLanguageVersion.of(javaTarget)
}

// Metadata.
group = "ru.vidtu.ias"
base.archivesName = "IAS"
version = "${version}+${name}"
description = "Allows you to change which account you are signed in to in-game without restarting Minecraft."

// Define Stonecutter preprocessor variables/constants.
sc {
    constants["fabric"] = true
    constants["forge"] = false
    constants["hacky_neoforge"] = false
    constants["neoforge"] = false
    swaps["minecraft_version"] = "\"${mcv}\""
}

// Migration helper.
sourceSets["main"].java.srcDir("src/_legacy/_shared")

loom {
    // Use debug logging config.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))

    // Set up runs.
    runs {
        // Customize the client run.
        named("client") {
            // Set up debug VM args.
            vmArgs("@../dev/args.vm.txt")

            // Set the run dir.
            runDir = "../../run"
        }

        // Remove server run, the mod is client-only.
        remove(findByName("server"))
    }

    // Configure Mixin.
    @Suppress("UnstableApiUsage") // <- Required to configure Mixin.
    mixin {
        // Use direct remapping instead of annotation processor and refmaps.
        useLegacyMixinAp = false
    }
}

// Make the game run with the compatible Java. (e.g,. Java 17 for 1.20.1)
tasks.withType<RunGameTask> {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") // Fabric.
    maven("https://maven.terraformersmc.com/releases/") // ModMenu.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Minecraft. The dependency may be manually specified for example for snapshots.
    val minecraftDependencyProperty = findProperty("sc.minecraft-dependency")
    require(minecraftDependencyProperty != mcv) { "Unneeded 'sc.minecraft-dependency' property set to ${minecraftDependencyProperty} in ${project}, it already uses this version." }
    val minecraftDependency = minecraftDependencyProperty ?: mcv
    minecraft("com.mojang:minecraft:${minecraftDependency}")

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Fabric Loader.
    implementation(libs.fabric.loader)

    // Modular Fabric API.
    val fapi = "${property("sc.fabric-api")}"
    require(fapi.isNotBlank() && fapi != "[SC]") { "Fabric API version is not provided via 'sc.fabric-api' in ${project}." }
    implementation(fabricApi.module("fabric-lifecycle-events-v1", fapi)) // Handles game ticks.
    implementation(fabricApi.module("fabric-resource-loader-v1", fapi)) // Loads languages.

    // ModMenu.
    val modmenu = "${property("sc.modmenu")}"
    require(modmenu.isNotBlank() && modmenu != "[SC]") { "ModMenu version is not provided via 'sc.modmenu' in ${project}." }
    // Sometimes, ModMenu is not yet updated for the version. (it almost never updates to snapshots nowadays)
    // So we should depend on it compile-time (it is really an optional dependency for us) to allow both
    // compilation of an optional ModMenu compatibility class (HModMenu.java) and launching the game.
    if ("${findProperty("sc.modmenu.compile-only")}".toBoolean()) {
        compileOnly("com.terraformersmc:modmenu:${modmenu}")
    } else {
        implementation("com.terraformersmc:modmenu:${modmenu}")
        implementation(fabricApi.module("fabric-key-mapping-api-v1", fapi)) // ModMenu dependency.
        implementation(fabricApi.module("fabric-screen-api-v1", fapi)) // ModMenu dependency.
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    source(rootDir.resolve("src/_legacy/_shared")) // Migration helper.
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = javaTarget
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")

    // Replace the Fabric Resource Loader version.
    // >=26.1.2 has consistent v1, this is used by Intermediary.
    inputs.property("fabricResourceLoaderRevision", "v1")

    // Replace the Fabric API module name.
    // >=26.1.2 has consistent fabric-api, this is used by Intermediary.
    inputs.property("fabricApiName", "fabric-api")

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    require(platformRequirement == "[SC]") { "Platform requirement is provided via 'sc.platform-requirement' in ${project}, but Fabric builds ignore it." }

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != mcv) { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: mcv
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version.
    inputs.property("mixinJava", javaTarget)

    // Expand version and dependencies.
    inputs.property("version", version)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "ias.mixins.json")) {
        expand(inputs.properties)
    }

    // Minify JSON files.
    val files = fileTree(outputs.files.asPath)
    doLast {
        files.forEach {
            if (it.name.endsWith(".json", ignoreCase = true)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            }
        }
    }
}

tasks.withType<Jar> {
    // Add LICENSE and NOTICE.
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.ias.debug.package")}".toBoolean()) {
        exclude("**/package-info.class")
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<Jar> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}
