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
// Based on ModDevGradle and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

// NeoForge 1.20.1 is a piece of hacky mess that's basically Forge 1.20.1 with
// a "95% OFF" discount. It is loosely Forge, but not Forge. It uses Forge
// packages, but diverges from (can't keep up with) the (Lex) MCForge 1.20.1.
// I don't know why support this edge case for approximately 6 or 7 users total.

import com.google.gson.Gson
import com.google.gson.JsonElement

// Configure plugins.
plugins {
    alias(libs.plugins.moddevgradle.legacy)
}

// Language.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

// Metadata.
group = "ru.vidtu.ias"
base.archivesName = "IAS"
version = "${version}+${name}"
description = "Allows you to change which account you are signed in to in-game without restarting Minecraft."

// Define Stonecutter preprocessor variables/constants.
sc {
    constants["fabric"] = false
    constants["forge"] = true // Yes, that's correct for NeoForge 1.20.1.
    constants["hacky_neoforge"] = true // And that's extremely correct.
    constants["neoforge"] = false // Yes, that's also correct.
    swaps["minecraft_version"] = "\"1.20.1\""
}

// Migration helper.
sourceSets["main"].java.srcDir("src/_legacy/_shared")
sourceSets["main"].java.srcDir("src/_legacy/1.20.1/root")
sourceSets["main"].java.srcDir("src/_legacy/1.20.1/neoforge")
sourceSets["main"].java.setSrcDirs(sourceSets["main"].java.srcDirs.filter { !"${it}".contains("stonecutter") })

legacyForge {
    // Minecraft and Forge.
    val neoforge = "${property("sc.neoforge")}"
    require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge (Hacky) version is not provided via 'sc.neoforge' in ${project}." }
    val extractedMinecraft = neoforge.substringBefore('-')
    require(extractedMinecraft == "1.20.1") { "NeoForge (Hacky) version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want 1.20.1." }
    enable {
        // Set the version.
        neoForgeVersion = neoforge

        // Enable recompilation for CI.
        // TODO(VidTu): Check if needed
        setDisableRecompilation(false)
    }

    // Set up runs.
    runs {
        // Customize the client run.
        register("client") {
            // Make client.
            client()

            // Set up debug VM args.
            jvmArguments.addAll(rootDir.resolve("dev/args.vm.txt")
                    .readLines()
                    .filter { "line.separator" !in it }
                    .filter { it.isNotBlank() })
            loggingConfigFile = rootDir.resolve("dev/log4j2.xml")

            // Set the run dir.
            gameDirectory = file("../../run")
        }
    }

    // Register sourcesets for debugging.
    mods {
        register("ias") {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets["main"], "ias.mixins.refmap.json")
    config("ias.mixins.json")
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") // NeoForge.
    maven("https://maven.minecraftforge.net/") // Forge.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Mixin.
    annotationProcessor("${libs.mixin.get()}:processor")

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    source(rootDir.resolve("src/_legacy/_shared")) // Migration helper.
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    require(platformRequirement.isNotBlank() && platformRequirement != "[SC]") { "Platform requirement is not provided via 'sc.platform-requirement' in ${project}." }
    inputs.property("platformRequirement", platformRequirement)

    // Expand the updater URL.
    inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/updater-neoforge.json")

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != "1.20.1") { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: "1.20.1"
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version.
    inputs.property("mixinJava", 17)

    // Expand version and dependencies.
    inputs.property("version", version)
    inputs.property("platform", "forge") // Yes, that's correct for NeoForge 1.20.1.
    filesMatching(listOf("ias.mixins.json", "META-INF/mods.toml")) {
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
