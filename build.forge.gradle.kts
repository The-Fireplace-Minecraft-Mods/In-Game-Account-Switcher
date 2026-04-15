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

// This is the Forge loader buildscript. It is processed by the
// Stonecutter multiple times, for each version. (compiled once)
// Based on ForgeGradle and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.minecraftforge.renamer.gradle.RenameJar

// Configure plugins.
plugins {
    alias(libs.plugins.forgegradle)
    alias(libs.plugins.forgerenamer)
}

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = if (mcp >= "26.1.2") 25
else if (mcp >= "1.20.6") 21
else if (mcp >= "1.18.2") 17
else if (mcp >= "1.17.1") 16
else 8
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

// Add GSON to buildscript classpath, we use it for minifying JSON files.
buildscript {
    dependencies {
        classpath(libs.gson)
    }
}

// Define Stonecutter preprocessor variables/constants.
sc {
    constants["fabric"] = false
    constants["forge"] = true
    constants["hacky_neoforge"] = false
    constants["neoforge"] = false
    swaps["minecraft_version"] = "\"${mcv}\""
}

// Migration helper.
sourceSets["main"].java.srcDir("src/_legacy/_shared")
if (mcp <= "1.21.5") {
    sourceSets["main"].java.srcDir("src/_legacy/${mcv}/root")
    sourceSets["main"].java.srcDir("src/_legacy/${mcv}/forge")
    sourceSets["main"].java.setSrcDirs(sourceSets["main"].java.srcDirs.filter { !"${it}".contains("stonecutter") })
}

minecraft {
    // Mappings.
    if (mcp <= "26.1.2") {
        mappings("official", mcv)
    }

    // Set up runs.
    runs {
        // Customize the client run.
        register("client") {
            // Set up debug VM args.
            if (javaVersion.isJava9Compatible) {
                jvmArgs("@../dev/args.vm.txt")
            } else {
                jvmArgs(rootDir.resolve("dev/args.vm.txt")
                    .readLines()
                    .filter { "line.separator" !in it }
                    .filter { it.isNotBlank() })
            }

            // Set the run dir.
            workingDir = file("../../run")

            // AuthLib for 1.16.5 is bugged, disable Mojang API
            // to fix issues with multiplayer testing.
            if (mcp eq "1.16.5") {
                systemProperty("minecraft.api.account.host", "http://0.0.0.0:0/")
                systemProperty("minecraft.api.auth.host", "http://0.0.0.0:0/")
                systemProperty("minecraft.api.services.host", "http://0.0.0.0:0/")
                systemProperty("minecraft.api.session.host", "http://0.0.0.0:0/")
            }
        }
    }
}

repositories {
    mavenCentral()
    maven(fg.forgeMaven) // Forge.
    maven(fg.minecraftLibsMaven) // Minecraft Libraries.
    minecraft.mavenizer(this) // Minecraft.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Mixin.
    if (mcp < "1.20.6") {
        annotationProcessor("${libs.mixin.get()}:processor")
    }

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Minecraft and Forge.
    val forge = "${property("sc.forge")}"
    require(forge.isNotBlank() && forge != "[SC]") { "Forge version is not provided via 'sc.forge' in ${project}." }
    val extractedMinecraft = forge.substringBefore('-')
    require(mcp eq extractedMinecraft) { "Forge version '${forge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
    implementation(minecraft.dependency("net.minecraftforge:forge:${forge}"))
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    source(rootDir.resolve("src/_legacy/_shared")) // Migration helper.
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by 1.16.x) doesn't support the "-release" flag and
    // uses "-source" and "-target" ones (see the top of the file),
    // so we must NOT specify it, or the "javac" will fail.
    // JDK 9+ does listen to this option.
    if (javaVersion.isJava9Compatible) {
        options.release = javaTarget
    }
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
    inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/updater-forge.json")

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != mcv) { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: mcv
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version. Forge is full of edge-cases covered here.
    val mixinJava = if (mcp >= "26.1.2") 21
    else if (mcp eq "1.20.6") 18
    else javaTarget
    inputs.property("mixinJava", mixinJava)

    // Expand version and dependencies.
    inputs.property("version", version)
    inputs.property("platform", "forge")
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

if (mcp >= "1.20.6") {
    // Output into "build/libs" instead of "versions/<ver>/build/libs".
    tasks.withType<Jar> {
        destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
    }
} else {
    // Rename. (remap)
    renamer {
        // Specify mappings.
        mappings(minecraft.dependency.toSrg)

        // Remap mixins.
        enableMixinRefmaps {
            config("ias.mixins.json")
            source(sourceSets["main"]) {
                refMap = "ias.mixins.refmap.json"
            }
            jar(tasks.named<Jar>("jar"))
        }

        // Use Mixin mappings for field remapping.
        classes(tasks.named<Jar>("jar")) {
            mappings(renamer.mixin.generatedMappings)
            archiveClassifier = "srg"
        }
    }

    // Output remapped JAR into "build/libs" instead of "versions/<ver>/build/libs".
    tasks.withType<RenameJar> {
        output = rootProject.layout.buildDirectory.file("libs").get().asFile.resolve("IAS-${version}.jar")
    }
}
