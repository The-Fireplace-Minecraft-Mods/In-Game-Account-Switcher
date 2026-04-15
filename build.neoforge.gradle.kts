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

// This is the main (multi-version loader) buildscript. It is processed by the
// Stonecutter multiple times, for each version and each loader. (compiled once)
// Based on NeoGradle and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement

// Configure plugins.
plugins {
    alias(libs.plugins.neogradle)
}

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = if (mcp >= "26.1.2") 25
else if (mcp >= "1.20.6") 21
else 17
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
    constants["fabric"] = false
    constants["forge"] = false
    constants["hacky_neoforge"] = false
    constants["neoforge"] = true
    swaps["minecraft_version"] = "\"${mcv}\""
}

// Migration helper.
sourceSets["main"].java.srcDir("src/_legacy/_shared")
if (mcp <= "1.21.5") {
    sourceSets["main"].java.srcDir("src/_legacy/${mcv}/root")
    sourceSets["main"].java.srcDir("src/_legacy/${mcv}/neoforge")
    sourceSets["main"].java.setSrcDirs(sourceSets["main"].java.srcDirs.filter { !"${it}".contains("stonecutter") })
}

// Set up runs.
runs {
    // Customize the client run.
    register("client") {
        // Set up debug VM args.
        jvmArguments("@../dev/args.vm.txt")

        // Set the run dir.
        workingDirectory = file("../../run")
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") // NeoForge.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Minecraft and NeoForge.
    val neoforge = "${property("sc.neoforge")}"
    require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge version is not provided via 'sc.neoforge' in ${project}." }
    val extractedMinecraft = if (mcp >= "26.1.2") neoforge.substringBeforeLast('.') else "1.${neoforge.substringBeforeLast('.')}"
    require(mcp eq extractedMinecraft) { "NeoForge version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
    implementation("net.neoforged:neoforge:${neoforge}")
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
    if (mcp >= "1.20.6") {
        exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
    } else {
        exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
    }

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    require(platformRequirement.isNotBlank() && platformRequirement != "[SC]") { "Platform requirement is not provided via 'sc.platform-requirement' in ${project}." }
    inputs.property("platformRequirement", platformRequirement)

    // Expand the updater URL.
    inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/updater-neoforge.json")

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != mcv) { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: mcv
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version.
    inputs.property("mixinJava", javaTarget)

    // Expand version and dependencies.
    inputs.property("version", version)
    inputs.property("platform", "neoforge")
    filesMatching(listOf("ias.mixins.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(inputs.properties)
    }

    // Minify JSON (including ".mcmeta") and TOML files.
    val files = fileTree(outputs.files.asPath)
    doLast {
        val jsonAlike = Regex("^.*\\.(?:json|mcmeta)$", RegexOption.IGNORE_CASE)
        files.forEach {
            if (it.name.matches(jsonAlike)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                it.writeText(it.readLines()
                    .filter { s -> !s.startsWith('#') }
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
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

    // Add manifest.
    manifest {
        attributes("MixinConfigs" to "ias.mixins.json")
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<Jar> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}
